package cn.xu.rondo.response.exception;

import cn.xu.rondo.enums.ErrorEnum;
import lombok.Getter;

@Getter
public class ApiException extends RuntimeException {
    private int code;
    private String msg;

    public ApiException(ErrorEnum errorCodeEnum) {
        super(errorCodeEnum.getMsg());
        this.code = errorCodeEnum.getCode();
        this.msg = errorCodeEnum.getMsg();
    }
}