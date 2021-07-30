package cn.xu.rondo.controller;


import cn.hutool.core.codec.Base64;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.http.HtmlUtil;
import cn.hutool.http.HttpRequest;
import cn.xu.rondo.entity.Room;
import cn.xu.rondo.entity.Song;
import cn.xu.rondo.entity.User;
import cn.xu.rondo.entity.vo.*;
import cn.xu.rondo.enums.EE;
import cn.xu.rondo.response.Response;
import cn.xu.rondo.response.exception.ApiException;
import cn.xu.rondo.service.IRoomService;
import cn.xu.rondo.service.ISongService;
import cn.xu.rondo.service.IUserService;
import cn.xu.rondo.socket.IMSocket;
import cn.xu.rondo.utils.*;
import cn.xu.rondo.utils.params_resolver.UserId;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * <p>
 * 歌曲表 前端控制器
 * </p>
 *
 * @author jason xu
 * @since 2021-07-17
 */
@RestController
@RequestMapping("/song")
public class SongController extends BaseController {

    private static final Logger log = LoggerFactory.getLogger(SongController.class);

    @Autowired
    IUserService userService;
    @Autowired
    ISongService songService;
    @Autowired
    IRoomService roomService;
    @Autowired
    RedisUtil redis;
    @Autowired
    RedisTemplate redisTemplate;
    @Autowired
    KwUtils kwUtils;
    @Autowired
    IMSocket imSocket;

    @Value("${server.port}")
    String port;

    /**
     * 搜索歌曲
     *
     * @param isHot   是否搜索热门
     * @param page    页数
     * @param keyword 关键词
     */
    @GetMapping("/search")
    public List<SearchVo> search(@RequestParam("isHot") Boolean isHot,
                                 @RequestParam(value = "page", defaultValue = "1") Integer page,
                                 @RequestParam(value = "keyword", required = false) String keyword) {
        if (isHot != null && isHot) {
            List<SearchVo> hotList = redis.getCacheList(Constants.PopularWeek);
            if (hotList != null && hotList.size() != 0) {
                log.warn("热门歌曲 FROM Redis cache");
                return hotList;
            }
            hotList = songService.getWeekHot();
            if (hotList != null && hotList.size() != 0) {
                redis.setCacheList(Constants.PopularWeek, hotList);
                redis.expire(Constants.PopularWeek, 60, TimeUnit.SECONDS);
            }
            return hotList;
        }
        // 获取酷我的热搜词
        List<String> cacheList = kwUtils.getKwSearchKey();
        // 如果传递keyword为空的话，就从热搜词中选取一个
        if (keyword == null || StringUtils.isEmpty(keyword)) {
            keyword = cacheList.get(RandomUtil.randomInt(0, cacheList.size() - 1));
        }
        return kwUtils.getSearchResult(keyword, page);
    }

    /**
     * 删除用户已点歌单歌曲
     *
     * @param mid    歌曲mid
     * @param userId 用户id
     * @return 消息
     */
    @DeleteMapping("/del/{mid}")
    public String delMySong(@PathVariable("mid") @NotNull Integer mid,
                            @UserId Integer userId) {
        boolean flag = songService.removeByMap(new HashMap<String, Object>() {{
            put("song_mid", mid);
            put("song_user", userId);
        }});
        return flag ? "歌曲删除成功" : "歌曲删除失败";
    }

