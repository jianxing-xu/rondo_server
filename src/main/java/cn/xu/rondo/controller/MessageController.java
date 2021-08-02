package cn.xu.rondo.controller;


import cn.hutool.core.util.RandomUtil;
import cn.hutool.http.HtmlUtil;
import cn.hutool.http.HttpUtil;
import cn.xu.rondo.entity.Keywords;
import cn.xu.rondo.entity.Message;
import cn.xu.rondo.entity.Room;
import cn.xu.rondo.entity.User;
import cn.xu.rondo.entity.dto.AtDTO;
import cn.xu.rondo.entity.dto.SendMsgDTO;
import cn.xu.rondo.entity.dto.message.MoDTO;
import cn.xu.rondo.entity.vo.MsgVo;
import cn.xu.rondo.enums.ChatType;
import cn.xu.rondo.enums.EE;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotNull;
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
    public String back(@PathVariable("room_id") Integer roomId,
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
        return "消息撤回成功";
    }

    @DeleteMapping("/back/{room_id}")
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

    @GetMapping("/list/{room_id}")
    public List<Message> list(@PathVariable("room_id") Integer roomId,
                              @RequestParam(value = "page_num", required = false, defaultValue = "1") Integer pageNum,
                              @RequestParam(value = "page_size", required = false, defaultValue = "20") Integer pageSize,
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
        // 房间密码
        if (!room.isPublic() && !user.isAdmin() && room.isOwner(userId) && !room.getRoom_password().equals(savedPassword)) {
            throw ERR(EE.ROOM_PSD_ERR);
        }
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

        // TODO: 判断嘉宾发言 , 暂时pass嘉宾功能 room_sendmsg == 1 Coming soon


        // 处理TEXT消息
        if (ChatType.TEXT.equals(type)) {
            msg = HtmlUtil.filter(msg);
            if (StringUtils.isEmpty(msg)) {
                throw ERR(EE.MSG_EMPTY);
            }
            // 检测到管理员 @all 消息，向所有房间的人发送消息
            if (user.isAdmin()) {
                if (msg.contains("@all")) {
                    final String content = msg.replace("@all", "");
                    String imMsg = new MsgVo(MsgVo.SYSTEM, new JSONObject() {{
                        put("content", content);
                    }}).build();
                    broadcast(imMsg);
                    return "广播发送成功";
                }
            }
            // 关键词过滤
            final List<Keywords> keys = keywords();
            for (Keywords k : keys) {
                msg = msg.replace(k.getKeywords_source(), k.getKeywords_target());
            }
        }
        // 处理IMG消息
        if (ChatType.IMG.equals(type)) {
            if (!user.isAdmin() && !user.isVip()) {
                //在 18 -- 9 点之间 不允许发送自定义图片
                if (messageService.checkTimeIn()) throw ERR(EE.NOT_SUP_SEND_PIC);
            }
            if (StringUtils.isEmpty(resource)) throw ERR(EE.MSG_EMPTY);

            //站外图片绝对路径判断
            if (HttpUtil.isHttp(resource) || HttpUtil.isHttps(resource)) {
                resource = resource.replace("https", "");
                resource = resource.replace("http", "");
                if (!resource.contains(get("api_url"))) throw ERR(EE.NOT_SUP_SEND_PIC);
            }
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
                // 插入数据库
                Message message = new Message();
                message.setMessage_user(userId);
                message.setMessage_type(type);
                message.setMessage_content("");
                message.setMessage_to(roomId);
                message.setMessage_where(where);
                message.setMessage_status(1);
                message.setMessage_createtime(Common.time().intValue());
                message.setMessage_updatetime(Common.time().intValue());

                // GEN ID
                messageService.save(message);

                JSONObject data = new JSONObject();
                data.put("content", msg);
                data.put("where", where);
                data.put("at", atUser);
                data.put("message_id", message.getMessage_id());
                data.put("message_time", Common.time());
                data.put("resource", msg);
                user.setUser_password(null);
                data.put("user", user);
                String imMsg = new MsgVo(MsgVo.TEXT, data).build();
                // 发送socket消息
                sendMsg(roomId, imMsg);
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
                imgData.put("content", resource);
                imgData.put("where", where);
                imgData.put("at", atUser);
                imgData.put("message_id", imgMsg.getMessage_id());
                imgData.put("message_time", Common.time());
                imgData.put("resource", resource);
                user.setUser_password(null);
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
    public String mo(@RequestBody MoDTO dto,
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
            isRobotEnable = !Common.checkShutdown(0, roomId, 1) && random > 10;
        }
        if (isRobotEnable) {
            User robotInfo = userService.getById(1);
            String content = Constants.touch_machine[RandomUtil.randomInt(0, Constants.touch_machine.length - 1)];
            JSONObject macMsg = new JSONObject();
            macMsg.put("content", content);
            macMsg.put("where", roomId);
            macMsg.put("at", new JSONObject() {{
                put("user_id", userId);
                put("user_name", user.getUser_name());
            }});
            macMsg.put("message_id", 0);
            macMsg.put("resource", content);
            robotInfo.setUser_password(null);
            macMsg.put("user", robotInfo);
            sendMsg(roomId, new MsgVo(MsgVo.TEXT, macMsg).build());
        }

        return "摸好了！";
    }

}