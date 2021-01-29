package com.nowcoder.community.service;

import com.nowcoder.community.dao.UserMapper;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.util.CommunityConstant;
import com.nowcoder.community.util.CommunityUtil;
import com.nowcoder.community.util.MailClient;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@Service
public class UserService implements CommunityConstant {

    @Autowired
    private UserMapper userMapper;
    //注入邮件客户端
    @Autowired
    private MailClient mailClient;
    //注入模板引擎
    @Autowired
    private TemplateEngine templateEngine;
    //注入域名
    @Value("${community.path.domain}")
    private String domain; //来接收注入的值
    //和路径名
    @Value("${server.servlet.context-path}")
    private String contextPath; //来接收注入的值

    //查询一个用户
    public User findUserById(int id) {
        return userMapper.selectById(id);
    }

    //用户注册方法
    public Map<String, Object> register(User user) {
        Map<String, Object> map = new HashMap<>();

        //对空值处理
        if (user == null) {
            throw new IllegalArgumentException("参数不能为空!");
        }

        //对账号处理
        if (StringUtils.isBlank(user.getUsername())) {
            map.put("usernameMsg", "账号不能为空!");
            return map;
        }

        //对密码处理
        if (StringUtils.isBlank(user.getPassword())) {
            map.put("passwordMsg", "密码不能为空!");
            return map;
        }

        //对邮箱处理
        if (StringUtils.isBlank(user.getEmail())) {
            map.put("emailMsg", "邮箱不能为空!");
            return map;
        }

        //验证账号
        User u = userMapper.selectByName(user.getUsername());
        if (u != null) {
            map.put("usernameMsg", "该账号已存在!");
            return map;
        }

        //验证邮箱
        User e = userMapper.selectByEmail(user.getEmail());
        if (e != null) {
            map.put("emailMsg", "该邮箱已被注册!");
            return map;
        }

        //注册用户流程
        //产生随机数
        user.setSalt(CommunityUtil.generateUUID().substring(0, 5));
        //对密码进行加密
        user.setPassword(CommunityUtil.md5(user.getPassword() + user.getSalt()));
        //用户类型
        user.setType(0);
        //用户状态
        user.setStatus(0);
        //用户激活码
        user.setActivationCode(CommunityUtil.generateUUID());
        //用户头像设置
        user.setHeaderUrl(String.format("http://images.nowcoder.com/head/%dt.png",
                new Random().nextInt(1000)));
        //用户注册时间
        user.setCreateTime(new Date());
        //把设置好的user对象添加到库中
        userMapper.insertUser(user);

        //给用户发送激活邮件
        Context context = new Context();
        context.setVariable("email", user.getEmail());
        //路径示例:http://localhost:8080/community/activation/101/code
        //mybatis.configuration.useGeneratedKeys=true因为我们在application.properties 配置了mybatis
        //所以 完成添加之后 user的Id会进行属性的回填 我们在拼接url的时候就可以获取到用户的id;
        String url = domain + contextPath + "/activation/" + user.getId() +"/"+ user.getActivationCode();
        context.setVariable("url", url);
        //使用模板引擎,生成邮件的内容
        String content = templateEngine.process("/mail/activation", context);
        //发送邮件
        mailClient.sendMail(user.getEmail(), "激活账号", content);

        return map;
    }

    //判断激活状态的方法
    public int activation(int userId, String code) {
        User user = userMapper.selectById(userId);
        if (user.getStatus() == 1) { //已经激活过了
            //重复激活!
            return ACTIVATION_REPEAT;
        } else if (user.getActivationCode().equals(code)) {//返回的激活码与数据库中的一致,开始激活!
            //改变用户的激活状态
            userMapper.updateStatus(userId, 1);
            return ACTIVATION_SUCCESS;
        } else {//激活失败!
            return ACTIVATION_FAILURE;
        }

    }


}
