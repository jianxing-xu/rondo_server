package cn.xu.rondo.entity.dto;


import lombok.Getter;

import javax.validation.constraints.NotEmpty;

@Getter
public class LoginDTO {
    @NotEmpty(message = "用户名不能为空")
    private String account;
    @NotEmpty(message = "密码不能为空")
    private String password;
    private String plat = "xx";
}
