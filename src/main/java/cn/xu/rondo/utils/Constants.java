package cn.xu.rondo.utils;

import cn.hutool.crypto.symmetric.SymmetricAlgorithm;
import cn.hutool.crypto.symmetric.SymmetricCrypto;

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
     * redis 用户最后一次摸的时间key_userId
     */
    public static String mo = "mo_";

    /**
     * 房间线程名前缀
     */
    public static String RoomThreadPREFIX = "room-thread-";

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
     * redis 禁言用户key
     */
    public static final String Shutdown = "shutdown_room_";

    /**
     * redis 禁止点歌key
     */
    public static final String SongDown = "song_down_room_";

    /**
     * redis 在线用户key_roomId
     */
    public static final String OnlineList = "online_list_";

    /**
     * redis 用户输入过的房间密码key
     */
    public static String SavedPwd(Integer roomId, Integer userId) {
        return String.format("password_room_%s_password_%s", roomId, userId);
    }

    /**
     * redis 房间聊天消息key_roomId
     */
    public static final String RoomMsgList = "room_message_list_";

    /**
     * redis禁止发言的IP地址列表key
     */
    public static String IPBanList = "ip_ban_list";

    /**
     * redis 最后一次发言记录key_userId
     */
    public static String LastSend = "last_send_";
    /**
     * redis 最后一次发言消息的内容 key_userId
     */
    public static String LastMsg = "last_msg_";


    /**
     * 临时token
     */
    public static final String tempToken = "1d40dc0b40000743d69ba671d8a418250f66422df9c61e332deeec7c15a2dade";


    public static String mailCode(String key) {
        return mailCodePre + key;
    }


    /**
     * token有效时间
     */
    public static final Integer tokenExpire = 1000 * 60 * 30;

    // hu tool 加密签名工具
    public static SymmetricCrypto aes = new SymmetricCrypto(SymmetricAlgorithm.AES, "xxuujyiuaannxyiu".getBytes());


    public static String[] touch_machine = {
            "你是谁，为什么要摸我！",
            "铁疙瘩摸着舒服吗？",
            "卧槽！你~~",
            "请不要对我毛手毛脚的！",
            "虽然你努力的样子很可爱，但是这里还是让我来吧！",
            "不要停下来！",
            "不要因为可爱就一直碰！",
            "你真的好残酷啊！",
            "太乱来的话我可要调教你了哦！",
            "啊对不起，我忘了你的脑瓜也不好使！",
            "今晚绝对不会然你睡着的！",
            "WOW~~",
            "我为什么要一直忍着，忍不了了！"
    };


}
