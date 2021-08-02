package cn.xu.rondo.entity.dto.user;


import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class UpdateUserDTO {
    @NotBlank(message = "名字还是要有的！")
    private String userName;
    @NotNull(message = "直接不传就不太好！")
    private String userHead;
    @NotNull(message = "不能不传呀！")
    private String userRemark;
    @NotNull(message = "男的女的？")
    private Integer userSex;
    @NotNull(message = "摸一摸提示要有")
    private String userTouchTip;
    @NotNull(message = "为空字符串就是不修改密码")
    private String userPassword;
}
