package cn.xu.rondo.entity.vo;


import cn.xu.rondo.entity.User;
import lombok.Data;

/**
 * 聊天消息返回对象
 */
@Data
public class MessageVO {
    String message_content;//: "hello alert('Xss来了')"
    Integer message_createtime;//: 1627455963
    Integer message_id;//: 10
    Integer message_status;//: 0
    Integer message_to;//: 888
    String message_type;//: "text"
    Integer message_updatetime;//: 1627455963
    Integer message_user;//: 100006
    String message_where;//: "channel"
    User user;
}
