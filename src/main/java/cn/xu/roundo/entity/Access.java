package cn.xu.roundo.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.io.Serializable;

/**
 * <p>
 * 授权信息表
 * </p>
 *
 * @author jason xu
 * @since 2021-07-17
 */
@TableName("sa_access")
public class Access implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "access_id", type = IdType.AUTO)
    private Integer access_id;

    /**
     * 用户ID
     */
    private Integer access_user;

    /**
     * AccessToken
     */
    private String access_token;

    /**
     * 登录平台
     */
    private String access_plat;

    /**
     * IP
     */
    private String access_ip;

    /**
     * 状态
     */
    private Integer access_status;

    /**
     * 创建时间
     */
    private Integer access_createtime;

    /**
     * 修改时间
     */
    private Integer access_updatetime;


    public Integer getAccess_id() {
        return access_id;
    }

    public void setAccess_id(Integer access_id) {
        this.access_id = access_id;
    }

    public Integer getAccess_user() {
        return access_user;
    }

    public void setAccess_user(Integer access_user) {
        this.access_user = access_user;
    }

    public String getAccess_token() {
        return access_token;
    }

    public void setAccess_token(String access_token) {
        this.access_token = access_token;
    }

    public String getAccess_plat() {
        return access_plat;
    }

    public void setAccess_plat(String access_plat) {
        this.access_plat = access_plat;
    }

    public String getAccess_ip() {
        return access_ip;
    }

    public void setAccess_ip(String access_ip) {
        this.access_ip = access_ip;
    }

    public Integer getAccess_status() {
        return access_status;
    }

    public void setAccess_status(Integer access_status) {
        this.access_status = access_status;
    }

    public Integer getAccess_createtime() {
        return access_createtime;
    }

    public void setAccess_createtime(Integer access_createtime) {
        this.access_createtime = access_createtime;
    }

    public Integer getAccess_updatetime() {
        return access_updatetime;
    }

    public void setAccess_updatetime(Integer access_updatetime) {
        this.access_updatetime = access_updatetime;
    }

    @Override
    public String toString() {
        return "Access{" +
        "access_id=" + access_id +
        ", access_user=" + access_user +
        ", access_token=" + access_token +
        ", access_plat=" + access_plat +
        ", access_ip=" + access_ip +
        ", access_status=" + access_status +
        ", access_createtime=" + access_createtime +
        ", access_updatetime=" + access_updatetime +
        "}";
    }
}
