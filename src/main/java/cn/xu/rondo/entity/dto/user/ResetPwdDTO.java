package cn.xu.rondo.entity.dto.user;

import lombok.Getter;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotEmpty;

/**
 *      * @param code   验证码
 *      * @param mail   邮箱
 *      * @param newPwd 新密码
 */
@Getter
public class ResetPwdDTO {
    @NotEmpty
    private String code;
    @NotEmpty
    private String mail;
    @NotEmpty
    @Length(min = 6, max = 16)
    private String newPwd;
}
