package cn.xu.rondo.controller;


import cn.hutool.core.date.DateUtil;
import cn.hutool.http.HtmlUtil;
import cn.xu.rondo.entity.Message;
import cn.xu.rondo.entity.Room;
import cn.xu.rondo.entity.Song;
import cn.xu.rondo.entity.User;
import cn.xu.rondo.entity.vo.MsgVo;
import cn.xu.rondo.enums.ChatType;
import cn.xu.rondo.enums.ErrorEnum;
import cn.xu.rondo.response.exception.ApiException;
import cn.xu.rondo.service.IMessageService;
import cn.xu.rondo.service.IRoomService;
import cn.xu.rondo.service.IUserService;
import cn.xu.rondo.socket.IMSocket;
import cn.xu.rondo.utils.Common;
import cn.xu.rondo.utils.Constants;
import cn.xu.rondo.utils.RedisUtil;
import cn.xu.rondo.utils.StringUtils;
import cn.xu.rondo.utils.params_resolver.UserId;
import cn.xu.rondo.utils.validation.EnumValue;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.validator.constraints.Length;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import org.springframework.stereotype.Controller;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * <p>
 * 消息表 前端控制器
 * </p>
 *
 * @author jason xu
 * @since 2021-07-17
 */
@Slf4j
@Validated
@RestController
@RequestMapping("/message")
public class MessageController extends BaseController {
    @Autowired
    IRoomService roomService;
    @Autowired
    IMessageService messageService;
    @Autowired
    IUserService userService;
    @Autowired
    IMSocket imSocket;

    @Autowired
    RedisUtil redis;

    @DeleteMapping("/back/{room_id}/{msg_id}")
    public String back(@PathVariable("room_id") Integer roomId,
                       @PathVariable("msg_id") Integer msgId,
                       @UserId Integer userId) {
        User user = userService.getById(userId);
        Room room = roomService.getById(roomId);
        Message msg = messageService.getById(msgId);
        if (room == null || msg == null) throw new ApiException(ErrorEnum.INFO_QUERY_ERR);
        if (!roomId.equals(msg.getMessage_to()) && !"channel".equals(msg.getMessage_where())) {
            throw new ApiException(ErrorEnum.BACK_ERR);
        }
        if (!user.isAdmin() && !userId.equals(msg.getMessage_user()) && !userId.equals(room.getRoom_user())) {
            throw new ApiException(ErrorEnum.PERMISSION_LOW);
        }
        if (Common.time() > msg.getMessage_createtime() + 300) {
            throw new ApiException(ErrorEnum.BACK_ONLY_5);
        }
        JSONObject data = new JSONObject();
        user.setUser_password(null);
        data.put("user", user);
        data.put("message_id", msgId);
        String imMsg = new MsgVo(MsgVo.BACK, data).build();
        imSocket.sendMsgToRoom(String.valueOf(roomId), imMsg);
        messageService.removeById(msgId);
        return "消息撤回成功";
    }

    @DeleteMapping("/back/{room_id}")
    public String clear(@PathVariable("room_id") Integer roomId,
                        @UserId Integer userId) {
        User user = userService.getById(userId);
        Room room = roomService.getById(roomId);
        if (room == null) throw new ApiException(ErrorEnum.INFO_QUERY_ERR);
        if (!user.isAdmin() && !userId.equals(room.getRoom_user())) {
            throw new ApiException(ErrorEnum.PERMISSION_LOW);
        }
        JSONObject data = new JSONObject();
        user.setUser_password(null);
        data.put("user", user);
        String msg = new MsgVo(MsgVo.CLEAR, data).build();
        imSocket.sendMsgToRoom(String.valueOf(roomId), msg);
        return "房间消息清除成功!";
    }