    /**
     * 收藏歌曲（就是添加到我的歌单，就是往song表中添加一条记录）
     *
     * @param mid    歌曲mid
     * @param roomId 房间id
     * @param userId 用户id
     * @return 消息
     */
    @PostMapping("/fav")
    public String favMySong(@RequestParam("mid") @NotNull Integer mid,
                            @RequestParam("room_id") @NotNull Integer roomId,
                            @UserId Integer userId) {
        QueryWrapper<Song> wrapper = new QueryWrapper<>();
        wrapper.eq("song_user", userId);
        wrapper.eq("song_mid", mid);
        Song song = songService.getOne(wrapper);
        if (song != null) throw new ApiException(EE.EXIST_FAV);
        SearchVo searchVo = redis.getCacheObject(Constants.SongDetail + mid);
        if (searchVo == null) throw new ApiException(EE.SONG_QUERY_ERR);

        Song newSong = new Song();
        newSong.setSong_mid(searchVo.getMid());
        newSong.setSong_name(searchVo.getName());
        newSong.setSong_singer(searchVo.getSinger());
        newSong.setSong_pic(searchVo.getPic());
        newSong.setSong_length(searchVo.getLength());
        newSong.setSong_user(userId);
        newSong.setSong_createtime((int) (System.currentTimeMillis() / 1000));
        newSong.setSong_updatetime((int) (System.currentTimeMillis() / 1000));
        songService.save(newSong);

        User user = userService.getById(userId);
        if (user == null) throw new ApiException(EE.INFO_QUERY_ERR);
        //TODO: sendWebsocketMsg 歌曲收藏成功系统消息通知 OK!
        JSONObject data = new JSONObject();
        data.put("content", user.getUser_name() + "收藏了当前歌曲");
        String msg = new MsgVo(MsgVo.SYSTEM, data).build();
        imSocket.sendMsgToRoom(String.valueOf(roomId), msg);
        return "收藏成功";
    }

    /**
     * 添加一首歌到song表中
     */
    @PostMapping("/addNewSong")
    public String addNewSong(@RequestParam("song_mid") Long songMid,
                             @RequestParam("song_name") String songName,
                             @RequestParam("song_singer") String songSinger,
                             @RequestParam("song_pic") String songPic,
                             @RequestParam("song_length") Integer songLength,
                             @UserId Integer userId) {
        Song song = new Song();
        song.setSong_mid(songMid);
        song.setSong_name(songName);
        song.setSong_singer(songSinger);
        song.setSong_pic(songPic);
        song.setSong_length(songLength);
        song.setSong_user(userId);
        song.setSong_createtime((int) (System.currentTimeMillis() / 1000));
        song.setSong_updatetime((int) (System.currentTimeMillis() / 1000));
        songService.save(song);
        return "添加成功！";
    }

