package cn.xu.rondo.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.io.Serializable;

/**
 * <p>
 * 小程序用户表
 * </p>
 *
 * @author jason xu
 * @since 2021-07-17
 */
@TableName("sa_weapp")
public class Weapp implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "weapp_id", type = IdType.AUTO)
    private Integer weapp_id;

    /**
     * OPENID
     */
    private String weapp_openid;

    /**
     * 状态
     */
    private Integer weapp_status;

    /**
     * 创建时间
     */
    private Integer weapp_createtime;

    /**
     * 修改时间
     */
    private Integer weapp_updatetime;


    public Integer getWeapp_id() {
        return weapp_id;
    }

    public void setWeapp_id(Integer weapp_id) {
        this.weapp_id = weapp_id;
    }

    public String getWeapp_openid() {
        return weapp_openid;
    }

    public void setWeapp_openid(String weapp_openid) {
        this.weapp_openid = weapp_openid;
    }

    public Integer getWeapp_status() {
        return weapp_status;
    }

    public void setWeapp_status(Integer weapp_status) {
        this.weapp_status = weapp_status;
    }

    public Integer getWeapp_createtime() {
        return weapp_createtime;
    }

    public void setWeapp_createtime(Integer weapp_createtime) {
        this.weapp_createtime = weapp_createtime;
    }

    public Integer getWeapp_updatetime() {
        return weapp_updatetime;
    }

    public void setWeapp_updatetime(Integer weapp_updatetime) {
        this.weapp_updatetime = weapp_updatetime;
    }

    @Override
    public String toString() {
        return "Weapp{" +
        "weapp_id=" + weapp_id +
        ", weapp_openid=" + weapp_openid +
        ", weapp_status=" + weapp_status +
        ", weapp_createtime=" + weapp_createtime +
        ", weapp_updatetime=" + weapp_updatetime +
        "}";
    }
}
