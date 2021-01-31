/**
 * FileName: LoginController
 * Author:   XueZhenLonG
 * Date:     2021/1/29 15:25
 * Description: 登录控制器
 */
package com.nowcoder.community.controller;

import com.google.code.kaptcha.Producer;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.service.UserService;
import com.nowcoder.community.util.CommunityConstant;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.imageio.ImageIO;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

/**
 * 〈登录控制器〉
 *
 * @author XueZhenLonG
 * @create 2021/1/29
 * @since 1.0.0
 */
@Controller
public class LoginController implements CommunityConstant {

    //创建一个日志对象
    private static final Logger logger = LoggerFactory.getLogger(LoginController.class);

    @Autowired
    private UserService userService;

    @Autowired
    private Producer kaptchaProducer;

    @Value("server.servlet.context-path")
    private String context_path;


    /**
     * 访问登录页面
     */

    @RequestMapping(path = "/login",method = RequestMethod.GET)
    public String getLoginPage(){
        return "/site/login";
    }

    /**
     * 访问注册页面
     */
    @RequestMapping(path = "/register", method = RequestMethod.GET)
    public String getRegisterPage(){
        return "/site/register";
    }


    //处理注册的请求,需要浏览器提交数据,所以是post请求
    @RequestMapping(path = "/register", method = RequestMethod.POST)
    public String register(Model model, User user){
        Map<String, Object> map = userService.register(user);
        //当我们添加用户成功的时候
        if (map==null || map.isEmpty()){
            //注册成功之后跳转到注册成功的页面,给出成功信息.并跳转会主页面!
            model.addAttribute("msg","注册成功,我们已经向您的邮箱发送一个一封激活邮件,请您尽快激活");
            //告诉中间页面,跳转目标
            model.addAttribute("target","/index");
            return "/site/operate-result";
        }else {//当我们添加用户失败的时候
            //获取失败的原因
            model.addAttribute("usernameMsg",map.get("usernameMsg"));
            model.addAttribute("passwordMsg",map.get("passwordMsg"));
            model.addAttribute("emailMsg",map.get("emailMsg"));
            //跳转回注册页面
            return "/site/register";
        }
    }

    //处理注册用户的激活功能
    //路径示例:http://localhost:8080/community/activation/101/code
    @RequestMapping(path = "/activation/{userId}/{code}",method = RequestMethod.GET)
    public String activation(Model model, @PathVariable("userId")int userId,@PathVariable("code")String code){
        int result = userService.activation(userId, code);
        if (result == ACTIVATION_SUCCESS){
            //激活成功之后跳转到激活成功的页面,给出成功信息.并跳转回登录页面!
            model.addAttribute("msg","激活成功,您的账号已经可以正常使用!");
            //告诉中间页面,跳转目标
            model.addAttribute("target","/login");

        }else if (result == ACTIVATION_REPEAT){
            model.addAttribute("msg","无效操作,该账号已经激活过了!");
            model.addAttribute("target","/index");
        }else{
            model.addAttribute("msg","激活失败,您提供的激活码不正确!");
            model.addAttribute("target","/index");
        }
        return "/site/operate-result";
    }

    //生成随机的验证码方法
    @RequestMapping(path = "/kaptcha",method = RequestMethod.GET)
    public void getKaptcha(HttpServletResponse response, HttpSession session){
        //生成验证码
        String text = kaptchaProducer.createText();
        //生成图片
        BufferedImage image = kaptchaProducer.createImage(text);
        //将验证码存入session
        session.setAttribute("kaptcha",text);
        //将图片输出给浏览器
        //设置相应的数据类型
        response.setContentType("image/png");
        try {
            OutputStream os = response.getOutputStream();
            ImageIO.write(image,"png",os);
        } catch (IOException e) {
            logger.error("响应验证码失败:"+e.getMessage());
        }

    }


    //处理用户登录验证的功能
    @RequestMapping(path = "/login",method = RequestMethod.POST)
    public String login(String username,String password,String code,
                        boolean rememberme,Model model,HttpSession session,HttpServletResponse response) {
        String kaptcha = (String) session.getAttribute("kaptcha");
        //进行验证码的检查
        if (StringUtils.isBlank(kaptcha) || StringUtils.isBlank(code) || !kaptcha.equalsIgnoreCase(code)){
            model.addAttribute("codeMsg","验证码不正确!");
            return "/site/login";
        }
        //检查账号密码
        //设置凭证超时的秒数
        int expiredSeconds = rememberme ? REMEMBER_EXPIRED_SECONDS : DEFAULT_EXPIRED_SECONDS;
        //调用登录验证服务
        Map<String, Object> map = userService.login(username, password, expiredSeconds);
        //对map进行判断
        if (map.containsKey("ticket")){
            //创建一个cookie在本地存储凭证
            Cookie cookie = new Cookie("ticket", map.get("ticket").toString());
            //设置cookie的生效路径
            cookie.setPath(context_path);
            //设置cookie的存活时间
            cookie.setMaxAge(expiredSeconds);
            //将cookie添加到response里面
            response.addCookie(cookie);
            //含有ticket说明登陆成功,重定向到首页
            return "redirect:/index";

        }else {
            model.addAttribute("usernameMsg",map.get("usernameMsg"));
            model.addAttribute("passwordMsg",map.get("passwordMsg"));
            //发生错误,返回登录页面
            return "/site/login";
        }

    }


    //处理用户的退出的功能
    @RequestMapping(path = "/logout",method = RequestMethod.GET)
    public String logout(@CookieValue("ticket")String ticket){
        userService.logout(ticket);
        return "redirect:/login";
    }

}
