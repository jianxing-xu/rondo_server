package cn.xu.roundo.socket;

import cn.xu.roundo.entity.vo.MsgVo;
import cn.xu.roundo.task.SongTask;
import cn.xu.roundo.utils.JWTUtils;
import cn.xu.roundo.utils.RedisUtil;
import cn.xu.roundo.utils.SpringUtils;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import io.jsonwebtoken.Claims;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.BoundListOperations;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.yeauty.annotation.OnOpen;
import org.yeauty.annotation.*;
import org.yeauty.annotation.ServerEndpoint;
import org.yeauty.pojo.Session;

import java.util.List;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

@CrossOrigin
@ServerEndpoint(value = "/ws", port = "8081")
@Component
public class IMSocket {
    private static final Logger log = LoggerFactory.getLogger(IMSocket.class);
    public static ConcurrentHashMap<String, ConcurrentHashMap<String, Session>> CHATMAP = new ConcurrentHashMap<>();
    private RedisUtil redis = SpringUtils.getBean("RedisUtil");

    //
    @OnOpen
    public void onOpen(Session session,
                       @RequestParam("account") String account,
                       @RequestParam("channel") String channel,
                       @RequestParam("ticket") String ticket) {
        Claims claims = JWTUtils.verifyJwt(ticket);
        String TAccount = String.valueOf(claims.getId());
        String TChannel = String.valueOf(claims.getSubject());

        if (TAccount != null && TChannel != null && TAccount.equals(account) && TChannel.equals(channel)) {
            if (CHATMAP.get(channel) == null || CHATMAP.get(channel).isEmpty()) {
                CHATMAP.put(channel, new ConcurrentHashMap<>());
            }
            CHATMAP.get(channel).put(account, session);
            updateOnline(channel);
        }
    }

    // 更新在线人数
    @Async
    public void updateOnline(String channel) {
        JSONObject online = new JSONObject();
        online.put("type", MsgVo.ONLINE);
        List<Integer> list = new Vector<>();
        for (String x : CHATMAP.get(channel).keySet()) {
            try {
                list.add(Integer.parseInt(x));
            } catch (Exception e) {
                //do nothing
            }
        }
        online.put("data", list);
        log.info("在线:" + list);
        sendMsgToRoom(channel, online.toJSONString());
    }

    @OnMessage
    public void onMessage(Session session, String message, @RequestParam("account") String account, @RequestParam("channel") String channel, @RequestParam("ticket") String ticket) {
        if (message.equals("bye")) {
            onClose(session, account, channel);
        }
    }

    @OnClose
    public void onClose(Session session, @RequestParam("account") String account, @RequestParam("channel") String channel) {
        try {
            session.close();
            CHATMAP.get(channel).remove(account);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 向指定房间发送消息
    public boolean sendMsgToRoom(String room_id, String message) {
        ConcurrentHashMap<String, Session> stringSessionConcurrentHashMap = IMSocket.CHATMAP.get(room_id);
        if (stringSessionConcurrentHashMap == null) return false;
        for (Session value : stringSessionConcurrentHashMap.values()) {
            value.sendText(message);
        }
        return true;
    }

    //向所有房间广播消息
    public boolean broadcast(String message) {
        for (ConcurrentHashMap<String, Session> room : IMSocket.CHATMAP.values()) {
            if (room == null) return false;
            for (Session value : room.values()) {
                value.sendText(message);
            }
        }
        return true;
    }
}