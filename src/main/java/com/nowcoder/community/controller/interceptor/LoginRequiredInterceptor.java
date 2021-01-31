/**
 * FileName: LoginRequiredIterceptor
 * Author:   XueZhenLonG
 * Date:     2021/1/31 11:26
 * Description: 实现检查登录的拦截器
 */
package com.nowcoder.community.controller.interceptor;

import com.nowcoder.community.annotation.LoginRequired;
import com.nowcoder.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;

/**
 * 〈实现检查登录的拦截器〉
 *
 * @author XueZhenLonG
 * @create 2021/1/31
 * @since 1.0.0
 */
@Component
public class LoginRequiredInterceptor implements HandlerInterceptor {

    @Autowired
    private HostHolder hostHolder;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //判断拦截器拦截的类型是不是方法, 我们只需要拦截方法
        if (handler instanceof HandlerMethod) {
            //如果他是方法,那么就给他转型
            HandlerMethod handlerMethod = (HandlerMethod) handler;
            //获得方法对象
            Method method = handlerMethod.getMethod();
            //尝试取出方法的注解
            LoginRequired loginRequired = method.getAnnotation(LoginRequired.class);
            //如果获得了loginRequired,那么就可知道它是需要拦截的方法,如果没有用户登录就不能访问返回false. 用户登录才能访问的方法.
            if (loginRequired !=null && hostHolder.getUser() ==null){
                //用户没有登录,那么我们就强制的给他重定向会首页
                response.sendRedirect(request.getContextPath() + "/login");
                return false;
            }

        }

        return true;
    }
}
