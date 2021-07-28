package cn.xu.rondo.config;


import cn.xu.rondo.enums.EE;
import cn.xu.rondo.response.NotResponseWrap;
import cn.xu.rondo.response.Response;
import cn.xu.rondo.response.exception.ApiException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

/**
 * 成功返回统一包装
 */

@RestControllerAdvice
public class ResponseControllerAdvice implements ResponseBodyAdvice<Object> {
    /**
     * 对那些方法需要包装，如果接口直接返回Response就没有必要再包装了，对于应用了免包装注解的接口就不需要包装直接返回
     *
     * @param returnType
     * @param aClass
     * @return 如果为true才会执行beforeBodyWrite
     */
    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> aClass) {
        return !(returnType.getParameterType().equals(Response.class) || returnType.hasMethodAnnotation(NotResponseWrap.class));
    }

    @Override
    public Object beforeBodyWrite(Object data, MethodParameter returnType, MediaType mediaType, Class<? extends HttpMessageConverter<?>> aClass, ServerHttpRequest request, ServerHttpResponse response) {
        // String类型不能直接包装，所以要进行些特别的处理
        if (returnType.getGenericParameterType().equals(String.class)) {
            ObjectMapper objectMapper = new ObjectMapper();
            try {
                // 将数据包装在Response里后，再转换为json字符串响应给前端
                return objectMapper.writeValueAsString(new Response<>(data));
            } catch (JsonProcessingException e) {
                throw new ApiException(EE.ERROR);
            }
        }
        // 这里统一包装
        return new Response<>(data);
    }
}
