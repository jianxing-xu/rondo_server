package cn.xu.rondo.entity.dto.user;

import lombok.Getter;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotEmpty;
@Getter
public class UpdatePwdDTO {
    @Length(min = 6, max = 16)
    @NotEmpty
    private String oldPwd;
    @Length(min = 6, max = 16)
    @NotEmpty
    private String newPwd;
}
