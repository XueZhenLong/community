/**
 * FileName: UserController
 * Author:   XueZhenLonG
 * Date:     2021/1/30 18:08
 * Description: 用于用户的账号设置
 */
package com.nowcoder.community.controller;

import com.nowcoder.community.annotation.LoginRequired;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.service.FollowService;
import com.nowcoder.community.service.LikeService;
import com.nowcoder.community.service.UserService;
import com.nowcoder.community.util.CommunityConstant;
import com.nowcoder.community.util.CommunityUtil;
import com.nowcoder.community.util.HostHolder;
import com.nowcoder.community.util.MailClient;
import javafx.scene.web.WebHistory;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * 〈用于用户的账号设置〉
 *
 * @author XueZhenLonG
 * @create 2021/1/30
 * @since 1.0.0
 */
@Controller
@RequestMapping("/user")
public class UserController implements CommunityConstant {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    //接收在application.properties中配置的文件存储路径的值
    @Value("${community.path.upload}")
    public String uploadPath;

    //接收在application.properties中配置的服务器域名
    @Value("${community.path.domain}")
    public String domain;

    //接收在application.properties中配置的项目的访问路径
    @Value("${server.servlet.context-path}")
    public String contextPath;

    //注入UserService 用于进行业务上操作
    @Autowired
    private UserService userService;
    //注入HostHolder 获得当前用户
    @Autowired
    private HostHolder hostHolder;
    //注入点赞功能
    @Autowired
    private LikeService likeService;
    //注入关注功能
    @Autowired
    private FollowService followService;

    //跳转设置页面请求路径
    @LoginRequired
    @RequestMapping(path = "/setting", method = RequestMethod.GET)
    public String getSettingPage() {
        return "/site/setting";
    }

    //进行文件的上传 提交功能
    @LoginRequired
    @RequestMapping(path = "/upload", method = RequestMethod.POST)
    public String uploadHeader(MultipartFile headerImage, Model model) {
        if (headerImage == null) {
            model.addAttribute("error", "您还没有选择图片!");
            return "/site/setting";
        }
        //读取文件的后缀
        //获取文件的全名
        String filename = headerImage.getOriginalFilename();
        //通过截取filename的子字符串,或者文件的后缀
        String suffix = filename.substring(filename.lastIndexOf("."));
        if (StringUtils.isBlank(suffix)) {
            model.addAttribute("error", "文件的格式不正确!");
            return "/site/setting";
        }
        //生成随机的文件名
        filename = CommunityUtil.generateUUID() + suffix;
        //确定文件存放的路径
        File dest = new File(uploadPath + "/" + filename);
        try {
            //进行文件的存储
            headerImage.transferTo(dest);
        } catch (IOException e) {
            logger.error("上传文件失败:" + e.getMessage());
            throw new RuntimeException("上传文件失败,服务器发生异常!", e);
        }

        //更新当前用户的头像的路径(web访问路径)
        //http://localhost:8080/community/user/header/xxx.png
        //获取用户
        User user = hostHolder.getUser();
        //拼接图片的url
        String headerUrl = domain + contextPath + "/user/header/" + filename;
        //更新数据库中的headerurl信息
        userService.updateHeader(user.getId(), headerUrl);
        //成功了! 那么我们就重定向返回首页.
        return "redirect:/index";
    }


    //获取头像的请求
    @RequestMapping(path = "header/{fileName}", method = RequestMethod.GET)
    public void getHeader(@PathVariable("fileName") String fileName, HttpServletResponse response) {
        //服务器存放的路径
        fileName = uploadPath + "/" + fileName;
        //解析fileName的后缀
        String suffix = fileName.substring(fileName.lastIndexOf("."));
        //响应图片
        response.setContentType("image/" + suffix);
        try(
                FileInputStream fis = new FileInputStream(fileName);
                ServletOutputStream os = response.getOutputStream();
                ) {
            byte[] buffer = new byte[1024];
            int b = 0;
            while ((b=fis.read(buffer))!= -1){
                os.write(buffer,0,b);
            }
        } catch (IOException e) {
            logger.error("读取头像失败:" + e);
        }
    }

    //修改密码
    @RequestMapping(path = "/change",method = RequestMethod.POST)
    public String changePassword(Model model,@RequestParam("old_password")String old_password,@RequestParam("new_password")String new_password,@RequestParam("confirm_password")String confirm_password){

        if (old_password==null || new_password==null ||confirm_password==null){
            model.addAttribute("changeMsg","不可以为空!");
            return "/site/setting";
        }
        if (!new_password.equals(confirm_password)){
            model.addAttribute("changeMsg","两次输入的密码不一致!");
            return "/site/setting";
        }else {
            User user = hostHolder.getUser();
            String password_user = user.getPassword();
            String s = CommunityUtil.md5(old_password + user.getSalt());
            if (password_user.equals(s)){
                int id = user.getId();
                String s1 = CommunityUtil.md5(new_password + user.getSalt());
                userService.changePassword(id,s1);
                model.addAttribute("changeMsg","恭喜您修改成功!");
                return "redirect:/index";
            }else{
                model.addAttribute("changeMsg_1","原来的密码输入错啦!");
                return "/site/setting";
            }
        }
    }

    //个人主页
    @RequestMapping(path = "/profile/{userId}",method = RequestMethod.GET)
    public String getProfilePage(@PathVariable("userId") int userId,Model model){
        //首先我们把要访问的用户查询出来
        User user = userService.findUserById(userId);
        //判断一下 如果不存在的话,抛异常
        if (user==null){
            throw new RuntimeException("该用户不存在!");
        }
        //把用户的信息传给前端.
        model.addAttribute("user",user);
        //用户点赞的数量,并传递给前端
        int userLikeCount = likeService.findUserLikeCount(userId);
        model.addAttribute("likeCount",userLikeCount);

        //用户关注数量
        long followeeCount = followService.findFolloweeCount(userId, ENTITY_TYPE_USER);
        model.addAttribute("followeeCount",followeeCount);
        //粉丝数量
        long followerCount = followService.findFollowerCount(ENTITY_TYPE_USER, userId);
        model.addAttribute("followerCount",followerCount);
        //是否关注
        boolean hasFollowed = false;
        //判断用户是否登录
        if (hostHolder.getUser()!= null){
            hasFollowed = followService.hasFollowed(hostHolder.getUser().getId(),userId,ENTITY_TYPE_USER);
        }
        model.addAttribute("hasFollowed",hasFollowed);
        return "/site/profile";
    }


}
