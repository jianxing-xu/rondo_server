package cn.xu.rondo.entity.dto.user;


import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class UpdateUserDTO {
    @NotBlank(message = "名字还是要有的！")
    private String user_name;
    @NotNull(message = "头像直接不传就不太好！")
    private String user_head;
//    @NotNull(message = "个性签名不能不传呀！")
    private String user_remark = "";
    @NotNull(message = "男的女的？")
    private Integer user_sex;
//    @NotNull(message = "摸一摸提示要有")
    private String user_touchtip = "";
//    @NotNull(message = "为空字符串就是不修改密码")
    private String user_password = "";
}
