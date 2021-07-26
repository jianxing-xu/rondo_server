package cn.xu.rondo.entity;

import cn.hutool.http.HtmlUtil;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.io.Serializable;

/**
 * <p>
 * 歌曲表
 * </p>
 *
 * @author jason xu
 * @since 2021-07-17
 */
@TableName("sa_song")
public class Song implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "song_id", type = IdType.AUTO)
    private Integer song_id;

    private Integer song_user;

    private Long song_mid;

    /**
     * 歌曲名称
     */
    private String song_name;

    /**
     * 歌手
     */
    private String song_singer;

    private String song_pic;

    private Integer song_length;

    /**
     * 被点次数
     */
    private Integer song_play;

    /**
     * 本周被点次数
     */
    private Integer song_week;

    /**
     * 0点歌 1收藏
     */
    private Integer song_fav;

    /**
     * 状态
     */
    private Integer song_status;

    /**
     * 创建时间
     */
    private Integer song_createtime;

    /**
     * 修改时间
     */
    private Integer song_updatetime;


    public Integer getSong_id() {
        return song_id;
    }

    public void setSong_id(Integer song_id) {
        this.song_id = song_id;
    }

    public Integer getSong_user() {
        return song_user;
    }

    public void setSong_user(Integer song_user) {
        this.song_user = song_user;
    }

    public Long getSong_mid() {
        return song_mid;
    }

    public void setSong_mid(Long song_mid) {
        this.song_mid = song_mid;
    }

    public String getSong_name() {
        return song_name;
    }

    public void setSong_name(String song_name) {
        this.song_name = HtmlUtil.escape(song_name);
    }

    public String getSong_singer() {
        return song_singer;
    }

    public void setSong_singer(String song_singer) {
        this.song_singer = song_singer;
        this.song_singer = HtmlUtil.escape(song_singer);
    }

    public String getSong_pic() {
        return song_pic;
    }

    public void setSong_pic(String song_pic) {
        this.song_pic = song_pic;
    }

    public Integer getSong_length() {
        return song_length;
    }

    public void setSong_length(Integer song_length) {
        this.song_length = song_length;
    }

    public Integer getSong_play() {
        return song_play;
    }

    public void setSong_play(Integer song_play) {
        this.song_play = song_play;
    }

    public Integer getSong_week() {
        return song_week;
    }

    public void setSong_week(Integer song_week) {
        this.song_week = song_week;
    }

    public Integer getSong_fav() {
        return song_fav;
    }

    public void setSong_fav(Integer song_fav) {
        this.song_fav = song_fav;
    }

    public Integer getSong_status() {
        return song_status;
    }

    public void setSong_status(Integer song_status) {
        this.song_status = song_status;
    }

    public Integer getSong_createtime() {
        return song_createtime;
    }

    public void setSong_createtime(Integer song_createtime) {
        this.song_createtime = song_createtime;
    }

    public Integer getSong_updatetime() {
        return song_updatetime;
    }

    public void setSong_updatetime(Integer song_updatetime) {
        this.song_updatetime = song_updatetime;
    }

    @Override
    public String toString() {
        return "Song{" +
        "song_id=" + song_id +
        ", song_user=" + song_user +
        ", song_mid=" + song_mid +
        ", song_name=" + song_name +
        ", song_singer=" + song_singer +
        ", song_pic=" + song_pic +
        ", song_length=" + song_length +
        ", song_play=" + song_play +
        ", song_week=" + song_week +
        ", song_fav=" + song_fav +
        ", song_status=" + song_status +
        ", song_createtime=" + song_createtime +
        ", song_updatetime=" + song_updatetime +
        "}";
    }
}
