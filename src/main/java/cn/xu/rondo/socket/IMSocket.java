package cn.xu.rondo.socket;

import cn.xu.rondo.entity.vo.MsgVo;
import cn.xu.rondo.entity.vo.SongQueueVo;
import cn.xu.rondo.utils.Constants;
import cn.xu.rondo.utils.JWTUtils;
import cn.xu.rondo.utils.RedisUtil;
import cn.xu.rondo.utils.SpringUtils;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.MalformedJwtException;
import io.netty.handler.codec.http.HttpHeaders;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

@CrossOrigin //跨域访问
@ServerEndpoint(path = "${rondo.ws.path}", port = "${rondo.ws.port}")// 标注此注解将此类作为socket服务连接类
@Component // 注入spring容器
public class IMSocket {
    private static final Logger log = LoggerFactory.getLogger(IMSocket.class);
    public static ConcurrentHashMap<String, ConcurrentHashMap<String, Session>> CHATMAP = new ConcurrentHashMap<>();
    private RedisUtil redis = SpringUtils.getBean("RedisUtil");


    @BeforeHandshake
    public void beforeHandshake(Session session, HttpHeaders headers,
                                @RequestParam("account") String account,
                                @RequestParam("channel") String channel,
                                @RequestParam("ticket") String ticket) {
        try {
            log.info(ticket);
            Claims claims = JWTUtils.verifyJwt(ticket);
            String TAccount = String.valueOf(claims.getId());
            String TChannel = String.valueOf(claims.getSubject());
            if (TAccount != null && TChannel != null && TAccount.equals(account) && TChannel.equals(channel)) {
                if (CHATMAP.get(channel) == null || CHATMAP.get(channel).isEmpty()) {
                    CHATMAP.put(channel, new ConcurrentHashMap<>());
                }
                session.setAttribute("channel", channel);
                session.setAttribute("account", account);
            } else {
                log.error("账号不匹配");
                onClose(session, account, channel);
            }
        } catch (MalformedJwtException e) {
            // 测试抛出异常 到 onError 中
            log.error("TICKET TOKEN 验证失败！CLOSE....");
            onClose(session, account, channel);
        } catch (Exception e) {
            e.printStackTrace();
            onClose(session, account, channel);
        }
    }

    //
    @OnOpen
    public void onOpen(Session session,
                       @RequestParam("account") String account,
                       @RequestParam("channel") String channel) {
        CHATMAP.get(channel).put(account, session);
        sendPlaySongEventNow(session, channel, MsgVo.NOW);
        updateOnline(channel);
    }

    @OnMessage
    public void onMessage(Session session, String message) {
        String account = session.getAttribute("account");
        String channel = session.getAttribute("channel");
        // 收到
        if (message.equals("bye")) {
            onClose(session, account, channel);
        }
        if (message.equals("pullPlaySong")) {
            sendPlaySongEventNow(session, channel, MsgVo.PLAY_SONG);
        }
    }

    @OnClose
    public void onClose(Session session,
                        @RequestParam("account") String account,
                        @RequestParam("channel") String channel) {
        try {
            session.close();
            ConcurrentHashMap<String, Session> room = CHATMAP.get(channel);
            if (room != null) {
                room.remove(account);
            }
            updateOnline(channel);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @OnError
    public void onError(Session session, Throwable throwable) {
        log.warn("WS连接抛出异常");
    }

    // 更新在线人数
    @Async
    public void updateOnline(String channel) {
        JSONObject online = new JSONObject();
        online.put("type", MsgVo.ONLINE);
        List<Integer> list = new Vector<>();
        if (CHATMAP.get(channel) != null) {
            for (String x : CHATMAP.get(channel).keySet()) {
                try {
                    list.add(Integer.parseInt(x));
                } catch (Exception e) {
                    log.error("不添加ip临时用户：" + x);
                }
            }
            online.put("data", list);
            log.info("房间" + channel + "在线:" + list);
            sendMsgToRoom(channel, online.toJSONString());
        }
    }

    // 向指定房间发送消息
    @Async
    public boolean sendMsgToRoom(String room_id, String message) {
        ConcurrentHashMap<String, Session> room = IMSocket.CHATMAP.get(room_id);

        // 每次发送消息增加在线人数数据
        JSONObject msgData = JSONObject.parseObject(message);
        final ConcurrentHashMap<String, Session> RoomUsers = CHATMAP.get(room_id);
        if (RoomUsers != null) {
            Integer onlineCount = RoomUsers.size();
            if (onlineCount != null) {
                msgData.put("online", onlineCount);
            }
        }
        if (room == null) return false;
        for (Session session : room.values()) {
            session.sendText(JSON.toJSONString(msgData));
        }
        return true;
    }

    //向所有房间广播消息
    @Async
    public boolean broadcast(String message) {
        for (ConcurrentHashMap<String, Session> room : IMSocket.CHATMAP.values()) {
            if (room == null) return false;
            for (Session value : room.values()) {
                value.sendText(message);
            }
        }
        return true;
    }

    @Async
    boolean sendToOne(Session session, String msg) {
        session.sendText(msg);
        return true;
    }

    @Async
    public void sendPlaySongEventNow(Session session, String channel, String type) {
        SongQueueVo nowSong = redis.getCacheObject(Constants.SongNow + channel);
        if (nowSong != null) {
            JSONObject data = new JSONObject();
            data.put("song", nowSong.getSong());
            data.put("since", nowSong.getSince());
            data.put("user", nowSong.getUser());
            data.put("at", nowSong.getAt());
            String msg = new MsgVo(type, data).build();
            // 向刚刚连接成功的用户发送播放消息
            sendToOne(session, msg);
        }
    }
}