    /**
     * 直接播放一首歌曲
     *
     * @param mid    歌曲mid
     * @param roomId 房间id
     * @param userId 用户id
     * @return 消息
     */
    @PostMapping("/play")
    public String play(@RequestParam("mid") @NotNull Long mid,
                       @RequestParam("room_id") @NotNull Integer roomId,
                       @UserId Integer userId) {
        Room room = roomService.getById(roomId);
        if (room == null) throw new ApiException(EE.ROOM_NOT_FOUND);
        if (room.getRoom_type() == 4) throw new ApiException(EE.BAN_PLAY);

        User user = userService.getById(userId);
        //判断用户是否有权限直接播放歌曲
        if (!user.isAdmin() && !room.getRoom_user().equals(user.getUser_id()) && !user.isVip())
            throw new ApiException(EE.PERMISSION_LOW);

        // 从 redis 中取到歌曲详情
        SearchVo detail = songService.getSongDetail(mid);
        if (detail == null) throw new ApiException(EE.SONG_QUERY_ERR);
        // 将歌曲置顶,歌曲队列中的 对象为 SongQueueVo
        boolean isPushed = false;
        List<SongQueueVo> queue = redis.getCacheList(Constants.SongList + roomId);
        if (queue == null) {
            queue = new ArrayList<>();
        }
        for (int i = 0; i < queue.size(); i++) {
            SongQueueVo queueItem = queue.get(i);
            if (queueItem.getSong().getMid().intValue() == mid) {
                SongQueueVo remove = queue.remove(i);
                queue.add(0, remove);
                isPushed = true;
                break;
            }
        }
        //TODO: 这里尝试同步一个歌曲的图片回来 cache 到redis中
        redis.setCacheObject(Constants.SongDetail + mid, detail);
        redis.expire(Constants.SongDetail + mid, 1, TimeUnit.HOURS);

        // 如果没有被置顶，就表示队列中没有这首歌
        if (!isPushed) {
            SongQueueVo songQueueVo = new SongQueueVo();
            songQueueVo.setSong(detail);
            songQueueVo.setUser(user);
            queue.add(0, songQueueVo);
        }


        // 重新设置 房间歌曲队列
        redis.setCacheListForDel(Constants.SongList + roomId, queue);
        redis.expire(Constants.SongList + roomId, 86400, TimeUnit.SECONDS);
        // 切掉正在播放的歌曲 将当前房间播放的歌曲置为null
        redis.setCacheObject(Constants.SongNow + roomId, null);

        //向数据库插入播放的歌曲
        Song existSong = songService.getOneByMap(new HashMap<String, Object>() {{
            put("song_mid", mid);
            put("song_user", userId);
        }});
        Song song = new Song();
        if (existSong == null) {
            song.setSong_mid(detail.getMid());
            song.setSong_name(detail.getName());
            song.setSong_pic(detail.getPic());
            song.setSong_length(detail.getLength());
            song.setSong_singer(detail.getSinger());
            song.setSong_createtime((int) (System.currentTimeMillis() / 1000));
            song.setSong_updatetime((int) (System.currentTimeMillis() / 1000));
            songService.save(song);
        } else {
            song.setSong_id(existSong.getSong_id());
            song.setSong_updatetime((int) (System.currentTimeMillis() / 1000));
            songService.updateById(song);
        }
        return "播放成功";
    }


