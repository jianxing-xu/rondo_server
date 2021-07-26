package cn.xu.rondo.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.io.Serializable;

/**
 * <p>
 * 配置表
 * </p>
 *
 * @author jason xu
 * @since 2021-07-17
 */
@TableName("sa_conf")
public class Conf implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "conf_id", type = IdType.AUTO)
    private Integer conf_id;

    /**
     * 参数名
     */
    private String conf_key;

    /**
     * 参数值
     */
    private String conf_value;

    /**
     * 参数描述
     */
    private String conf_desc;

    /**
     * 参数到期
     */
    private Integer conf_int;

    private Integer conf_status;

    private Integer conf_createtime;

    private Integer conf_updatetime;


    public Integer getConf_id() {
        return conf_id;
    }

    public void setConf_id(Integer conf_id) {
        this.conf_id = conf_id;
    }

    public String getConf_key() {
        return conf_key;
    }

    public void setConf_key(String conf_key) {
        this.conf_key = conf_key;
    }

    public String getConf_value() {
        return conf_value;
    }

    public void setConf_value(String conf_value) {
        this.conf_value = conf_value;
    }

    public String getConf_desc() {
        return conf_desc;
    }

    public void setConf_desc(String conf_desc) {
        this.conf_desc = conf_desc;
    }

    public Integer getConf_int() {
        return conf_int;
    }

    public void setConf_int(Integer conf_int) {
        this.conf_int = conf_int;
    }

    public Integer getConf_status() {
        return conf_status;
    }

    public void setConf_status(Integer conf_status) {
        this.conf_status = conf_status;
    }

    public Integer getConf_createtime() {
        return conf_createtime;
    }

    public void setConf_createtime(Integer conf_createtime) {
        this.conf_createtime = conf_createtime;
    }

    public Integer getConf_updatetime() {
        return conf_updatetime;
    }

    public void setConf_updatetime(Integer conf_updatetime) {
        this.conf_updatetime = conf_updatetime;
    }

    @Override
    public String toString() {
        return "Conf{" +
        "conf_id=" + conf_id +
        ", conf_key=" + conf_key +
        ", conf_value=" + conf_value +
        ", conf_desc=" + conf_desc +
        ", conf_int=" + conf_int +
        ", conf_status=" + conf_status +
        ", conf_createtime=" + conf_createtime +
        ", conf_updatetime=" + conf_updatetime +
        "}";
    }
}
