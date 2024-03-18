package com.pvs.config;

import com.pvs.interceptor.LoginInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;

/**
 * @author 高志强
 * @version 1.0
 */
@Configuration
@Slf4j
public class WebMvcConfig extends WebMvcConfigurationSupport {
    //将拦截器自动注入
    @Autowired
    private LoginInterceptor loginInterceptor;
    /**
     *注册自定义拦截器
     */
    @Override
    protected void addInterceptors(InterceptorRegistry registry) {
        log.info("开始注册自定义拦截器");
        registry.addInterceptor(loginInterceptor)
                .addPathPatterns()//需要拦截的路径
                .excludePathPatterns(//不需要拦截的路径
                        "/user/login",
                        "/shop/**",
                        "/voucher/**",
                        "/shop-type/**",
                        "/bolg/hot",
                        "/user/code"
                );
    }
}
