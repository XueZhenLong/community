/**
 * FileName: WebMvcConfig
 * Author:   XueZhenLonG
 * Date:     2021/1/30 16:01
 * Description: 拦截器的配置类
 */
package com.nowcoder.community.config;

import com.nowcoder.community.annotation.LoginRequired;
import com.nowcoder.community.controller.interceptor.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 〈拦截器的配置类〉
 *
 * @author XueZhenLonG
 * @create 2021/1/30
 * @since 1.0.0
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
    //进行拦截器的注入
    //示例 拦截器
    @Autowired
    private AlphaInterceptor alphaInterceptor;
    //判断用户登录的拦截器
    @Autowired
    private LoginTicketInterceptor loginTicketInterceptor;

    //我们使用了Spring security进行了重构,这个就不用了.
    //检查用户登录状态的拦截器
//    @Autowired
//    private LoginRequiredInterceptor loginRequiredInterceptor;

    //注入统计用户消息总数的拦截器
    @Autowired
    private MessageInterceptor messageInterceptor;

    //注入统计用户登录,活跃次数的拦截器
    @Autowired
    private DataInterceptor dataInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(alphaInterceptor)
                .excludePathPatterns("/**/*.css","/**/*.js","/**/*.jpg","/**/*.jpeg","/**/*.png")
                .addPathPatterns("/register","/login");

        registry.addInterceptor(loginTicketInterceptor)
                .excludePathPatterns("/**/*.css","/**/*.js","/**/*.jpg","/**/*.jpeg","/**/*.png");

//        registry.addInterceptor(loginRequiredInterceptor)
//                .excludePathPatterns("/**/*.css","/**/*.js","/**/*.jpg","/**/*.jpeg","/**/*.png");

        registry.addInterceptor(messageInterceptor)
                .excludePathPatterns("/**/*.css","/**/*.js","/**/*.jpg","/**/*.jpeg","/**/*.png");

        registry.addInterceptor(dataInterceptor)
                .excludePathPatterns("/**/*.css","/**/*.js","/**/*.jpg","/**/*.jpeg","/**/*.png");

    }


}
