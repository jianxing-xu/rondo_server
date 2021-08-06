package cn.xu.rondo.enums;

import lombok.Getter;

@Getter
public enum EE {
    SUCCESS(1000, "成功"),
    FAILED(1001, "响应失败"),
    VALIDATE_FAILED(1002, "参数校验失败"),
    ERROR(5000, "未知错误"),
    FORBID(1003, "禁止访问"),
    ACCOUNT_ERR(1002, "账号或密码错误"),
    ACCOUNT_EMPTY(1004, "用户不存在"),
    ROOM_EXIST(1005, "房间已存在"),
    ROOM_NOT_FOUND(1006, "房间不存在"),
    ROOM_BANED(1007, "房间已被封禁"),
    VISITOR_BAN(1008, "游客禁止访问"),
    ROOM_PSD_ERR(1009, "房间密码错误"),
    INFO_QUERY_ERR(1010, "信息查询失败"),
    PSD_VALID_ERR(1011, "密码长度应该为4-16位"),
    KW_QUERY_ERR(1012, "酷我接口查询失败"),
    EXIST_FAV(1013, "已经收藏过了"),
    SONG_QUERY_ERR(1014, "歌曲信息查询失败"),
    BAN_PLAY(1015, "房间禁止播放"),
    PERMISSION_LOW(1016, "你还没有权限做这个事"),
    AT_ME(1017, "“自己给自己送歌，属实不高端”——佚名"),
    AT_INFO_ERR(1018, "你要送的人找不到拉！"),
    BAN_USER_ADD_SONG(1019, "你被禁止点歌了"),
    //    QUEUE_EXIST_SONG(1020,"你点的歌曲");
    ONLY_ROOM_USER(1021, "该房间仅房主可点歌"),
    WAIT_ADD_SONG_CD(1022, "点歌太过频繁"),
    NOT_NOW_PLAY(1023, "没有正在播放的歌曲"),
    NO_VOTE_PASS(1024, "该房间未开启投票跳过"),
    PUSH_SELF_ERR(1025, "不能顶自己的歌呦~"),
    BANED_PUSH(1026, "当前房间设置了不允许顶歌"),
    PUSH_OUT(1027, "今日顶歌次数已经用完拉~"),
    REMOVE_ERR(1028, "移除失败，歌曲ID不存在"),
    PWD_ERROR(1028, "密码错误"),
    MAIL_CODE_ERR(1029, "验证码错误"),
    WHAT_FUCK(1030, "你想干嘛？"),
    FILE_EMPTY(1031, "文件为空！"),
    FILE_UPLOAD_ERR(1032, "文件上传失败"),
    AVA_MAX_1M(1033, "头像最大1M"),
    FILE_TYPE_ERR(1034, "文件类型错误"),
    MKDIR_ERR(1035, "创建文件夹失败"),
    BACK_ERR(1036, "撤回失败"),
    BACK_ONLY_5(1037, "你只能撤回5分钟内的消息"),
    NEED_PWD(1038, "需要重新输入密码"),
    POP_INPUT_PWD(1039, "请输入房间密码"),
    ROOM_PWD_ERROR(1139, "房间密码错误"),
    ROOM_SECURE(1040, "加密房间"),
    ALL_MUTE(1041, "全员禁言中"),
    MSG_EMPTY(1042, "消息不能为空"),
    MUTE(1043, "你被房主禁言了"),
    IP_MUTE(1044, "您所在ip地址被禁止发言"),
    OFTEN_MSG(1045, "消息发送的太快了！"),
    SAME_CONTENT(1046, "灌水可耻！请不要频繁发送相同的内容"),
    NOT_SUP_SEND_PIC(1047, "禁止在18:00~9:00发送自定义上传图片"),
    NOT_MSG_TYPE(1048, "未知消息类型"),
    MO_ME(1049, "我摸我自己，哎，就是玩儿！"),
    OFTEN_MO(1050, "摸的太快啦，稍后在摸！"),
    MUSIC_MAX_20(1051, "上传歌大小最大20MB"),
    MAIL_FORMAT_ERR(1052, "邮箱格式错误"),
    OFTEN_MAIL(1053, "发送邮箱过频繁1分钟后重试"),
    LRC_ERR(1054, "歌词获取失败");

    private Integer code;
    private String msg;

    EE(int code, String msg, String... args) {
        this.code = code;
        this.msg = msg;
    }
}