    /**
     * 用户点歌
     *
     * @param mid    歌曲mid 页面上可能是rid
     * @param roomId 房间id
     * @param at     送给谁？
     * @param userId 点歌用户id
     * @return 响应对象
     */
    @PostMapping("/add")
    public Response<String> addSong(@RequestParam("mid") @NotNull Long mid,
                                    @RequestParam("room_id") @NotNull Integer roomId,
                                    @RequestParam(value = "at", required = false) Integer at,
                                    @UserId Integer userId) {
        Room room = roomService.getById(roomId);
        if (room == null) throw new ApiException(EE.ROOM_NOT_FOUND);
        if (room.getRoom_type() != 1 && room.getRoom_type() != 4) throw new ApiException(EE.BAN_PLAY);
        User user = userService.getById(userId);
        // 从 redis 中取到歌曲详情
        SearchVo detail = songService.getSongDetail(mid);
        if (detail == null) throw new ApiException(EE.INFO_QUERY_ERR);

        //TODO: 判断 detail[mid] 尝试同步图片
        redis.setCacheObject(Constants.SongDetail + mid, detail);
        redis.expire(Constants.SongDetail + mid, 1, TimeUnit.HOURS);
        // 判断你送给谁
        User atUser = null;
        if (at != null) {
            atUser = userService.getById(at);
            if (atUser != null) {
                if (userId.equals(atUser.getUser_id())) throw new ApiException(EE.AT_ME);
            } else {
                throw new ApiException(EE.AT_INFO_ERR);
            }
        }

        boolean isBan = Common.checkShutdown(1, roomId, userId);
        if (isBan) throw new ApiException(EE.BAN_USER_ADD_SONG);

        List<SongQueueVo> queue = redis.getCacheList(Constants.SongList + roomId);
        if (queue == null) queue = new ArrayList<>();

        //计算用户已经点了歌的数量和点的歌是否已在等待播放列表
        int myAddCount = 0;
        String existSongName = null;
        for (SongQueueVo songQueueVo : queue) {
            if (songQueueVo.getUser().getUser_id().equals(userId)) {
                myAddCount++;
            }
            if (songQueueVo.getSong().getMid().equals(mid)) {
                existSongName = songQueueVo.getSong().getName();
            }
        }
        if (existSongName != null) return new Response<>(1020, "歌曲 '" + HtmlUtil.escape(existSongName) + "'正在等待播放呢！");
        // 取点歌cd时间
        Integer cd = room.getRoom_addsongcd();

        //权限判断
        if (!user.isAdmin() && !user.isVip() && !userId.equals(room.getRoom_user())) {
            if (room.getRoom_addsong() == 1) throw new ApiException(EE.ONLY_ROOM_USER);
            Long lastTime = redis.getCacheObject(Constants.AddSongLastTime + userId);
            if (lastTime != null) {
                long needWaitTime = cd - (Common.time() - lastTime);
                if (needWaitTime > 0) return Response.errorMsg(String.format("点歌太过频繁，请等待%s秒后在点", needWaitTime));
            }
            if (myAddCount >= room.getRoom_addcount())
                return Response.errorMsg(String.format("你还有%s首歌没有播放，请稍后在点歌吧~", myAddCount));
        }
        //缓存点歌时间
        redis.setCacheObject(Constants.AddSongLastTime + userId, Common.time());
        redis.expire(Constants.AddSongLastTime + userId, cd);

        //TODO:如果待播放只有机器人点的歌了就删除 删除机器人点的歌 OK!
        if (queue.size() == 1 && queue.get(0).getUser().getUser_id() == 1) {
            queue.clear();
        }

        // 往房间队列中添加一条记录
        SongQueueVo songQueueVo = new SongQueueVo();
        songQueueVo.setSong(detail);
        songQueueVo.setUser(user);
        songQueueVo.setAt(atUser);
        queue.add(0, songQueueVo);

        redis.setCacheListForDel(Constants.SongList + roomId, queue);
        redis.expire(Constants.SongList + roomId, 1, TimeUnit.DAYS);


        // TODO: sendWebsocketMsg 发送点歌消息到房间 OK!
//        $msg = [
//        'user' => getUserData($this->user),
//                'song' => $song,
//                "type" => "addSong",
//                'at' => $at ?? false,
//                "time" => date('H:i:s'),
//                'count' => count($songList) ?? 0
//        ];
        JSONObject data = new JSONObject();
        data.put("user", user);
        data.put("song", detail);
        data.put("at", atUser);
        data.put("count", queue.size());
        String msg = new MsgVo(MsgVo.ADD_SONG, data).build();
        imSocket.sendMsgToRoom(String.valueOf(roomId), msg);
        //向数据库插入播放的歌曲
        Song existSong = songService.getOneByMap(new HashMap<String, Object>() {{
            put("song_mid", mid);
            put("song_user", userId);
        }});
        if (existSong == null) {
            existSong = new Song();
            existSong.setSong_mid(detail.getMid());
            existSong.setSong_name(detail.getName());
            existSong.setSong_pic(detail.getPic());
            existSong.setSong_length(detail.getLength());
            existSong.setSong_singer(detail.getSinger());
            existSong.setSong_play(1);
            existSong.setSong_week(1);
            existSong.setSong_user(userId);
            existSong.setSong_createtime(Common.time().intValue());
            existSong.setSong_updatetime(Common.time().intValue());
            songService.save(existSong);
        } else {
            existSong.setSong_id(existSong.getSong_id());
            existSong.setSong_week(existSong.getSong_week() + 1);
            existSong.setSong_play(existSong.getSong_play() + 1);
            existSong.setSong_updatetime(Common.time().intValue());
            songService.updateById(existSong);
        }

        return Response.success(String.format("歌曲%s已添加到播放列表", HtmlUtil.escape(existSong.getSong_name())));
    }


