package cn.xu.rondo.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.io.Serializable;

/**
 * <p>
 * 应用表
 * </p>
 *
 * @author jason xu
 * @since 2021-07-17
 */
@TableName("sa_app")
public class App implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "app_id", type = IdType.AUTO)
    private Integer app_id;

    /**
     * key
     */
    private String app_key;

    /**
     * name
     */
    private String app_name;

    /**
     * url
     */
    private String app_url;

    /**
     * user
     */
    private Integer app_user;

    /**
     * scope
     */
    private String app_scope;

    /**
     * 状态
     */
    private Integer app_status;

    /**
     * 创建时间
     */
    private Integer app_createtime;

    /**
     * 修改时间
     */
    private Integer app_updatetime;


    public Integer getApp_id() {
        return app_id;
    }

    public void setApp_id(Integer app_id) {
        this.app_id = app_id;
    }

    public String getApp_key() {
        return app_key;
    }

    public void setApp_key(String app_key) {
        this.app_key = app_key;
    }

    public String getApp_name() {
        return app_name;
    }

    public void setApp_name(String app_name) {
        this.app_name = app_name;
    }

    public String getApp_url() {
        return app_url;
    }

    public void setApp_url(String app_url) {
        this.app_url = app_url;
    }

    public Integer getApp_user() {
        return app_user;
    }

    public void setApp_user(Integer app_user) {
        this.app_user = app_user;
    }

    public String getApp_scope() {
        return app_scope;
    }

    public void setApp_scope(String app_scope) {
        this.app_scope = app_scope;
    }

    public Integer getApp_status() {
        return app_status;
    }

    public void setApp_status(Integer app_status) {
        this.app_status = app_status;
    }

    public Integer getApp_createtime() {
        return app_createtime;
    }

    public void setApp_createtime(Integer app_createtime) {
        this.app_createtime = app_createtime;
    }

    public Integer getApp_updatetime() {
        return app_updatetime;
    }

    public void setApp_updatetime(Integer app_updatetime) {
        this.app_updatetime = app_updatetime;
    }

    @Override
    public String toString() {
        return "App{" +
        "app_id=" + app_id +
        ", app_key=" + app_key +
        ", app_name=" + app_name +
        ", app_url=" + app_url +
        ", app_user=" + app_user +
        ", app_scope=" + app_scope +
        ", app_status=" + app_status +
        ", app_createtime=" + app_createtime +
        ", app_updatetime=" + app_updatetime +
        "}";
    }
}
