package cn.xu.rondo.entity.dto.user;


import cn.xu.rondo.enums.SwitchEnum;
import cn.xu.rondo.utils.validation.EnumValue;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class UpdateUserDTOByAdmin extends UpdateUserDTO {
    @NotNull
    private Integer user_id;
    @NotNull
    private Integer user_role;
    @NotNull
    @EnumValue(enumClass = SwitchEnum.class,message = "vip 参数错误")
    private Integer user_vip;
    @NotNull
    @EnumValue(enumClass = SwitchEnum.class,message = "status 参数错误")
    private Integer user_status;
}