    @GetMapping("/list/{room_id}")
    public List<Message> list(@PathVariable("room_id") Integer roomId,
                              @RequestParam(value = "page_num", required = false, defaultValue = "1") Integer pageNum,
                              @RequestParam(value = "page_size", required = false, defaultValue = "20") Integer pageSize,
                              @UserId Integer userId) {

        Room room = roomService.getById(roomId);
        if (!room.isPublic()) {
            if (Common.isVisitor()) {
                throw new ApiException(ErrorEnum.ROOM_SECURE);
            } else {
                //加密房间 非游客
                User user = userService.getById(userId);
                if (user == null) throw new ApiException(ErrorEnum.INFO_QUERY_ERR);
                String savedPassword = redis.getCacheObject(Constants.SavedPwd(roomId, userId));
                if (!userId.equals(room.getRoom_user()) && !user.isAdmin() && !room.getRoom_password().equals(savedPassword)) {
                    throw new ApiException(ErrorEnum.ROOM_SECURE);
                }
            }
        }

        List<Message> list = redis.getCacheList(Constants.RoomMsgList + roomId);
        if (list != null && list.size() != 0) return list;

        Page<Message> page = new Page<>(pageNum, pageSize);
        QueryWrapper<Message> wrap = new QueryWrapper<>();
        wrap.eq("message_to", roomId);
        wrap.eq("message_status", 0);
        Page<Message> msgPager = messageService.page(page, wrap);
        final List<Message> records = msgPager.getRecords();
        redis.setCacheList(Constants.RoomMsgList + roomId, records);
        redis.expire(Constants.RoomMsgList + roomId, 10, TimeUnit.SECONDS);
        return records;
    }

    @PostMapping("/send")
    public String send(@RequestParam("to") Integer roomId,
                       @RequestParam(value = "at", required = false) Integer atUserId,
                       @RequestParam("type") @EnumValue(enumClass = ChatType.class, message = "未知消息类型") String type,
                       @RequestParam(value = "where", required = false, defaultValue = "channel") String where,
                       @RequestParam(value = "msg", required = false, defaultValue = "") @Length(max = 200) String msg,
                       @RequestParam(value = "resource", required = false, defaultValue = "") String resource,
                       @UserId Integer userId) {
        final Room room = roomService.getById(roomId);
        final User user = userService.getById(userId);
        if (room == null) throw new ApiException(ErrorEnum.ROOM_NOT_FOUND);
        String savedPassword = redis.getCacheObject(Constants.SavedPwd(roomId, userId));
        // 房间密码
        if (!room.isPublic() && !user.isAdmin() && room.isOwner(userId) && !room.getRoom_password().equals(savedPassword)) {
            throw new ApiException(ErrorEnum.ROOM_PSD_ERR);
        }
        // 全员禁言判断
        if (!user.isAdmin() && !room.isOwner(userId) && room.isBanAll()) {
            throw new ApiException(ErrorEnum.ALL_MUTE);
        }
        // 个人禁言判断
        if (Common.checkShutdown(0, roomId, userId)) {
            throw new ApiException(ErrorEnum.MUTE);
        }
        // 判断 ip 地址禁言
        if (messageService.checkIPBAN()) {
            throw new ApiException(ErrorEnum.IP_MUTE);
        }

        // TODO: 判断嘉宾发言 , 暂时pass嘉宾功能 room_sendmsg == 1

        // 处理TEXT消息
        if (ChatType.TEXT.equals(type)) {
            msg = HtmlUtil.filter(msg);
            if (StringUtils.isEmpty(msg)) {
                throw new ApiException(ErrorEnum.MSG_EMPTY);
            }
            // 检测到管理员 @all 消息，向所有房间的人发送消息
            if (user.isAdmin()) {
                if (msg.contains("@all")) {
                    final String content = msg.replace("@all", "");
                    String imMsg = new MsgVo(MsgVo.SYSTEM, new JSONObject() {{
                        put("content", content);
                    }}).build();
                    imSocket.broadcast(imMsg);
                    return "广播发送成功";
                }
            }
            // 房主/管理员不受限制
            if (!room.isOwner(userId) && !user.isAdmin()) {
                // 在1s内频繁发送消息判断
                Boolean isLast = redis.getCacheObject(Constants.LastSend + userId);
                if (isLast != null) throw new ApiException(ErrorEnum.OFTEN_MSG);
                // 10s内频繁发送相同的消息判断
                String lastContent = redis.getCacheObject(Constants.LastMsg + userId);
                if (msg.equals(lastContent)) throw new ApiException(ErrorEnum.SAME_CONTENT);
            }

        }
        // 处理IMG消息
        if (ChatType.IMG.equals(type)) {
            if (!user.isAdmin() && !user.isVip()) {
                // 在 18 -- 9 点之间 不允许发送自定义图片
//                if (messageService.checkTimeIn()) {
//
//                }
            }

            // 发送TODO:: 320行
        }

        return "";
    }
}

