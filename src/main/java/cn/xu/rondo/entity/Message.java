package cn.xu.rondo.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.io.Serializable;

/**
 * <p>
 * 消息表
 * </p>
 *
 * @author jason xu
 * @since 2021-07-17
 */
@TableName("sa_message")
public class Message implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "message_id", type = IdType.AUTO)
    private Long message_id;

    /**
     * user
     */
    private Integer message_user;

    /**
     * type
     */
    private String message_type;

    private String message_where;

    private String message_to;

    /**
     * content
     */
    private String message_content;

    /**
     * 状态
     */
    private Integer message_status;

    /**
     * 创建时间
     */
    private Integer message_createtime;

    /**
     * 修改时间
     */
    private Integer message_updatetime;


    public Long getMessage_id() {
        return message_id;
    }

    public void setMessage_id(Long message_id) {
        this.message_id = message_id;
    }

    public Integer getMessage_user() {
        return message_user;
    }

    public void setMessage_user(Integer message_user) {
        this.message_user = message_user;
    }

    public String getMessage_type() {
        return message_type;
    }

    public void setMessage_type(String message_type) {
        this.message_type = message_type;
    }

    public String getMessage_where() {
        return message_where;
    }

    public void setMessage_where(String message_where) {
        this.message_where = message_where;
    }

    public String getMessage_to() {
        return message_to;
    }

    public void setMessage_to(String message_to) {
        this.message_to = message_to;
    }

    public String getMessage_content() {
        return message_content;
    }

    public void setMessage_content(String message_content) {
        this.message_content = message_content;
    }

    public Integer getMessage_status() {
        return message_status;
    }

    public void setMessage_status(Integer message_status) {
        this.message_status = message_status;
    }

    public Integer getMessage_createtime() {
        return message_createtime;
    }

    public void setMessage_createtime(Integer message_createtime) {
        this.message_createtime = message_createtime;
    }

    public Integer getMessage_updatetime() {
        return message_updatetime;
    }

    public void setMessage_updatetime(Integer message_updatetime) {
        this.message_updatetime = message_updatetime;
    }

    @Override
    public String toString() {
        return "Message{" +
        "message_id=" + message_id +
        ", message_user=" + message_user +
        ", message_type=" + message_type +
        ", message_where=" + message_where +
        ", message_to=" + message_to +
        ", message_content=" + message_content +
        ", message_status=" + message_status +
        ", message_createtime=" + message_createtime +
        ", message_updatetime=" + message_updatetime +
        "}";
    }
}
