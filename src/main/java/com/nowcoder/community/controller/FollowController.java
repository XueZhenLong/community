/**
 * FileName: FollowController
 * Author:   XueZhenLonG
 * Date:     2021/2/4 15:44
 * Description: 处理用户管理关注功能
 */
package com.nowcoder.community.controller;

import com.nowcoder.community.entity.Event;
import com.nowcoder.community.entity.Page;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.event.EventProducer;
import com.nowcoder.community.service.FollowService;
import com.nowcoder.community.service.UserService;
import com.nowcoder.community.util.CommunityConstant;
import com.nowcoder.community.util.CommunityUtil;
import com.nowcoder.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 〈处理用户管理关注功能〉
 *
 * @author XueZhenLonG
 * @create 2021/2/4
 * @since 1.0.0
 */
@Controller
public class FollowController implements CommunityConstant {
    //注入关注业务
    @Autowired
    private FollowService followService;

    //注入当前用户
    @Autowired
    private HostHolder hostHolder;

    //注入用户关注的业务
    @Autowired
    private UserService userService;

    //注入kafka消息的生产者
    @Autowired
    private EventProducer eventProducer;

    //我们的关注功能是异步的,在我们点击关注的时候,不刷新页面.使用@ResponseBody提交请求 返回JSON给前端
    @RequestMapping(path = "/follow", method = RequestMethod.POST)
    @ResponseBody
    public String follow(int entityType, int entityId){
        //后续的作业,使用拦截器对follow 进行拦截, 要是没有关注,就让用户先登录
        //获取当前用户
        User user = hostHolder.getUser();
        //进行关注
        followService.follow(user.getId(),entityType,entityId);

        //---------------------------------------------
        //关注之后,开始kafka通知的行为
        //触发关注事件 (关注的时候我们才通知用户)
            Event event = new Event()
                    .setTopic(TOPIC_FOLLOW)
                    .setUserId(hostHolder.getUser().getId())
                    .setEntityType(entityType)
                    .setEntityId(entityId)
                    .setEntityUserId(entityId);
            //让生产者开始产生事件
            eventProducer.fireEvent(event);
        //---------------------------------------------

        return CommunityUtil.getJSONString(0,"已关注!");
    }

    @RequestMapping(path = "/unfollow", method = RequestMethod.POST)
    @ResponseBody
    public String unfollow(int entityType, int entityId){
        //后续的作业,使用拦截器对follow 进行拦截, 要是没有关注,就让用户先登录
        //获取当前用户
        User user = hostHolder.getUser();
        //进行关注
        followService.unfollow(user.getId(),entityType,entityId);

        return CommunityUtil.getJSONString(0,"已取消关注!");
    }
    //查询自己关注的人
    @RequestMapping(path = "/followees/{userId}", method = RequestMethod.GET)
    public String getFollowees(@PathVariable("userId")int userId, Page page, Model model){
        User user = userService.findUserById(userId);
        if (user==null){
            throw new RuntimeException("该用户不存在!");
        }
        model.addAttribute("user",user);
        page.setLimit(5);
        page.setPath("/followees/"+userId);
        page.setRows((int) followService.findFolloweeCount(userId,CommunityConstant.ENTITY_TYPE_USER));
        List<Map<String, Object>> userList = followService.findFollowees(userId, page.getOffset(), page.getLimit());
        if (userList !=null){
            for (Map<String,Object> map : userList) {
                User u =(User) map.get("user");
                map.put("hasFollowed",hasFollowed(u.getId()));
            }
        }
        model.addAttribute("users",userList);
        return "/site/followee";
    }

    //查询关注自己的人
    @RequestMapping(path = "/followers/{userId}", method = RequestMethod.GET)
    public String getFollowers(@PathVariable("userId")int userId, Page page, Model model){
        User user = userService.findUserById(userId);
        if (user==null){
            throw new RuntimeException("该用户不存在!");
        }
        model.addAttribute("user",user);
        page.setLimit(5);
        page.setPath("/followers/"+userId);
        page.setRows((int) followService.findFollowerCount(ENTITY_TYPE_USER,userId));
        List<Map<String, Object>> userList = followService.findFollowers(userId, page.getOffset(), page.getLimit());
        if (userList != null){
            for (Map<String,Object> map : userList) {
                User u =(User) map.get("user");
                map.put("hasFollowed",hasFollowed(u.getId()));
            }
        }
        model.addAttribute("users",userList);
        return "/site/follower";
    }


    private boolean hasFollowed(int userId){
        if (hostHolder.getUser() == null){
            return false;
        }
        return followService.hasFollowed(hostHolder.getUser().getId(),userId,ENTITY_TYPE_USER);
    }





}
