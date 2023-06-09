package cn.xu.rondo.config;

import cn.xu.rondo.response.exception.ApiException;
import io.jsonwebtoken.JwtException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import cn.xu.rondo.response.Response;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import java.util.ArrayList;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler({MethodArgumentNotValidException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Response<String> handlerMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        // 处理参数校验异常 Hibernate Validation
        FieldError error = e.getBindingResult().getFieldErrors().get(0);
        return new Response<>(400, String.format("%s: %s", error.getField(), error.getDefaultMessage()));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Response<String> handleConstraintViolationException(ConstraintViolationException e) {
        // 处理参数校验异常 Hibernate Validation
        ConstraintViolation<?> error = new ArrayList<>(e.getConstraintViolations()).get(0);
        return new Response<>(400, String.format("%s", error.getMessage()));
    }

    @ExceptionHandler(BindException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Response<String> handleBindException(BindException e) {
        // 处理参数校验异常 Hibernate Validation
        FieldError error = e.getBindingResult().getFieldErrors().get(0);
        return new Response<>(400, String.format("%s: %s", error.getField(), error.getDefaultMessage()));
    }


    @ExceptionHandler(JwtException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN) // http 403
    public Response<String> handleBindException(JwtException e) {
        log.warn(e.toString());
        // token校验失败返回
        return new Response<>(403, "登录过期");
    }

    //自定义异常统一响应
    @ExceptionHandler(ApiException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Response<String> handleBindException(ApiException e) {
        return new Response<>(e.getCode(), e.getMsg());
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Response<String> notSupportedMethod(HttpRequestMethodNotSupportedException e) {
        log.error("不支持的方法:{}", e.getMethod());
        return new Response<>(400, String.format("不支持的方法:%s", e.getMethod()));
    }

    /* springboot自带参数校验 参数传递不完整 */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Response<String> servletMissingParameter(MissingServletRequestParameterException e) {
        log.error(e.getMessage());
        return new Response<>(400, "参数不完整");
    }


    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR) // http 500
    public Response<String> runtimeException(Exception e) {
        e.printStackTrace();
        log.error(e.getMessage());
        return new Response<>(500, "系统错误");
    }


}