package cn.xu.rondo.controller;


import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.http.HtmlUtil;
import cn.hutool.http.HttpUtil;
import cn.xu.rondo.entity.*;
import cn.xu.rondo.entity.dto.AtDTO;
import cn.xu.rondo.entity.dto.SendMsgDTO;
import cn.xu.rondo.entity.dto.message.MoDTO;
import cn.xu.rondo.entity.vo.MessageVO;
import cn.xu.rondo.entity.vo.MsgVo;
import cn.xu.rondo.enums.ChatType;
import cn.xu.rondo.enums.EE;
import cn.xu.rondo.interceptor.VisitorInter;
import cn.xu.rondo.response.Response;
import cn.xu.rondo.service.IMessageService;
import cn.xu.rondo.service.IRoomService;
import cn.xu.rondo.service.IUserService;
import cn.xu.rondo.utils.Common;
import cn.xu.rondo.utils.Constants;
import cn.xu.rondo.utils.RedisUtil;
import cn.xu.rondo.utils.StringUtils;
import cn.xu.rondo.utils.params_resolver.UserId;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.annotations.Delete;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.Comparator;
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
    RedisUtil redis;

    @DeleteMapping("/back/{room_id}/{msg_id}")
    public Response<String> back(@PathVariable("room_id") Integer roomId,
                                 @PathVariable("msg_id") Integer msgId,
                                 @UserId Integer userId) {
        User user = userService.getById(userId);
        Room room = roomService.getById(roomId);
        Message msg = messageService.getById(msgId);
        if (room == null || msg == null) throw ERR(EE.INFO_QUERY_ERR);
        if (!roomId.equals(msg.getMessage_to()) && !"channel".equals(msg.getMessage_where())) {
            throw ERR(EE.BACK_ERR);
        }
        if (!user.isAdmin() && !userId.equals(msg.getMessage_user()) && !userId.equals(room.getRoom_user())) {
            throw ERR(EE.PERMISSION_LOW);
        }
        if (Common.time() > msg.getMessage_createtime() + 300) {
            throw ERR(EE.BACK_ONLY_5);
        }
        JSONObject data = new JSONObject();
        user.setUser_password(null);
        data.put("user", user);
        data.put("message_id", msgId);
        String imMsg = new MsgVo(MsgVo.BACK, data).build();
        sendMsg(roomId, imMsg);
        messageService.removeById(msgId);
        return Response.successTip("消息撤回成功");
    }

    @DeleteMapping("/clear/{room_id}")
    public String clear(@PathVariable("room_id") Integer roomId,
                        @UserId Integer userId) {
        User user = userService.getById(userId);
        Room room = roomService.getById(roomId);
        if (room == null) throw ERR(EE.INFO_QUERY_ERR);
        if (!user.isAdmin() && !userId.equals(room.getRoom_user())) {
            throw ERR(EE.PERMISSION_LOW);
        }
        JSONObject data = new JSONObject();
        user.setUser_password(null);
        data.put("user", user);
        String msg = new MsgVo(MsgVo.CLEAR, data).build();
        sendMsg(roomId, msg);
        return "房间消息清除成功!";
    }

    @VisitorInter
    @GetMapping("/list/{room_id}")
    public List<MessageVO> list(@PathVariable("room_id") Integer roomId,
                                @RequestParam(value = "page_num", required = false, defaultValue = "1") Integer pageNum,
                                @RequestParam(value = "page_size", required = false, defaultValue = "20") Integer pageSize,
                                @RequestParam(value = "status", required = false, defaultValue = "0") Integer status,
                                @UserId Integer userId) {

        Room room = roomService.getById(roomId);
        if (!room.isPublic()) {
            if (Common.isVisitor()) {
                throw ERR(EE.ROOM_SECURE);
            } else {
                //加密房间 非游客
                User user = userService.getById(userId);
                if (user == null) throw ERR(EE.INFO_QUERY_ERR);
                String savedPassword = redis.getCacheObject(Constants.SavedPwd(roomId, userId));
                if (!userId.equals(room.getRoom_user()) && !user.isAdmin() && !room.getRoom_password().equals(savedPassword)) {
                    throw ERR(EE.ROOM_SECURE);
                }
            }
        }

        List<MessageVO> list = redis.getCacheList(Constants.RoomMsgList + roomId);
        if (list != null && list.size() != 0) return list;

        Page<MessageVO> page = new Page<>(pageNum, pageSize);
        final List<MessageVO> messageVOS = messageService.selectMessages(page, roomId, status);
        messageVOS.sort(Comparator.comparing(MessageVO::getMessage_id));
        if (messageVOS.size() != 0) {
            redis.setCacheList(Constants.RoomMsgList + roomId, messageVOS);
            redis.expire(Constants.RoomMsgList + roomId, 10, TimeUnit.SECONDS);
        }

        return messageVOS;
    }

    @PostMapping("/send")
    public String send(@RequestBody @Validated SendMsgDTO dto,
                       @UserId Integer userId) {
        Integer roomId = dto.getRoom_id();
        String type = dto.getType();
        String msg = dto.getMsg();
        String resource = dto.getResource();
        String where = dto.getWhere();
        AtDTO atUser = dto.getAtUser();
        final User user = checkUser(userId);
        final Room room = checkRoom(roomId);
        String savedPassword = redis.getCacheObject(Constants.SavedPwd(roomId, userId));
        // 全员禁言判断
        if (!user.isAdmin() && !room.isOwner(userId) && room.isBanAll()) {
            throw ERR(EE.ALL_MUTE);
        }
        // 个人禁言判断
        if (Common.checkShutdown(0, roomId, userId)) {
            throw ERR(EE.MUTE);
        }
        // 判断 ip 地址禁言
        if (messageService.checkIPBAN()) {
            throw ERR(EE.IP_MUTE);
        }
        // 房间密码
        if (!room.isPublic() && !user.isAdmin() && !room.isOwner(userId) && !room.getRoom_password().equals(savedPassword)) {
            throw ERR(EE.ROOM_PSD_ERR);
        }

        // TODO: 判断嘉宾发言 , 暂时pass嘉宾功能 room_sendmsg == 1 Coming soon


        // 处理TEXT消息
        if (ChatType.TEXT.equals(type)) {
            msg = HtmlUtil.filter(msg);
            if (StringUtils.isEmpty(msg)) {
                throw ERR(EE.MSG_EMPTY);
            }
            // 关键词过滤
            final List<Keywords> keys = keywords();
            for (Keywords k : keys) {
                msg = msg.replace(k.getKeywords_source(), k.getKeywords_target());
            }
        }
        // 处理IMG消息
        if (ChatType.IMG.equals(type)) {
            if (StringUtils.isEmpty(resource)) throw ERR(EE.MSG_EMPTY);

            //站外图片绝对路径判断 TODO: 站外图片支持 Coming soon,,,,,,,
//            if (!resource.contains(get("api_url"))) {
//                if (!user.isAdmin() && !user.isVip() && !room.isOwner(userId)) {
//                    //在 18 -- 9 点之间 不允许发送自定义图片
//                    if (messageService.checkTimeIn()) {
//                        throw ERR(EE.NOT_SUP_SEND_PIC);
//                    }
//                }
//            }
        }

        // 房主/管理员不受限制
        if (!room.isOwner(userId) && !user.isAdmin()) {
            // 在1s内频繁发送消息判断
            Integer isLast = redis.getCacheObject(Constants.LastSend + userId);
            if (isLast != null) throw ERR(EE.OFTEN_MSG);
            // 10s内频繁发送相同的消息判断
            String lastContent = redis.getCacheObject(Constants.LastMsg + userId);
            if (ChatType.TEXT.equals(type)) {
                if (msg.equals(lastContent)) throw ERR(EE.SAME_CONTENT);
            } else {
                if (resource.equals(lastContent)) throw ERR(EE.SAME_CONTENT);
            }
        }


        // 消息预处理
        switch (type) {
            case ChatType.TEXT:
                // TODO: 飞机票功能 Coming soon
                // TODO: 链接功能 Coming soon
                // 检测到管理员 @all 消息，向所有房间的人发送消息
                // 插入数据库
                Message message = new Message();
                message.setMessage_user(userId);
                message.setMessage_type("text");
                message.setMessage_content("");
                message.setMessage_to(roomId);
                message.setMessage_where(where);
                message.setMessage_status(1);
                message.setMessage_createtime(Common.time().intValue());
                message.setMessage_updatetime(Common.time().intValue());

                // GEN ID
                messageService.save(message);

                JSONObject data = new JSONObject();
                data.put("message_to", roomId);
                data.put("message_type", "text");
                data.put("message_content", msg);
                data.put("message_where", where);
                data.put("message_id", message.getMessage_id());
                data.put("message_createtime", Common.time());
                data.put("message_resource", msg);
                data.put("message_status", 0);
                data.put("message_user", userId);
                data.put("at", atUser);
                data.put("user", user);
                String imMsg = new MsgVo(MsgVo.TEXT, data).build();
                // 发送socket消息 、、 TODO: 判断@ALL
                if (msg.contains("@all")) {
                    broadcast(imMsg);
                } else {
                    sendMsg(roomId, imMsg);
                }
                // 更新内容
                message.setMessage_content(msg);
                message.setMessage_status(0);
                message.setMessage_type(type);
                messageService.updateById(message);
                // 设置最后发送缓存 和 最后发送消息缓存
                redis.setCacheObject(Constants.LastSend + userId, 1);
                redis.expire(Constants.LastSend + userId, 1, TimeUnit.SECONDS);
                redis.setCacheObject(Constants.LastMsg + userId, msg);
                redis.expire(Constants.LastMsg + userId, 10, TimeUnit.SECONDS);

                //
                if (user.isAdmin()) {
                    if (msg.contains("@all")) {
                        final String content = msg;
                        String toAllMsg = new MsgVo(MsgVo.SYSTEM, new JSONObject() {{
                            put("user", user);
                            put("content", content);
                            put("@all", true);
                        }}).build();
                        broadcast(toAllMsg);
//                    return Response.successTip("广播发送成功");
                    }
                }

                // TODO: 机器人彩蛋 Coming soon
                if (atUser != null && atUser.getUser_id() == 1) {
                    log.info("===================@机器人===============");
                }

                break;
            case ChatType.IMG:
                Message imgMsg = new Message();
                imgMsg.setMessage_user(userId);
                imgMsg.setMessage_type("img");
                imgMsg.setMessage_content("");
                imgMsg.setMessage_to(roomId);
                imgMsg.setMessage_where(where);
                imgMsg.setMessage_status(1);
                imgMsg.setMessage_createtime(Common.time().intValue());
                imgMsg.setMessage_updatetime(Common.time().intValue());
                messageService.save(imgMsg);

                JSONObject imgData = new JSONObject();
                imgData.put("message_to", roomId);
                imgData.put("message_type", "img");
                imgData.put("message_content", resource);
                imgData.put("message_where", where);
                imgData.put("message_id", imgMsg.getMessage_id());
                imgData.put("message_createtime", Common.time());
                imgData.put("message_resource", resource);
                imgData.put("message_status", 0);
                imgData.put("message_user", userId);
                user.setUser_password(null);
                imgData.put("at", atUser);
                imgData.put("user", user);
                String imgImMsg = new MsgVo(MsgVo.IMG, imgData).build();
                sendMsg(roomId, imgImMsg);

                imgMsg.setMessage_content(resource);
                imgMsg.setMessage_status(0);
                messageService.updateById(imgMsg);

                // 设置最后发送缓存 和 最后发送消息缓存
                redis.setCacheObject(Constants.LastSend + userId, 1);
                redis.expire(Constants.LastSend + userId, 1, TimeUnit.SECONDS);
                redis.setCacheObject(Constants.LastMsg + userId, resource);
                redis.expire(Constants.LastMsg + userId, 10, TimeUnit.SECONDS);

                break;
            default:
                throw ERR(EE.NOT_MSG_TYPE);
        }

        return "发送成功！";
    }


    @PostMapping("/mo")
    public Response<String> mo(@RequestBody MoDTO dto,
                               @UserId Integer userId) {
        Integer roomId = dto.getRoom_id();
        Integer atUserId = dto.getAt();
        final User user = checkUser(userId);
        final Room room = checkRoom(roomId);
        final User atUser = checkUser(atUserId);

        if (atUser.getUser_id().equals(userId)) throw ERR(EE.MO_ME);

        int mo_cd = 5;
        if (!user.isAdmin() && room.isOwner(userId)) {
            final Boolean lastMo = redis.getCacheObject(Constants.mo + userId);
            if (lastMo != null) throw ERR(EE.OFTEN_MO);
        }
        redis.setCacheObject(Constants.mo + userId, true);
        redis.expire(Constants.mo + userId, mo_cd, TimeUnit.SECONDS);

        JSONObject data = new JSONObject();
        data.put("user", userData(user));
        data.put("at", userData(atUser));
        String msg = new MsgVo(MsgVo.TOUCH, data).build();
        sendMsg(roomId, msg);

        // 摸一摸彩蛋
        boolean isRobotEnable = false;
        // 如果摸的是机器人
        if (atUserId != null && atUserId.equals(1)) {
            int random = RandomUtil.randomInt(0, 100);
            // TODO：摸机器人触发概率
            isRobotEnable = !Common.checkShutdown(0, roomId, 1) && random > 80;
        }
        if (isRobotEnable) {
            User robotInfo = userService.getById(1);
            String content = Constants.touch_machine[RandomUtil.randomInt(0, Constants.touch_machine.length - 1)];
            JSONObject macMsg = new JSONObject();
            macMsg.put("message_to", roomId);
            macMsg.put("message_type", "text");
            macMsg.put("message_content", content);
            macMsg.put("message_where", "channel");
            macMsg.put("message_id", IdUtil.fastUUID());
            macMsg.put("message_createtime", Common.time());
            macMsg.put("message_resource", content);
            macMsg.put("message_status", 0);
            //
            macMsg.put("at", new JSONObject() {{
                put("user_id", userId);
                put("user_name", user.getUser_name());
            }});
            robotInfo.setUser_password(null);
            macMsg.put("user", robotInfo);
            sendMsg(roomId, new MsgVo(MsgVo.TEXT, macMsg).build());
        }

        return Response.successTip("摸好了！");
    }


    // TODO: ADMIN interface

    /**
     * 条件查询所有消息列表
     *
     * @param pageNum  页码
     * @param pageSize 每页大小
     * @param keyword  搜索关键字 in (song_name,song_id,song_user,song_singer)
     * @return JSONObject
     */
    @GetMapping("/all")
    public JSONObject list(@RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum,
                           @RequestParam(value = "pageSize", defaultValue = "20") Integer pageSize,
                           @RequestParam(value = "keyword", defaultValue = "") String keyword) {
        final Page<Message> userPager = new Page<>(pageNum, pageSize);
        final QueryWrapper<Message> wrapper = new QueryWrapper<>();
        wrapper.like("message_id", keyword).or();
        wrapper.like("message_user", keyword).or();
        wrapper.like("message_content", keyword).or();
        wrapper.like("message_to", keyword).or();
        wrapper.like("message_type", keyword);
        final Page<Message> page = messageService.page(userPager, wrapper);
        JSONObject json = new JSONObject();
        json.put("list", page.getRecords());
        json.put("total", page.getTotal());
        return json;
    }

    @DeleteMapping("/delMsgs/{ids}")
    public void delMsgs(@PathVariable String ids) {
        messageService.removeByIds(Arrays.asList(ids.split(",")));
    }
}