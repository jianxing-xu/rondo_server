package cn.xu.rondo.task;


import cn.xu.rondo.entity.Room;
import cn.xu.rondo.entity.User;
import cn.xu.rondo.entity.vo.MsgVo;
import cn.xu.rondo.entity.vo.SearchVo;
import cn.xu.rondo.entity.vo.SongQueueVo;
import cn.xu.rondo.service.IRoomService;
import cn.xu.rondo.service.ISongService;
import cn.xu.rondo.service.IUserService;
import cn.xu.rondo.socket.IMSocket;
import cn.xu.rondo.socket.RoomThread;
import cn.xu.rondo.utils.*;
import com.alibaba.fastjson.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.yeauty.pojo.Session;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@EnableScheduling
@Configuration
public class SongTask {

    @Value("${server.servlet.context-path}")
    String contextPath;

    @Autowired
    public IRoomService roomService;

    @Autowired
    public RedisUtil redis;

    @Autowired
    public RedisTemplate redisTemplate;

    @Autowired
    public IUserService userService;

    @Autowired
    public ISongService songService;

    @Autowired
    public KwUtils kwUtils;

    public IMSocket imSocket = SpringUtils.getBean(IMSocket.class);

    private static ConcurrentHashMap<String, ConcurrentHashMap<String, Session>> chatmap = IMSocket.CHATMAP;

    private static final Logger log = LoggerFactory.getLogger(SongTask.class);

    // 记录当前房间数据，每5s从数据库同步一次
    public static Map<Integer, Room> rooms = new HashMap<>();

    @Scheduled(fixedDelay = 5000)
    void printTreads() {
        final Set<Thread> threads = Thread.getAllStackTraces().keySet();
        for (Thread thread : threads) {
            if (thread.getName().startsWith(Constants.RoomThreadPREFIX)) {
                log.warn("线程：" + thread.getName());
            }
        }
    }


    // 每隔5秒检查房间，如果有新房间来，就开启房间线程（在删除房间的时候需要停止房间线程）
    @Scheduled(fixedDelay = 5000)
    void updateRoomThread() {
        rooms.forEach((id, room) -> {
            final Thread existRoomThread = Common.getThreadByName(Constants.RoomThreadPREFIX + room.getRoom_id());
            if (existRoomThread == null) {
                final RoomThread roomThread = new RoomThread(id);
                roomThread.start();
            }
        });
    }

    // 每30s发送一次预加载url接口
    @Scheduled(fixedRate = 30000)
    private void preloadUrl() {
        // TODO: 预加载歌曲 下载歌曲
        for (String channel : chatmap.keySet()) {
            if (redisTemplate.opsForList().size(Constants.SongList + channel) >= 1) {
                // 拿到歌曲队列中的第一个对象
                SongQueueVo index = (SongQueueVo) redisTemplate.opsForList().index(Constants.SongList + channel, 0);
                if (index == null) continue;
                Long mid = index.getSong().getMid();
                String cachePlayUrl = redis.getCacheObject(Constants.SongPlayUrl + mid);

                if (cachePlayUrl == null) {
                    cachePlayUrl = kwUtils.getPlayUrl(mid);
                }
                if (cachePlayUrl != null && StringUtils.isNotEmpty(cachePlayUrl)) {
                    // 缓存播放地址1分钟，之后在 请求 /song/playUrl 的时候就可以拿到缓存，不用发请求了
                    redis.setCacheObject(Constants.SongPlayUrl + mid, cachePlayUrl);
                    redis.expire(Constants.SongPlayUrl + mid, 1, TimeUnit.MINUTES);
                }

                JSONObject data = new JSONObject();
                data.put("url", contextPath + "/song/playUrl/" + index.getSong().getMid());
                String message = new MsgVo(MsgVo.PRE_LOAD_URL, data).build();
                log.info("预加载了" + channel + "下一首url：" + contextPath + "/song/playUrl?mid=" + index.getSong().getMid());
                imSocket.sendMsgToRoom(channel, message);
            }
        }
    }

    // 每5s更新房间数据
    @Scheduled(fixedDelay = 5000)
    private void updateRoom() {
        final List<Room> allRooms = getAllRooms();
        allRooms.forEach(room -> rooms.put(room.getRoom_id(), room));
    }


