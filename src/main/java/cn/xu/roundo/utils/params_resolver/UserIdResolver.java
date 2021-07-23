package cn.xu.roundo.utils.params_resolver;

import cn.xu.roundo.utils.Constants;
import cn.xu.roundo.utils.JWTUtils;
import io.jsonwebtoken.Claims;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import javax.servlet.http.HttpServletRequest;


public class UserIdResolver implements HandlerMethodArgumentResolver {

    public static final Logger log = LoggerFactory.getLogger(UserIdResolver.class);

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return (parameter.getParameterType().isAssignableFrom(String.class) || parameter.getParameterType().isAssignableFrom(Integer.class)) && parameter.hasParameterAnnotation(UserId.class);
    }

    @Override
    public Object resolveArgument(MethodParameter methodParameter, ModelAndViewContainer modelAndViewContainer, NativeWebRequest nativeWebRequest, WebDataBinderFactory webDataBinderFactory) throws Exception {
        HttpServletRequest request = nativeWebRequest.getNativeRequest(HttpServletRequest.class);
        String token = request.getHeader("token");
        if (Constants.tempToken.equals(token)) return -1;
        Claims body = JWTUtils.verifyJwt(token);
        Integer userId = Integer.valueOf(body.getId());
        return userId;
    }
}