    /**
     * 查询我的歌单(传递userId就是获取其他用户歌单，不传递就是获取自己歌单，自己歌单需要pageNum,pageSize)
     *
     * @param pageNum  页数
     * @param pageSize 每页数量
     * @param userId   我的userId
     * @param otherId  其他userId
     * @return json数据
     */
    @GetMapping("/userSongs")
    public JSONObject mySongs(@RequestParam(value = "page_num", required = false, defaultValue = "1") Integer pageNum,
                              @RequestParam(value = "page_size", required = false, defaultValue = "20") Integer pageSize,
                              @UserId Integer userId,
                              @RequestParam(value = "user_id", required = false) Integer otherId) {
        Page<Song> page = new Page<>(otherId != null ? 1 : pageNum, otherId != null ? 50 : pageSize);
        QueryWrapper<Song> wrapper = new QueryWrapper<>();
        Integer id = otherId == null ? userId : otherId;
        wrapper.eq("song_user", id);
        wrapper.orderByDesc("song_updatetime", "song_play", "song_id");

        Page<Song> pager = songService.page(page, wrapper);
        List<Song> records = pager.getRecords();

        JSONObject json = new JSONObject();
        json.put("list", records);
        json.put("total", pager.getTotal());
        return json;
    }


    /**
     * 投票切歌
     *
     * @param mid    歌曲id
     * @param roomId 房间id
     * @return 成功消息
     */
    @GetMapping("/pass")
    public String pass(@RequestParam("mid") @NotNull Long mid,
                       @RequestParam("room_id") @NotNull Integer roomId,
                       @UserId Integer userId, HttpServletRequest request) {

        Room room = roomService.getById(roomId);
        User user = userService.getById(userId);
        if (room == null) throw new ApiException(EE.ROOM_NOT_FOUND);
        SearchVo songDetail = songService.getSongDetail(mid);
        if (songDetail == null) throw new ApiException(EE.SONG_QUERY_ERR);

        // 取正在播放的歌曲
        SongQueueVo now = redis.getCacheObject(Constants.SongNow + roomId);
        if (now == null) throw new ApiException(EE.NOT_NOW_PLAY);

        //既不是管理员，也不是vip，也不是房主，还不是自己点的歌 就得投票
        if (!user.isAdmin() && !user.isVip() && !userId.equals(room.getRoom_user()) && !userId.equals(now.getUser().getUser_id())) {
            if (!room.isOpenVotePass()) throw new ApiException(EE.NO_VOTE_PASS);
            // 获取在线人数
            Integer onlineCount = room.getRoom_online();
            int targetCount = room.getRoom_votepercent() * onlineCount / 100 + 1;
            if (targetCount < 2) targetCount = 2;
            if (targetCount > 20) targetCount = 20;

            // 获取缓存中的投票数量
            Integer passCount = redis.getCacheObject(Constants.SongNextCount(roomId, now.getSong().getMid()));
            if (passCount == null) passCount = 0;

            // 获取已经投过票的mid
            Long passMid = redis.getCacheObject(Constants.SongAlreadyPass(roomId, userId));
            if (now.getSong().getMid().equals(passMid)) {
                log.info("已有" + passCount + "人不想听," + room.getRoom_votepercent() + "%(" + targetCount + "人)不想听即可激动切歌~ ");
                return "已有" + passCount + "人不想听," + room.getRoom_votepercent() + "%(" + targetCount + "人)不想听即可激动切歌~ ";
            }

            redis.setCacheObject(Constants.SongAlreadyPass(roomId, userId), now.getSong().getMid());
            redis.expire(Constants.SongAlreadyPass(roomId, userId), 3600, TimeUnit.SECONDS);
            passCount++;
            //TODO: 发送pass系统消息到房间 OK!
            log.info("有人表示不太喜欢当前歌曲" + passCount + "/" + targetCount);
            JSONObject data = new JSONObject();
            data.put("content", String.format("有人表示不太喜欢当前播放的歌（%s/%s）", passCount, targetCount));
            String msg = new MsgVo(MsgVo.SYSTEM, data).build();
            imSocket.sendMsgToRoom(String.valueOf(roomId), msg);

            if (passCount >= targetCount) {
                redis.setCacheObject(Constants.SongNow + roomId, null);
                log.info("人数已经到达切歌需求，系统自动切歌");
                //TODO: 发送人数到达目标切歌系统消息 OK!
                JSONObject jsonData = new JSONObject();
                data.put("content", room.getRoom_votepercent() + "%的在在线用户（" + targetCount + "人）不想听这首歌，系统已自动切歌！");
                String imMsg = new MsgVo(MsgVo.SYSTEM, jsonData).build();
                imSocket.sendMsgToRoom(String.valueOf(roomId), imMsg);
            }
            redis.setCacheObject(Constants.SongNextCount(roomId, now.getSong().getMid()), passCount);
            redis.expire(Constants.SongNextCount(roomId, now.getSong().getMid()), 3600, TimeUnit.SECONDS);

            List<PassUserInfoVo> passList = redis.getCacheList(Constants.SongPassList + roomId);
            if (passList == null) passList = new ArrayList<>();
            PassUserInfoVo pass = new PassUserInfoVo();
            pass.setUser(user.getUser_id());
            pass.setName(user.getUser_name());
            pass.setIp(Common.getIpAddr());
            passList.add(0, pass);
            redis.setCacheListForDel(Constants.SongPassList + roomId, passList);
            redis.expire(Constants.SongPassList + roomId, 3600, TimeUnit.SECONDS);
            return "你的不想听态度表态成功！";
        }
        // 有权限的人直接切歌
        redis.setCacheObject(Constants.SongNow + roomId, null);
        //TODO: 发送切歌系统消息到房间 OK!
        JSONObject passData = new JSONObject();
        user.setUser_password(null);
        passData.put("user", user);
        passData.put("song", songDetail);
        String passMsg = new MsgVo(MsgVo.PASS, passData).build();
        imSocket.sendMsgToRoom(String.valueOf(roomId), passMsg);
        return "切歌成功";
    }

