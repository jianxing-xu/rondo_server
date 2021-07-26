package cn.xu.rondo.task;


import cn.hutool.http.HtmlUtil;
import cn.xu.rondo.entity.Room;
import cn.xu.rondo.entity.User;
import cn.xu.rondo.entity.vo.MsgVo;
import cn.xu.rondo.entity.vo.SearchVo;
import cn.xu.rondo.entity.vo.SongQueueVo;
import cn.xu.rondo.service.IRoomService;
import cn.xu.rondo.service.ISongService;
import cn.xu.rondo.service.IUserService;
import cn.xu.rondo.socket.IMSocket;
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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@EnableScheduling
@Configuration
public class SongTask {

    @Value("server.servlet.context-path")
    String contextPath;

    @Autowired
    IRoomService roomService;

    @Autowired
    RedisUtil redis;

    @Autowired
    RedisTemplate redisTemplate;

    @Autowired
    IUserService userService;

    @Autowired
    ISongService songService;

    @Autowired
    KwUtils kwUtils;

    IMSocket imSocket = SpringUtils.getBean(IMSocket.class);

    private static ConcurrentHashMap<String, ConcurrentHashMap<String, Session>> chatmap = IMSocket.CHATMAP;

    private static final Logger log = LoggerFactory.getLogger(SongTask.class);

    @Scheduled(fixedDelay = 2000)
    void execute() {
        List<Room> rooms = getAllRooms();
        if (rooms == null) {
            log.warn("暂无房间");
            return;
        }
        for (Room room : rooms) {
            log.info("----------------------------------------------------");
            try {
                SongQueueVo songQueueVo = getPlaying(room.getRoom_id());
                // 判断是否有正在播放的歌曲
                if (songQueueVo != null && songQueueVo.getSong() != null) {
                    // 当前时间戳小于 歌曲的开始播放时间 + 歌曲的长度表示歌曲还没有播放完，正在播放中
                    if (Common.time() < songQueueVo.getSong().getLength() + songQueueVo.getSince()) {
                        log.info(String.format("房间：%s 正在播放 %s 中,已经播放了%s秒了", room.getRoom_name(), HtmlUtil.escape(songQueueVo.getSong().getName()), Common.time() - songQueueVo.getSince()));
                        continue;
                    }

                    // 执行到这里表示歌曲已经播放完了,如果是单曲循环
                    log.info(String.format("歌曲 %s 已经播放完了", songQueueVo.getSong().getName()));
                    //必须 开启电台模式 单曲循环才有效
                    if (room.isSingleCycle() && room.isRadioStation()) {
                        // 重置当前点歌时间为当前时间戳
                        log.info(String.format("房间 %s 开始单曲循环", room.getRoom_name()));
                        songQueueVo.setSince(Common.time());
                        play(room.getRoom_id(), songQueueVo);
                        continue;
                    }
                }
                // 执行到这里就是，当前房间没有正在播放的歌曲-----------------NO_PLAYING---------------
                // 从队列中弹出一首歌播放
                songQueueVo = popSong(room.getRoom_id(), room.isRadioStation());
                if (songQueueVo != null) {
                    play(room.getRoom_id(), songQueueVo);
                    log.info("开始播放 刚刚弹出的歌：" + songQueueVo.getSong().getName());
                    continue;
                }
                log.info("房间 " + room.getRoom_name() + " 队列里没有歌曲拉~");
                // 如果是电台房间，就从用户已点歌曲中随机取一首歌播放
                if (room.isRadioStation()) {
                    songQueueVo = songService.getRandSongByUser(room.getRoom_user());
                    log.info(String.format("房间 %s 开启了电台模式", room.getRoom_name()));
                    // 如果电台中没有歌曲 就播放不了了
                    if (songQueueVo == null) {
                        log.info("房间 " + room.getRoom_name() + " 的电台也没有歌曲！没有歌曲在播放！！");
                        continue;
                    }
                    log.info(String.format("房间 %s 从电台中拿到了歌曲 %s,并开始播放", room.getRoom_name(), songQueueVo.getSong().getName()));
                    play(room.getRoom_id(), songQueueVo);
                } else {
                    // 如果不是电台模式，需要判断是否开启机器人点歌
                    log.info(String.format("房间 %s 不是电台模式", room.getRoom_name()));
                    if (room.isRobot()) {
                        log.info(String.format("房间 %s 开启了机器人点歌模式", room.getRoom_name()));
                        SongQueueVo song = getSongByRobot();
                        if (song == null) {
                            log.info("房间 " + room.getRoom_name() + " 的机器人没点着歌");
                            continue;
                        }
                        log.info(String.format("机器人点着歌了：%s", song.getSong().getName()));
                        play(room.getRoom_id(), song);
                    } else {
                        log.info("也没有机器人点歌 没在");
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                redis.setCacheObject(Constants.SongNow + room.getRoom_id(), null);
            }
        }
    }

    // 每30s发送一次预加载url接口
    @Scheduled(fixedRate = 20000)
    private void preloadUrl() {
        //redisTemplate.boundSetOps("history").persist();
        //redisTemplate.boundHashOps("shutdown").persist();
        for (String channel : chatmap.keySet()) {
            if (redisTemplate.opsForList().size(Constants.SongList + channel) >= 1) {
                // 拿到歌曲队列中的第一个对象
                SongQueueVo index = (SongQueueVo) redisTemplate.opsForList().index(Constants.SongList + channel, 1);
                if (index == null) continue;
                JSONObject data = new JSONObject();
                data.put("url", contextPath + "/song/playUrl?mid=" + index.getSong().getMid());
                String message = new MsgVo(MsgVo.PRE_LOAD_URL, data).build();
                log.info("预加载了" + channel + "下一首url：" + contextPath + "/song/playUrl?mid=" + index.getSong().getMid());
                imSocket.sendMsgToRoom(channel, message);
            }
        }
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
        redis.expire(Constants.SongDetail + song.getSong().getMid(), 3600,TimeUnit.SECONDS);
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