    public SongQueueVo getSongByRobot() {
        SearchVo randomSong = kwUtils.getRandomSong();
        if (randomSong != null) {
            redis.setCacheObject(Constants.SongDetail + randomSong.getMid(), randomSong);
            redis.expire(Constants.SongDetail + randomSong.getMid(), 3600, TimeUnit.SECONDS);
            SongQueueVo songQueueVo = new SongQueueVo();
            songQueueVo.setSong(randomSong);
            User robot = userService.getById(1);
            songQueueVo.setUser(robot);
            return songQueueVo;
        }
        return null;
    }

    // 在队列里面没有歌曲的时候，机器人自动点一首歌加入到到队列中
    public void getSongByRobot(Integer roomId) {
        final List<SongQueueVo> queueVos = getSongList(roomId);
        SearchVo randomSong = null;
        if (queueVos.size() == 0) {
            try {
                randomSong = kwUtils.getRandomSong();
            } catch (Exception e) {
                log.error("加入队列：机器人点歌异常....");
            }
            if (randomSong != null) {
                redis.setCacheObject(Constants.SongDetail + randomSong.getMid(), randomSong);
                redis.expire(Constants.SongDetail + randomSong.getMid(), 3600, TimeUnit.SECONDS);
                SongQueueVo songQueueVo = new SongQueueVo();
                songQueueVo.setSong(randomSong);
                User robot = userService.getById(1);
                songQueueVo.setUser(robot);
                queueVos.add(songQueueVo);
                redis.setCacheListForDel(Constants.SongList + roomId, queueVos);
                redis.expire(Constants.SongList + roomId, 1, TimeUnit.DAYS);
            }
        }
    }

    // 获取所有房间
    public List<Room> getAllRooms() {
        List<Room> all_room = redis.getCacheList("all_room");
        if (all_room != null && all_room.size() != 0) {
            return all_room;
        }
        all_room = roomService.list();
        if (all_room == null) all_room = new ArrayList<>();
        redis.setCacheList("all_room", all_room);
        redis.expire("all_room", 5, TimeUnit.SECONDS);
        return all_room;
    }

    // song_now播放完毕后，从queue中弹出一首歌播放
    public SongQueueVo popSong(Integer roomId, boolean isRadio) {
        List<SongQueueVo> queue = redis.getCacheList(Constants.SongList + roomId);
        if (queue != null && queue.size() != 0) {
            SongQueueVo popSong = queue.remove(0);
            // 设置开始播放时间戳
            popSong.setSince(Common.time() + 5);
            if (!isRadio) {
                if (queue.size() == 0) {
                    redis.deleteObject(Constants.SongList + roomId);
                } else {
                    redis.setCacheListForDel(Constants.SongList + roomId, queue);
                    redis.expire(Constants.SongList + roomId, 86400);
                }
            }
            log.info(String.format("房间 %s 从列表中弹出了一首歌：%s", roomId, popSong.getSong().getName()));
            return popSong;
        }
        return null;
    }

    // 播放一首歌，不操作队列中的歌
    public void play(Integer roomId, SongQueueVo song) {
        if (song == null) {
            log.info("play方法 房间 " + roomId + " 接收到的是 null");
            return;
        }

        // 统一在播放方法中设置播放时间戳
        song.setSince(Common.time());
        redis.setCacheObject(Constants.SongNow + roomId, song);
        redis.expire(Constants.SongNow + roomId, song.getSong().getLength() + 10);

        redis.setCacheObject(Constants.SongDetail + song.getSong().getMid(), song.getSong());
        redis.expire(Constants.SongDetail + song.getSong().getMid(), 3600, TimeUnit.SECONDS);
        List<SongQueueVo> songList = getSongList(roomId);
        JSONObject data = new JSONObject();
        data.put("at", song.getAt());
        data.put("user", song.getUser());
        data.put("song", song.getSong());
        data.put("since", song.getSince());
        data.put("count", songList.size());
        String msg = new MsgVo(MsgVo.PLAY_SONG, data).build();
        imSocket.sendMsgToRoom(String.valueOf(roomId), msg);
    }

    // 获取播放中的歌
    public SongQueueVo getPlaying(Integer roomId) {
        return redis.getCacheObject(Constants.SongNow + roomId);
    }

    public List<SongQueueVo> getSongList(Integer roomId) {
        List<SongQueueVo> cacheList = redis.getCacheList(Constants.SongList + roomId);
        if (cacheList == null) cacheList = new ArrayList<>();
        return cacheList;
    }


}
