package cn.xu.rondo.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.io.Serializable;

/**
 * <p>
 * 房间表
 * </p>
 *
 * @author jason xu
 * @since 2021-07-17
 */
@TableName("sa_room")
public class Room implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "room_id", type = IdType.AUTO)
    private Integer room_id;

    /**
     * 所有者ID
     */
    private Integer room_user;

    /**
     * 点歌CD
     */
    private Integer room_addsongcd;

    /**
     * 点歌数量
     */
    private Integer room_addcount;

    /**
     * 顶歌日限额
     */
    private Integer room_pushdaycount;

    /**
     * 顶歌CD
     */
    private Integer room_pushsongcd;

    /**
     * 已登录在线
     */
    private Integer room_online;

    /**
     * 所有在线
     */
    private Integer room_realonline;

    /**
     * 是否从列表隐藏
     */
    private Integer room_hide;

    /**
     * 房间名称
     */
    private String room_name;

    /**
     * 房间类型
     */
    private Integer room_type;

    private Integer room_public;

    /**
     * 房间密码
     */
    @JsonIgnore //采用jackson序列化返回时忽略此字段
    private String room_password;

    /**
     * 进入房间提醒
     */
    private String room_notice;

    private Integer room_addsong;

    private Integer room_sendmsg;

    private Integer room_robot;

    private Integer room_order;

    private String room_reason;

    /**
     * 0随机1单曲
     */
    private Integer room_playone;

    private Integer room_votepass;

    private Integer room_votepercent;

    /**
     * 房间背景图
     */
    private String room_background;

    /**
     * 插件地址
     */
    private String room_app;

    /**
     * 插件是否全屏
     */
    private Integer room_fullpage;

    /**
     * 状态
     */
    private Integer room_status;

    /**
     * 创建时间
     */
    private Integer room_createtime;

    /**
     * 修改时间
     */
    private Integer room_updatetime;

    // 是否开启投票
    public boolean isOpenVotePass() {
        return room_votepass == 1;
    }

    // 是否单曲循环
    public boolean isSingleCycle() {
        return room_playone == 1;
    }

    // 是否为电台模式
    public boolean isRadioStation() {
        return room_type == 4;
    }

    // 是否开启机器人点歌
    public boolean isRobot() {
        return room_robot == 1;
    }


    public Integer getRoom_id() {
        return room_id;
    }

    public void setRoom_id(Integer room_id) {
        this.room_id = room_id;
    }

    public Integer getRoom_user() {
        return room_user;
    }

    public void setRoom_user(Integer room_user) {
        this.room_user = room_user;
    }

    public Integer getRoom_addsongcd() {
        return room_addsongcd;
    }

    public void setRoom_addsongcd(Integer room_addsongcd) {
        this.room_addsongcd = room_addsongcd;
    }

    public Integer getRoom_addcount() {
        return room_addcount;
    }

    public void setRoom_addcount(Integer room_addcount) {
        this.room_addcount = room_addcount;
    }

    public Integer getRoom_pushdaycount() {
        return room_pushdaycount;
    }

    public void setRoom_pushdaycount(Integer room_pushdaycount) {
        this.room_pushdaycount = room_pushdaycount;
    }

    public Integer getRoom_pushsongcd() {
        return room_pushsongcd;
    }

    public void setRoom_pushsongcd(Integer room_pushsongcd) {
        this.room_pushsongcd = room_pushsongcd;
    }

    public Integer getRoom_online() {
        return room_online;
    }

    public void setRoom_online(Integer room_online) {
        this.room_online = room_online;
    }

    public Integer getRoom_realonline() {
        return room_realonline;
    }

    public void setRoom_realonline(Integer room_realonline) {
        this.room_realonline = room_realonline;
    }

    public Integer getRoom_hide() {
        return room_hide;
    }

    public void setRoom_hide(Integer room_hide) {
        this.room_hide = room_hide;
    }

    public String getRoom_name() {
        return room_name;
    }

    public void setRoom_name(String room_name) {
        this.room_name = room_name;
    }

    public Integer getRoom_type() {
        return room_type;
    }

    public void setRoom_type(Integer room_type) {
        this.room_type = room_type;
    }

    public Integer getRoom_public() {
        return room_public;
    }

    public void setRoom_public(Integer room_public) {
        this.room_public = room_public;
    }

    public String getRoom_password() {
        return room_password;
    }

    public void setRoom_password(String room_password) {
        this.room_password = room_password;
    }

    public String getRoom_notice() {
        return room_notice;
    }

    public void setRoom_notice(String room_notice) {
        this.room_notice = room_notice;
    }

    public Integer getRoom_addsong() {
        return room_addsong;
    }

    public void setRoom_addsong(Integer room_addsong) {
        this.room_addsong = room_addsong;
    }

    public Integer getRoom_sendmsg() {
        return room_sendmsg;
    }

    public void setRoom_sendmsg(Integer room_sendmsg) {
        this.room_sendmsg = room_sendmsg;
    }

    public Integer getRoom_robot() {
        return room_robot;
    }

    public void setRoom_robot(Integer room_robot) {
        this.room_robot = room_robot;
    }

    public Integer getRoom_order() {
        return room_order;
    }

    public void setRoom_order(Integer room_order) {
        this.room_order = room_order;
    }

    public String getRoom_reason() {
        return room_reason;
    }

    public void setRoom_reason(String room_reason) {
        this.room_reason = room_reason;
    }

    public Integer getRoom_playone() {
        return room_playone;
    }

    public void setRoom_playone(Integer room_playone) {
        this.room_playone = room_playone;
    }

    public Integer getRoom_votepass() {
        return room_votepass;
    }

    public void setRoom_votepass(Integer room_votepass) {
        this.room_votepass = room_votepass;
    }

    public Integer getRoom_votepercent() {
        return room_votepercent;
    }

    public void setRoom_votepercent(Integer room_votepercent) {
        this.room_votepercent = room_votepercent;
    }

    public String getRoom_background() {
        return room_background;
    }

    public void setRoom_background(String room_background) {
        this.room_background = room_background;
    }

    public String getRoom_app() {
        return room_app;
    }

    public void setRoom_app(String room_app) {
        this.room_app = room_app;
    }

    public Integer getRoom_fullpage() {
        return room_fullpage;
    }

    public void setRoom_fullpage(Integer room_fullpage) {
        this.room_fullpage = room_fullpage;
    }

    public Integer getRoom_status() {
        return room_status;
    }

    public void setRoom_status(Integer room_status) {
        this.room_status = room_status;
    }

    public Integer getRoom_createtime() {
        return room_createtime;
    }

    public void setRoom_createtime(Integer room_createtime) {
        this.room_createtime = room_createtime;
    }

    public Integer getRoom_updatetime() {
        return room_updatetime;
    }

    public void setRoom_updatetime(Integer room_updatetime) {
        this.room_updatetime = room_updatetime;
    }

    // 是否公开
    public boolean isPublic() {
        return room_public == 0;
    }

    // 是否房主
    public boolean isOwner(Integer userId) {
        return this.room_user.equals(userId);
    }

    // 是否全员禁言
    public boolean isBanAll() {
        return this.room_sendmsg == 1;
    }

    @Override
    public String toString() {
        return "Room{" +
                "room_id=" + room_id +
                ", room_user=" + room_user +
                ", room_addsongcd=" + room_addsongcd +
                ", room_addcount=" + room_addcount +
                ", room_pushdaycount=" + room_pushdaycount +
                ", room_pushsongcd=" + room_pushsongcd +
                ", room_online=" + room_online +
                ", room_realonline=" + room_realonline +
                ", room_hide=" + room_hide +
                ", room_name=" + room_name +
                ", room_type=" + room_type +
                ", room_public=" + room_public +
                ", room_password=" + room_password +
                ", room_notice=" + room_notice +
                ", room_addsong=" + room_addsong +
                ", room_sendmsg=" + room_sendmsg +
                ", room_robot=" + room_robot +
                ", room_order=" + room_order +
                ", room_reason=" + room_reason +
                ", room_playone=" + room_playone +
                ", room_votepass=" + room_votepass +
                ", room_votepercent=" + room_votepercent +
                ", room_background=" + room_background +
                ", room_app=" + room_app +
                ", room_fullpage=" + room_fullpage +
                ", room_status=" + room_status +
                ", room_createtime=" + room_createtime +
                ", room_updatetime=" + room_updatetime +
                "}";
    }
}