    /**
     * 获取房间等待播放队列
     *
     * @param roomId 房间id
     */
    @GetMapping("/queue")
    public List<SongQueueVo> queue(@RequestParam("room_id") Integer roomId) {
        List<SongQueueVo> queue = redis.getCacheList(Constants.SongList + roomId);
        if (queue == null) queue = new ArrayList<>();
        return queue;
    }

    /**
     * 获取歌曲歌词
     *
     * @param mid 歌曲id
     * @return 歌词数组
     */
    @GetMapping("/lrc/{mid}")
    public JSONArray getLrc(@PathVariable("mid") @NotNull Long mid) {

        List<LrcLine> lyricList = redis.getCacheList(Constants.SongLrcKey + mid);
        if (lyricList != null && lyricList.size() != 0) {
            return JSONArray.parseArray(JSON.toJSONString(lyricList));
        }
        String token = RandomUtil.randomNumbers(8);
        try {
            String res = HttpRequest.get("http://m.kuwo.cn/newh5/singles/songinfoandlrc?musicId=" + mid)
                    .header("csrf", token)
                    .cookie("kw_token=" + token)
                    .timeout(5000)
                    .execute()
                    .body();
            if (res != null) {
                JSONObject json = JSONObject.parseObject(res);
                if ("200".equals(String.valueOf(json.get("status")))) {
                    JSONArray jsonArray = json.getJSONObject("data").getJSONArray("lrclist");
                    redis.setCacheListForDel(Constants.SongLrcKey + mid, jsonArray);
                    redis.expire(Constants.SongLrcKey + mid, 3600, TimeUnit.SECONDS);
                    return jsonArray;
                } else {
                    throw new ApiException(EE.KW_QUERY_ERR);
                }
            }
        } catch (Exception e) {
            throw new ApiException(EE.KW_QUERY_ERR);
        }
        throw new ApiException(EE.KW_QUERY_ERR);
    }


