package cn.xu.rondo.controller;


import cn.xu.rondo.entity.Conf;
import cn.xu.rondo.entity.Keywords;
import cn.xu.rondo.entity.Room;
import cn.xu.rondo.entity.User;
import cn.xu.rondo.enums.EE;
import cn.xu.rondo.response.exception.ApiException;
import cn.xu.rondo.service.IConfService;
import cn.xu.rondo.service.IKeywordsService;
import cn.xu.rondo.service.IRoomService;
import cn.xu.rondo.service.IUserService;
import cn.xu.rondo.service.impl.ConfServiceImpl;
import cn.xu.rondo.service.impl.KeywordsServiceImpl;
import cn.xu.rondo.service.impl.RoomServiceImpl;
import cn.xu.rondo.service.impl.UserServiceImpl;
import cn.xu.rondo.socket.IMSocket;
import cn.xu.rondo.utils.SpringUtils;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;

import java.util.List;

public class BaseController {

    public static IConfService confService = SpringUtils.getBean(ConfServiceImpl.class);

    public static IKeywordsService keywordsService = SpringUtils.getBean(KeywordsServiceImpl.class);

    public static IMSocket socket = SpringUtils.getBean(IMSocket.class);

    public static IRoomService roomService = SpringUtils.getBean(RoomServiceImpl.class);

    public static IUserService userService = SpringUtils.getBean(UserServiceImpl.class);

    // 获取配置值
    protected String get(String key) {
        QueryWrapper<Conf> wrap = new QueryWrapper<>();
        wrap.eq("conf_key", key);
        final Conf conf = confService.getOne(wrap);
        return conf.getConf_value();
    }

    // 抛出异常
    protected ApiException ERR(EE e) {
        return new ApiException(e);
    }

    // 向房间发送消息
    protected void sendMsg(Integer roomId, String msg) {
        socket.sendMsgToRoom(String.valueOf(roomId), msg);
    }

    // 广播消息
    protected void broadcast(String msg) {
        socket.broadcast(msg);
    }

    // 获取关键词列表
    protected List<Keywords> keywords() {
        QueryWrapper<Keywords> wrap = new QueryWrapper<>();
        wrap.eq("keywords_status", 0).orderByDesc("keywords_all");
        return keywordsService.list(wrap);
    }

    // 检查用户，房间，
    protected Room checkRoom(Integer roomId) {
        final Room room = roomService.getById(roomId);
        if (room == null) throw ERR(EE.ROOM_NOT_FOUND);
        return room;
    }

    protected User checkUser(Integer userId) {
        final User user = userService.getById(userId);
        if (user == null) throw ERR(EE.ACCOUNT_EMPTY);
        return user;
    }

    // 获取部分用户信息
    protected JSONObject userData(User user) {
        JSONObject data = new JSONObject();
        data.put("user_id", user.getUser_id());
        data.put("user_icon", user.getUser_icon());
        data.put("user_touchtip", user.getUser_touchtip());
        data.put("user_sex", user.getUser_sex());
        data.put("user_vip", user.getUser_vip());
        data.put("user_extra", user.getUser_extra());
        data.put("user_device", user.getUser_device());
        data.put("user_name", user.getUser_name());
        data.put("user_head", user.getUser_head());
        data.put("user_remark", user.getUser_remark());
        data.put("app_id", user.getUser_app());
        data.put("app_name", user.getUser_app());
        data.put("user_admin", user.isAdmin());
        return data;
    }


}


