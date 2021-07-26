package cn.xu.rondo.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.io.Serializable;

/**
 * <p>
 * 附件表
 * </p>
 *
 * @author jason xu
 * @since 2021-07-17
 */
@TableName("sa_attach")
public class Attach implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "attach_id", type = IdType.AUTO)
    private Integer attach_id;

    /**
     * 路径
     */
    private String attach_path;

    private Integer attach_used;

    private String attach_thumb;

    /**
     * 类型
     */
    private String attach_type;

    private String attach_sha;

    /**
     * 大小
     */
    private Integer attach_size;

    /**
     * 用户
     */
    private Integer attach_user;

    /**
     * 状态
     */
    private Integer attach_status;

    /**
     * 创建时间
     */
    private Integer attach_createtime;

    /**
     * 修改时间
     */
    private Integer attach_updatetime;


    public Integer getAttach_id() {
        return attach_id;
    }

    public void setAttach_id(Integer attach_id) {
        this.attach_id = attach_id;
    }

    public String getAttach_path() {
        return attach_path;
    }

    public void setAttach_path(String attach_path) {
        this.attach_path = attach_path;
    }

    public Integer getAttach_used() {
        return attach_used;
    }

    public void setAttach_used(Integer attach_used) {
        this.attach_used = attach_used;
    }

    public String getAttach_thumb() {
        return attach_thumb;
    }

    public void setAttach_thumb(String attach_thumb) {
        this.attach_thumb = attach_thumb;
    }

    public String getAttach_type() {
        return attach_type;
    }

    public void setAttach_type(String attach_type) {
        this.attach_type = attach_type;
    }

    public String getAttach_sha() {
        return attach_sha;
    }

    public void setAttach_sha(String attach_sha) {
        this.attach_sha = attach_sha;
    }

    public Integer getAttach_size() {
        return attach_size;
    }

    public void setAttach_size(Integer attach_size) {
        this.attach_size = attach_size;
    }

    public Integer getAttach_user() {
        return attach_user;
    }

    public void setAttach_user(Integer attach_user) {
        this.attach_user = attach_user;
    }

    public Integer getAttach_status() {
        return attach_status;
    }

    public void setAttach_status(Integer attach_status) {
        this.attach_status = attach_status;
    }

    public Integer getAttach_createtime() {
        return attach_createtime;
    }

    public void setAttach_createtime(Integer attach_createtime) {
        this.attach_createtime = attach_createtime;
    }

    public Integer getAttach_updatetime() {
        return attach_updatetime;
    }

    public void setAttach_updatetime(Integer attach_updatetime) {
        this.attach_updatetime = attach_updatetime;
    }

    @Override
    public String toString() {
        return "Attach{" +
        "attach_id=" + attach_id +
        ", attach_path=" + attach_path +
        ", attach_used=" + attach_used +
        ", attach_thumb=" + attach_thumb +
        ", attach_type=" + attach_type +
        ", attach_sha=" + attach_sha +
        ", attach_size=" + attach_size +
        ", attach_user=" + attach_user +
        ", attach_status=" + attach_status +
        ", attach_createtime=" + attach_createtime +
        ", attach_updatetime=" + attach_updatetime +
        "}";
    }
}