    /**
     * 顶歌（将一首歌顶到队列前头）
     *
     * @param mid    歌曲mid
     * @param roomId 房间id
     * @return 成功消息
     */
    @PostMapping("/push")
    public Response<String> push(@RequestParam("mid") @NotNull Long mid,
                                 @RequestParam("room_id") @NotNull Integer roomId,
                                 @UserId Integer userId) {
        Room room = roomService.getById(roomId);
        User user = userService.getById(userId);
        if (room == null) throw new ApiException(EE.ROOM_NOT_FOUND);
        SearchVo songDetail = songService.getSongDetail(mid);
        if (songDetail == null) throw new ApiException(EE.SONG_QUERY_ERR);

        List<SongQueueVo> queue = redis.getCacheList(Constants.SongList + roomId);
        if (queue == null) queue = new ArrayList<>();

        // 记录需要顶的歌，和对应的index
        SongQueueVo pushSong = null;
        Integer pushIndex = null;
        for (int i = 0; i < queue.size(); i++) {
            SongQueueVo song = queue.get(i);
            //如果在队列中找到了这首歌
            if (song.getSong().getMid().equals(mid)) {
                // 管理者身份判断
                if (!user.isVip() && !user.isAdmin() && !userId.equals(room.getRoom_user())) {
                    // 顶自己的歌
                    if (userId.equals(song.getUser().getUser_id())) throw new ApiException(EE.PUSH_SELF_ERR);
                }
                // 是管理者 或 vip
                pushSong = song;
                pushIndex = i;
                break;
            }
        }
        if (pushSong == null) throw new ApiException(EE.SONG_QUERY_ERR);

        // 房间日顶歌限额
        Integer pushCount = room.getRoom_pushdaycount();
        if (pushCount < 0) throw new ApiException(EE.BANED_PUSH);
        // 用户顶歌次数
        Integer pushCache = redis.getCacheObject(Constants.UserPushCount(DateUtil.today(), userId));
        if (pushCache == null) pushCache = 0;
        Integer pushCd = room.getRoom_pushsongcd();

        if (!user.isVip() && !user.isAdmin() && !userId.equals(room.getRoom_user())) {
            Long pushLastTime = redis.getCacheObject(Constants.PushLastTime + userId);
            if (pushLastTime == null) pushLastTime = 0L;
            if (pushCache >= pushCount) throw new ApiException(EE.PUSH_OUT);
            // 判断 顶歌CD
            if (Common.time() - pushLastTime < pushCd) {
                int minus = (pushCd - (Common.time().intValue() - pushLastTime.intValue())) / 60;
                int seconds = (pushCd - (Common.time().intValue() - pushLastTime.intValue())) % 60;
                return Response.errorMsg(String.format("顶歌太频繁了，请%s分%s秒后重试", minus, seconds));
            }
            pushCache++;
            redis.setCacheObject(Constants.UserPushCount(DateUtil.today(), userId), pushCache);
            redis.expire(Constants.UserPushCount(DateUtil.today(), userId), 86400, TimeUnit.SECONDS);
        }

        // 将移除的歌顶到第一位,缓存会redis中
        queue.add(0, queue.remove(pushIndex.intValue()));
        redis.setCacheListForDel(Constants.SongList + roomId, queue);
        redis.expire(Constants.SongList + roomId, 1, TimeUnit.DAYS);
        // TODO: 发送顶歌系统消息到房间，提示对应次数 OK!
        JSONObject data = new JSONObject();
        user.setUser_password(null);
        data.put("user", user);
        data.put("song", songDetail);
        data.put("count", queue.size());
        String msg = new MsgVo(MsgVo.PUSH, data).build();
        imSocket.sendMsgToRoom(String.valueOf(roomId), msg);

        redis.setCacheObject(Constants.PushLastTime + userId, Common.time());
        redis.expire(Constants.PushLastTime + userId, room.getRoom_pushsongcd(), TimeUnit.SECONDS);
        if (!user.isVip() && !user.isAdmin() && !userId.equals(room.getRoom_user())) {
            return Response.success(String.format("顶歌成功，今日剩余%s次顶歌机会", pushCount - pushCache));
        }

        return Response.success("顶歌成功");
    }

