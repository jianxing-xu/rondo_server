package cn.xu.rondo.response.exception;

import cn.xu.rondo.enums.EE;
import lombok.Getter;

@Getter
public class ApiException extends RuntimeException {
    private int code;
    private String msg;

    public ApiException(EE errorCodeEnum) {
        super(errorCodeEnum.getMsg());
        this.code = errorCodeEnum.getCode();
        this.msg = errorCodeEnum.getMsg();
    }
}