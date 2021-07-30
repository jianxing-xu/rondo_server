package cn.xu.rondo.config;

import cn.xu.rondo.interceptor.TokenInterceptor;
import cn.xu.rondo.utils.params_resolver.UserIdResolver;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;


@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new TokenInterceptor())
                .addPathPatterns("/**")
                .excludePathPatterns("/user/login", "/res/**", "/common/**")
                .excludePathPatterns("/user/pwd/**")
                .excludePathPatterns("favicon.ico")
                .excludePathPatterns("/message/list/**")
                .excludePathPatterns("/badge/badge/**", "/song/playUrl/**")

                // 暂时 放行
                .excludePathPatterns("/**")
        ;
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(new UserIdResolver());
    }
}