    /**
     * 从队列里移除一首歌
     *
     * @param mid    歌曲mid
     * @param roomId 房间id
     * @return 成功消息
     */
    @PostMapping("/removeForQueue")
    public String remove(@RequestParam("mid") @NotNull Long mid,
                         @RequestParam("room_id") @NotNull Integer roomId,
                         @UserId Integer userId) {
        Room room = roomService.getById(roomId);
        User user = userService.getById(userId);
        if (room == null) throw new ApiException(EE.ROOM_NOT_FOUND);
        SearchVo songDetail = songService.getSongDetail(mid);
        if (songDetail == null) throw new ApiException(EE.SONG_QUERY_ERR);

        List<SongQueueVo> queue = redis.getCacheList(Constants.SongList + roomId);
        if (queue == null) queue = new ArrayList<>();
        SongQueueVo removeSong = null;
        for (int i = 0; i < queue.size(); i++) {
            SongQueueVo item = queue.get(i);
            if (item.getSong().getMid().equals(mid)) {
                removeSong = item;
                queue.remove(i);
                break;
            }
        }
        if (removeSong == null) throw new ApiException(EE.REMOVE_ERR);

        // 管理员，vip，自己点的不可以删
        if (!user.isAdmin() && !user.isVip() && !userId.equals(removeSong.getUser().getUser_id())) {
            throw new ApiException(EE.PERMISSION_LOW);
        }

        redis.setCacheListForDel(Constants.SongList + roomId, queue);
        redis.expire(Constants.SongList + roomId, 86400, TimeUnit.SECONDS);

        //TODO: 发送移除队列歌曲系统消息到房间 OK!
        JSONObject data = new JSONObject();
        user.setUser_password(null);
        data.put("user", user);
        data.put("song", removeSong.getSince());
        data.put("count", queue.size());
        String msg = new MsgVo(MsgVo.REMOVE_SONG, data).build();
        imSocket.sendMsgToRoom(String.valueOf(roomId), msg);
        return "删除成功";

    }


    /**
     * 获取歌曲的url
     *
     * @param mid 歌曲id
     * @return 歌曲URL
     */
    @GetMapping("/playUrl/{mid}")
    public void getPlayUrl(@PathVariable("mid") @NotNull Long mid,
                           HttpServletResponse response) throws IOException {
        String cacheUrl = redis.getCacheObject(Constants.SongPlayUrl + mid);
        if (cacheUrl != null && StringUtils.isNotEmpty(cacheUrl)) {
            response.sendRedirect(cacheUrl);
            return;
        }
        String url = kwUtils.getPlayUrl(mid);
        if (url == null) throw new ApiException(EE.KW_QUERY_ERR);
        //TODO: BBBUG 对获取的url加入了 wait_download_list 队列进行下载到本地处理，这里暂时不做处理
        //TODO: 自己上传歌曲判断
        redis.setCacheObject(Constants.SongPlayUrl + mid, url);
        redis.expire(Constants.SongPlayUrl + mid, 1, TimeUnit.MINUTES);
        response.sendRedirect(url);
    }

    /**
     * 获取房间歌曲队列
     *
     * @param roomId 房间id
     * @param userId 用户id
     * @return 房间歌曲队列
     */
    @GetMapping("/queueSongs/{room_id}")
    public List<SongQueueVo> getQueue(@PathVariable("room_id") @NotNull Integer roomId,
                                      @UserId Integer userId) {
        log.info("尝试获取临时用户ID：" + userId);
        List<SongQueueVo> queue = redis.getCacheList(Constants.SongList + roomId);
        if (queue == null) queue = new ArrayList<>();
        return queue;
    }
}


