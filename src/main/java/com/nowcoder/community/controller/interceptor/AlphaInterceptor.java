/**
 * FileName: AlphaInterceptor
 * Author:   XueZhenLonG
 * Date:     2021/1/30 15:48
 * Description: 学习拦截器的示例
 */
package com.nowcoder.community.controller.interceptor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 〈学习拦截器的示例〉
 *
 * @author XueZhenLonG
 * @create 2021/1/30
 * @since 1.0.0
 */
@Component
public class AlphaInterceptor implements HandlerInterceptor {
    private static final Logger logger = LoggerFactory.getLogger(AlphaInterceptor.class);

    // 在Controller之前执行
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        logger.debug("preHandle:"+handler.toString());
        return true;
    }
    // 在Controller之后执行,模板引擎之前调用
    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        logger.debug("postHandle:"+handler.toString());
    }
    //在模板引擎(TemplateEngine)之后执行
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        logger.debug("afterCompletion:"+handler.toString());
    }


}
