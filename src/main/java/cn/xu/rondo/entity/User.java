package cn.xu.rondo.entity;

import cn.xu.rondo.utils.Constants;
import cn.xu.rondo.utils.StringUtils;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * 用户表
 * </p>
 *
 * @author jason xu
 * @since 2021-07-17
 */
@TableName("sa_user")
public class User implements Serializable {

    private static final Logger log = LoggerFactory.getLogger(User.class);

    private static final long serialVersionUID = 1L;

    public void encodePwd() {
        user_password = new String(Constants.aes.encrypt(user_password));
        log.warn(this.user_password, this.user_password.length());
    }
    public String encodePwd(String password) {
        return new String(Constants.aes.encrypt(password));
    }

    public boolean verifyPwd(String pwd) {
        return user_password.equals(new String(Constants.aes.encrypt(pwd)));
    }

    /**
     * UID
     */
    @TableId(value = "user_id", type = IdType.AUTO)
    private Integer user_id;

    private Integer user_icon;

    private Integer user_sex;

    /**
     * 帐号
     */
    private String user_account;

    /**
     * 密码
     */
    @JsonIgnore
    private String user_password;

    /**
     * 密码盐
     */
    private String user_salt;

    /**
     * 用户昵称
     */
    private String user_name;

    private String user_head;

    private String user_remark;

    /**
     * 用户组
     */
    private Integer user_group;

    /**
     * 注册IP
     */
    private String user_ipreg;

    private String user_openid;

    private String user_extra;

    private Integer user_app;

    private String user_device;

    private String user_touchtip;

    private Integer user_vip;

    public Integer getUser_vip() {
        return user_vip;
    }

    public void setUser_vip(Integer user_vip) {
        this.user_vip = user_vip;
    }

    public Integer getUser_role() {
        return user_role;
    }

    public void setUser_role(Integer user_role) {
        this.user_role = user_role;
    }

    /**
     * 1被禁用
     */
    private Integer user_status;

    /**
     * 创建时间
     */
    private Integer user_createtime;

    /**
     * 修改时间
     */
    private Integer user_updatetime;

    /**
     * 1：管理员 0：普通用户
     */
    private Integer user_role;

    public boolean isAdmin() {
        return "1".equals(user_role+"");
    }

    public boolean isVip() {
        return user_vip == 1;
    }

    public Integer getRole() {
        return user_role;
    }

    public void setRole(Integer user_role) {
        this.user_role = user_role;
    }

    public Integer getUser_id() {
        return user_id;
    }

    public void setUser_id(Integer user_id) {
        this.user_id = user_id;
    }

    public Integer getUser_icon() {
        return user_icon;
    }

    public void setUser_icon(Integer user_icon) {
        this.user_icon = user_icon;
    }

    public Integer getUser_sex() {
        return user_sex;
    }

    public void setUser_sex(Integer user_sex) {
        this.user_sex = user_sex;
    }

    public String getUser_account() {
        return user_account;
    }

    public void setUser_account(String user_account) {
        this.user_account = user_account;
    }

    public String getUser_password() {
        return user_password;
    }

    public void setUser_password(String user_password) {
        this.user_password = user_password;
    }

    public String getUser_salt() {
        return user_salt;
    }

    public void setUser_salt(String user_salt) {
        this.user_salt = user_salt;
    }

    public String getUser_name() {
        return user_name;
    }

    public void setUser_name(String user_name) {
        this.user_name = user_name;
    }

    public String getUser_head() {
        return user_head;
    }

    public void setUser_head(String user_head) {
        this.user_head = user_head;
    }

    public String getUser_remark() {
        return user_remark;
    }

    public void setUser_remark(String user_remark) {
        this.user_remark = user_remark;
    }

    public Integer getUser_group() {
        return user_group;
    }

    public void setUser_group(Integer user_group) {
        this.user_group = user_group;
    }

    public String getUser_ipreg() {
        return user_ipreg;
    }

    public void setUser_ipreg(String user_ipreg) {
        this.user_ipreg = user_ipreg;
    }

    public String getUser_openid() {
        return user_openid;
    }

    public void setUser_openid(String user_openid) {
        this.user_openid = user_openid;
    }

    public String getUser_extra() {
        return user_extra;
    }

    public void setUser_extra(String user_extra) {
        this.user_extra = user_extra;
    }

    public Integer getUser_app() {
        return user_app;
    }

    public void setUser_app(Integer user_app) {
        this.user_app = user_app;
    }

    public String getUser_device() {
        return user_device;
    }

    public void setUser_device(String user_device) {
        this.user_device = user_device;
    }

    public String getUser_touchtip() {
        return user_touchtip;
    }

    public void setUser_touchtip(String user_touchtip) {
        this.user_touchtip = user_touchtip;
    }

//    public String getUser_vip() {
//        return user_vip;
//    }
//
//    public void setUser_vip(String user_vip) {
//        this.user_vip = user_vip;
//    }

    public Integer getUser_status() {
        return user_status;
    }

    public void setUser_status(Integer user_status) {
        this.user_status = user_status;
    }

    public Integer getUser_createtime() {
        return user_createtime;
    }

    public void setUser_createtime(Integer user_createtime) {
        this.user_createtime = user_createtime;
    }

    public Integer getUser_updatetime() {
        return user_updatetime;
    }

    public void setUser_updatetime(Integer user_updatetime) {
        this.user_updatetime = user_updatetime;
    }

    @Override
    public String toString() {
        return "User{" +
                "user_id=" + user_id +
                ", user_icon=" + user_icon +
                ", user_sex=" + user_sex +
                ", user_account=" + user_account +
                ", user_password=" + user_password +
                ", user_salt=" + user_salt +
                ", user_name=" + user_name +
                ", user_head=" + user_head +
                ", user_remark=" + user_remark +
                ", user_group=" + user_group +
                ", user_ipreg=" + user_ipreg +
                ", user_openid=" + user_openid +
                ", user_extra=" + user_extra +
                ", user_app=" + user_app +
                ", user_device=" + user_device +
                ", user_touchtip=" + user_touchtip +
                ", user_vip=" + user_vip +
                ", user_status=" + user_status +
                ", user_createtime=" + user_createtime +
                ", user_updatetime=" + user_updatetime +
                "}";
    }
}
