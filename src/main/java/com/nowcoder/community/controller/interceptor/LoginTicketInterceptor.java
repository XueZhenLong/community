/**
 * FileName: LoginTicketInterceptor
 * Author:   XueZhenLonG
 * Date:     2021/1/30 16:13
 * Description: 验证用户登录状态的拦截器
 */
package com.nowcoder.community.controller.interceptor;

import com.nowcoder.community.entity.LoginTicket;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.service.UserService;
import com.nowcoder.community.util.CookieUtil;
import com.nowcoder.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;

/**
 * 〈验证用户登录状态的拦截器〉
 *
 * @author XueZhenLonG
 * @create 2021/1/30
 * @since 1.0.0
 */
@Component
public class LoginTicketInterceptor implements HandlerInterceptor {

    @Autowired
    private UserService userService;

    @Autowired
    private HostHolder hostHolder;

    // 在Controller之前执行
    //我们一开始就要验证用户的登录状态
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //从cookie 中获取凭证
        String ticket = CookieUtil.getValue(request,"ticket");
        //如果ticket有值,name就是登录状态
        if (ticket!=null){
            //查询凭证
            LoginTicket loginTicket = userService.findLoginTicket(ticket);
            //判断凭证是否有效
            if (loginTicket!=null && loginTicket.getStatus() ==0
                    && loginTicket.getExpired().after(new Date())){
                //凭证有效,根据凭证查询用户
                User user = userService.findUserById(loginTicket.getUserId());
                //在本次请求中持有用户
                hostHolder.setUser(user);
            }
        }
        return true;
    }
    // 在Controller之后执行,模板引擎之前调用
    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        User user = hostHolder.getUser();
        if (user!=null && modelAndView !=null){
            String headerUrl = user.getHeaderUrl();
            modelAndView.addObject("loginUser",user);
            modelAndView.addObject("headerUrl",headerUrl);

        }
    }
    //在模板引擎(TemplateEngine)之后执行
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        hostHolder.clear();
    }
}
