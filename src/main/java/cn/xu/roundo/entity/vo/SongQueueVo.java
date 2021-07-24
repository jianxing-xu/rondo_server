package cn.xu.roundo.entity.vo;


import cn.xu.roundo.entity.User;
import lombok.Data;

/**
 * 每个房间的歌曲队列中的数据对象
 */
@Data
public class SongQueueVo {
    User user;
    SearchVo song;
    User at;

    // 歌开始播放的时间，用于记录歌曲的播放进度
    Long since;
}
