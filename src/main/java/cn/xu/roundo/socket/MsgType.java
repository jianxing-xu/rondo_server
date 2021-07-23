package cn.xu.roundo.socket;


import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum MsgType {
    ADD_SONG("addSong", "点歌{user,song}"),
    PLAY_SONG("playSong", "播放歌曲{user,song}"),
    SYSTEM("system", "系统消息"),
    TEXT("text", "文字消息"),
    IMG("img", "图片消息"),
    LINK("link", "链接消息"),
    ONLINE("online", "在线用户列表"),
    REMOVE_SONG("remove_song", "移除歌曲{user,song}"),
    REMOVE_BAN("remove_ban", "解禁用户"),
    SHUT_DOWN("shutdown", "禁言用户{user}"),
    SONG_DOWN("songdown", "禁止点歌{user}"),
    PASS("pass", "投票切歌{user}"),
    BACK("back", "消息撤回"),
    ROOM_UPDATE("room_update", "房间资料更新{room}");

    private String type;
    private String comment;

    public String t() {
        return type;
    }

    public String cm() {
        return comment;
    }
}
//    addSong	点歌	用户对象/歌曲对象
//        playSong	播放歌曲	用户对象/歌曲对象
//        push	置顶歌曲	用户对象/歌曲对象
//        system	系统消息	-
//        text	文字消息	用户对象/@对象/消息/key/sha
//        img	图片消息	用户对象/@对象/消息/key/sha
//        online	在线列表	用户对象
//        link	链接消息	-
//        removeSong	移除已点歌曲	-
//        removeban	解禁用户	-
//        shutdown	禁言	-
//        songdown	禁歌	-
//        pass	切歌	-
//        passGame	pass猜歌	-
//        back	撤回	-
//        roomUpdate	房间资料更新	-
//        game_music	猜歌消息下发	-
//        game_music_success	猜歌结果