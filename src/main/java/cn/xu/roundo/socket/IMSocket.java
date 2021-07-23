package cn.xu.roundo.socket;

import cn.xu.roundo.utils.JWTUtils;
import cn.xu.roundo.utils.RedisUtil;
import cn.xu.roundo.utils.SpringUtils;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import io.jsonwebtoken.Claims;
import org.springframework.data.redis.core.BoundListOperations;
import org.springframework.scheduling.annotation.Async;
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
public class IMSocket {

    public static ConcurrentHashMap<String, ConcurrentHashMap<String, Session>> CHATMAP = new ConcurrentHashMap<>();
    private RedisUtil redis = SpringUtils.getBean("RedisUtil");

    @OnOpen
    public void onOpen(Session session, @RequestParam("account") String account, @RequestParam("channel") String channel, @RequestParam("ticket") String ticket) throws Exception {
        Claims claims = JWTUtils.verifyJwt(ticket);
        String TAccount = String.valueOf(claims.get("account"));
        String TChannel = String.valueOf(claims.get("channel"));

        if (TAccount != null && TChannel != null && TAccount.equals(account) && TChannel.equals(channel)) {
            if (CHATMAP.get(channel) == null || CHATMAP.get(channel).isEmpty()) {
                CHATMAP.put(channel, new ConcurrentHashMap<>());
            }
            CHATMAP.get(channel).put(account, session);
            updateOnline(channel);
        }
    }

    @Async
    void getFirstSong(String channel, Session session) throws Exception {
        List<Object> list = redis.getCacheList("room_" + channel);
        if (list.size() != 0) {
            JSONObject song = JSON.parseObject(list.get(0).toString());
//            song.put("since", (SongTask.channelhome.get(channel) == null ? System.currentTimeMillis() + 100 : SongTask.channelhome.get(channel)) / 1000);
            song.put("type", "playSong");
            session.sendText(song.toJSONString());
        }
    }

    @Async
    void updateOnline(String channel) {
        JSONObject online = new JSONObject();
        online.put("type", MsgType.ONLINE.t());
        List list = new Vector();
        for (String x : CHATMAP.get(channel).keySet()) {
            try {
                list.add(Integer.parseInt(x));
            } catch (Exception e) {
                //do nothing
            }
        }
        online.put("data", list);
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

        }
    }

    // 向指定房间发送消息
    public static boolean sendMsgToRoom(String room_id, String message) {
        ConcurrentHashMap<String, Session> stringSessionConcurrentHashMap = IMSocket.CHATMAP.get(room_id);
        if (stringSessionConcurrentHashMap == null) return false;
        for (Session value : stringSessionConcurrentHashMap.values()) {
            value.sendText(message);
        }
        return true;
    }

    //向所有房间广播消息
    public static boolean broadcast(String message) {
        for (ConcurrentHashMap<String, Session> room : IMSocket.CHATMAP.values()) {
            if (room == null) return false;
            for (Session value : room.values()) {
                value.sendText(message);
            }
        }
        return true;
    }
}