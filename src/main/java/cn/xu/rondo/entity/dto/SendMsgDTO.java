package cn.xu.rondo.entity.dto;


import cn.xu.rondo.enums.ChatType;
import cn.xu.rondo.utils.params_resolver.UserId;
import cn.xu.rondo.utils.validation.EnumValue;
import lombok.Data;
import lombok.Getter;
import org.hibernate.validator.constraints.Length;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.annotation.RequestParam;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

//@RequestParam("to") Integer roomId,
//@RequestParam(value = "at", required = false) AtDTO atUser,
//@RequestParam("type") @EnumValue(enumClass = ChatType.class, message = "未知消息类型") String type,
//@RequestParam(value = "where", required = false, defaultValue = "channel") String where,
//@RequestParam(value = "msg", required = false, defaultValue = "") @Length(max = 200) String msg,
//@RequestParam(value = "resource", required = false, defaultValue = "") String resource,
//@UserId Integer userId
@Getter
public class SendMsgDTO {
    @NotNull
    private Integer room_id;

    @Valid
    private AtDTO atUser;

    @EnumValue(enumClass = ChatType.class, message = "未知消息类型")
    @NotNull
    private String type;

    private String where = "channel";

    @Length(max = 200)
    private String msg = "";

    private String resource = "";
}
