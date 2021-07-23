package cn.xu.roundo.utils;

public class Constants {
    /**
     * redis 邮件前缀key
     */
    public static final String mailCodePre = "mail_code_";
    /**
     * redis 热门房间key
     */
    public static final String roomList = "room_list";
    /**
     * redis 用户ip地域key
     */
    public static final String ipAddress = "ip_address_";
    /**
     * redis 周热门歌曲key
     */
    public static final String PopularWeek = "popular_week";
    /**
     * redis kw热搜词key
     */
    public static final String KwHotKey = "kw_hot_key";
    /**
     * redis 搜索词+key-搜索结果缓存
     */
    public static final String SearchHistoryResult = "search_key_";
    /**
     * redis 歌曲详情key
     */
    public static final String SongDetail = "song_detail_";
    /**
     * redis 房间歌曲队列key_
     */
    public static final String SongList = "song_list_";
    /**
     * redis 正在播放的歌曲 key_roomid
     */
    public static final String SongNow = "song_now_";

    /**
     * redis对房间用户禁止点歌 key
     */
    public static String SongDown(Integer roomId, Integer userId) {
        return String.format("songdown_room_%s_user_%s", roomId, userId);
    }

    /**
     * redis 点歌cd的key
     */
    public static String AddSongLastTime = "add_song_last_time_";

    /**
     * redis 当前歌曲的投票跳过数量key
     */
    public static String SongNextCount(Integer roomId, Long mid) {
        return String.format("song_next_count_%s_mid_%s", roomId, mid);
    }

    /**
     * redis 用户已投票缓存key
     */
    public static String SongAlreadyPass(Integer roomId, Integer userId) {
        return String.format("song_already_pass_room_%s_user_%s", roomId, userId);
    }

    /**
     * redis 歌曲歌词key
     */
    public static String SongLrcKey = "song_lrc_";

    /**
     * redis 记录投票用户的信息 key
     */
    public static final String SongPassList = "song_pass_list_";

    /**
     * redis 记录顶歌次数
     */
    public static String UserPushCount(String date, Integer userId) {
        return String.format("user_push_%s_count_%s", date, userId);
    }

    /**
     * redis 用户顶歌cd
     */
    public static final String PushLastTime = "push_last_";

    /**
     * redis 歌曲临时url
     */
    public static final String SongPlayUrl = "song_play_temp_url_new_";

    /**
     * 临时token
     */
    public static final String tempToken = "1d40dc0b40000743d69ba671d8a418250f66422df9c61e332deeec7c15a2dade";


    /**
     * token有效时间
     */
    public static final Integer tokenExpire = 1000 * 60 * 30;


    public static String mailCode(String key) {
        return mailCodePre + key;
    }
}
