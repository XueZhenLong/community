/**
 * FileName: DiscussPostController
 * Author:   XueZhenLonG
 * Date:     2021/1/31 18:06
 * Description: 对于帖子文章进行处理
 */
package com.nowcoder.community.controller;

import com.nowcoder.community.entity.DiscussPost;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.service.DiscussPostService;
import com.nowcoder.community.service.UserService;
import com.nowcoder.community.util.CommunityUtil;
import com.nowcoder.community.util.HostHolder;
import jdk.nashorn.internal.codegen.CompileUnit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Date;

/**
 * 〈对于帖子(文章)进行处理的控制器〉
 *
 * @author XueZhenLonG
 * @create 2021/1/31
 * @since 1.0.0
 */
@Controller
@RequestMapping("/discuss")
public class DiscussPostController {
    //注入user的Service
    @Autowired
    private UserService userService;

    //注入操作帖子的Service
    @Autowired
    private DiscussPostService discussPostService;

    //注入当前的用户 用于绑定用户发送的帖子
    @Autowired
    private HostHolder hostHolder;

    //对于帖子的增加操作
    @RequestMapping(path ="/add",method = RequestMethod.POST)
    @ResponseBody
    public String addDiscussPost(String title,String content){
        //获取当前的用户
        User user = hostHolder.getUser();
        //判断当前是否为登录状态
        if (user==null){
            return CommunityUtil.getJSONString(403,"您还没有登录,请您先登录!");
        }
        //创建帖子的实体对象,对内容进行封装
        DiscussPost post = new DiscussPost();
        post.setUserId(user.getId());
        post.setContent(content);
        post.setTitle(title);
        post.setCreateTime(new Date());

        discussPostService.addDiscussPost(post);
        //报错的情况,将来统一处理.
        return CommunityUtil.getJSONString(0,"发布成功!");
    }

    @RequestMapping(path = "/detail/{discussPostId}",method = RequestMethod.GET)
    public String getDiscussPost(@PathVariable("discussPostId")int discussPostId, Model model){
        //查询帖子
        DiscussPost post = discussPostService.findDiscussPostById(discussPostId);
        //获取文章
        model.addAttribute("post",post);
        //获取文章作者的名字
        int userId = post.getUserId();
        User user = userService.findUserById(userId);
        model.addAttribute("user",user);

        return "/site/discuss-detail";


    }

}
