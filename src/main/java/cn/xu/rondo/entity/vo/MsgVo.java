package cn.xu.rondo.entity.vo;


import com.alibaba.fastjson.JSONObject;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MsgVo {
    public static final String JOIN = "join"; // 加入房间
    public static final String PUSH = "push";
    private String type;
    private JSONObject data;

    public MsgVo(String t) {
        JSONObject j = new JSONObject();
        j.put("msg", "hello word!");
        this.type = t;
        this.data = j;
    }

    public MsgVo(String type, JSONObject data) {
        this.type = type;
        this.data = data;
    }

    public String build() {
        JSONObject json = new JSONObject();
        json.put("type", this.type);
        json.put("timestamp", System.currentTimeMillis());
        json.put("data", this.data);
        return json.toJSONString();
    }

    public static final String ADD_SONG = "addSong"; // , "点歌{user,song}"),

    public static final String PLAY_SONG = "playSong";//,"播放歌曲{user,song}"),

    public static final String SYSTEM = "system"; //,"系统消息"),

    public static final String StringTEXT = "text"; //"文字消息"),

    public static final String StringIMG = "img"; //,"图片消息"),

    public static final String LINK = "link"; // "链接消息"),

    public static final String ONLINE = "online"; //,"在线用户列表"),

    public static final String REMOVE_SONG = "remove_song"; //"移除歌曲{user,song}"),

    public static final String REMOVE_BAN = "remove_ban"; //,"解禁用户"),

    public static final String SHUT_DOWN = "shutdown"; //,"禁言用户{user}"),

    public static final String SONG_DOWN = "songdown"; //,"禁止点歌{user}"),

    public static final String PASS = "pass"; //,"切歌{user}"),

    public static final String BACK = "back"; //,"消息撤回"),

    public static final String ROOM_UPDATE = "room_update"; //,"房间资料更新{room}"),

    public static final String PRE_LOAD_URL = "preload"; //,"预加载url{url}");

    public static final String NOW = "now";  // 正在播放的歌曲
}
