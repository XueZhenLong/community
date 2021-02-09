package com.nowcoder.community.service;

import com.nowcoder.community.dao.LoginTicketMapper;
import com.nowcoder.community.dao.UserMapper;
import com.nowcoder.community.entity.LoginTicket;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.util.CommunityConstant;
import com.nowcoder.community.util.CommunityUtil;
import com.nowcoder.community.util.MailClient;
import com.nowcoder.community.util.RedisKeyUtil;
import io.lettuce.core.RedisURI;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
public class UserService implements CommunityConstant {
    //注入验证用户登录
//    @Autowired
//    private LoginTicketMapper loginTicketMapper;

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

    @Autowired
    private RedisTemplate redisTemplate;

    //查询一个用户
    public User findUserById(int id) {
//        return userMapper.selectById(id);
        //使用Redis进行优化
        User user = getCache(id);
        //如果没有在Redis缓存中查询到,那么久初始化缓存
        if (user == null){
            user = initCache(id);
        }
        return user;
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
        String url = domain + contextPath + "/activation/" + user.getId() + "/" + user.getActivationCode();
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
            //因为激活用户状态,改变了用户, 因此我们清理掉Redis中的缓存数据
            clearCache(userId);
            return ACTIVATION_SUCCESS;
        } else {//激活失败!
            return ACTIVATION_FAILURE;
        }

    }

    //实现用户的登录验证的功能
    public Map<String, Object> login(String username, String password,int expiredSeconds) {
        HashMap<String, Object> map = new HashMap<>();
        //空值处理
        if (StringUtils.isBlank(username)){
            map.put("usernameMsg","账号不能为空!");
            return map;
        }
        if (StringUtils.isBlank(password)){
            map.put("passwordMsg","密码不能为空!");
            return map;
        }
        //验证账号是否存在
        User user = userMapper.selectByName(username);
        if (user==null){
            map.put("usernameMsg","该账号不存在!");
            return map;
        }
        //验证用户的状态
        if (user.getStatus()==0){
            map.put("usernameMsg","该账号未激活!");
            return map;
        }
        //验证用户的密码
        password = CommunityUtil.md5(password+user.getSalt());
        if (!user.getPassword().equals(password)){
            map.put("passwordMsg","密码不正确!");
            return map;
        }
        //生成登录凭证 说明账号密码都是对的,所以要产生登录凭证,服务器要记录,客户端也要记录
        LoginTicket loginTicket = new LoginTicket();
        loginTicket.setUserId(user.getId());
        loginTicket.setTicket(CommunityUtil.generateUUID());
        loginTicket.setStatus(0);
        loginTicket.setExpired(new Date(System.currentTimeMillis() + expiredSeconds *1000));
//        loginTicketMapper.insertLoginTicket(loginTicket);
        //使用Redis优化登录凭证,添加凭证
        //----------------------------
        //取出ticketKey
        String redisKey = RedisKeyUtil.getTicketKey(loginTicket.getTicket());
        redisTemplate.opsForValue().set(redisKey,loginTicket);
        //----------------------------

        //将凭证存入map
        map.put("ticket",loginTicket.getTicket());
        return map;
    }

    //实现用户的退出功能
    public void logout(String ticket){
        //调用loginTicketMapper改变用户的登录状态
//        loginTicketMapper.updateStatus(ticket,1);
        //使用Redis优化登录凭证,删除凭证
        //----------------------------
        //先取出来,改变其状态,再添加回去,覆盖原有的值
        String redisKey = RedisKeyUtil.getTicketKey(ticket);
        LoginTicket loginTicket = (LoginTicket) redisTemplate.opsForValue().get(redisKey);
        loginTicket.setStatus(1);
        redisTemplate.opsForValue().set(redisKey,loginTicket);
        //----------------------------

    }

    //查询用户登录凭证的
    public LoginTicket findLoginTicket(String ticket){
        String redisKey = RedisKeyUtil.getTicketKey(ticket);
        return (LoginTicket)redisTemplate.opsForValue().get(redisKey);
    }

    //用于用户上传文件,对数据库的headerUrl进行修改
    public int updateHeader(int userId, String headerUrl){
//        return userMapper.updateHeader(userId, headerUrl);
        //修改了用户信息,要清理Redis中的缓存
        //先进行对于数据库的更新,再对Redis进行清理,返回行数
        int rows = userMapper.updateHeader(userId, headerUrl);
        clearCache(userId);
        return rows;


    }

    //用于用户的修改密码功能
    public int changePassword(int userId,String password){
        int rows = userMapper.updatePassword(userId, password);
        clearCache(userId);
        return rows;
    }

    //通过用户的名字查询
    public User findUserByName(String username){
        return userMapper.selectByName(username);
    }

//------------------------用户详情优化-----------------------------

    /**
     * 优化查询用户信息功能,使用Redis缓存用户信息数据
     */
    //1.查询时,优先从缓存中取值
    private User getCache(int userId){
        //尝试从Redis缓存中取user对象
        String redisKey = RedisKeyUtil.getUserKey(userId);
        return (User) redisTemplate.opsForValue().get(redisKey);
    }

    //2.取不到,初始化缓存数据
    private User initCache(int userId){
        //从数据库中取出user对象
        User user = userMapper.selectById(userId);
        //把取出来的user对象放入到Redis缓存中
        String redisKey = RedisKeyUtil.getUserKey(userId);
        redisTemplate.opsForValue().set(redisKey,user,3600, TimeUnit.SECONDS);
        //返回user对象
        return user;
    }

    //3.数据变更,清除缓存数据
    private void clearCache(int userId){
        //拼接出redisKey
        String redisKey = RedisKeyUtil.getUserKey(userId);
        //直接在Redis缓存中删除redisKey
        redisTemplate.delete(redisKey);
    }

    //根据userId获取用户的权限
    public Collection<? extends GrantedAuthority> getAuthorities(int userId){
        User user = this.findUserById(userId);
        List<GrantedAuthority> list = new ArrayList<>();
        list.add(new GrantedAuthority() {
            @Override
            public String getAuthority() {
                switch (user.getType()){
                    case 1:
                        return AUTHORITY_ADMIN;
                    case 2:
                        return AUTHORITY_MODERATOR;
                    default:
                        return AUTHORITY_USER;
                }
            }
        });
        return list;
    }
}
