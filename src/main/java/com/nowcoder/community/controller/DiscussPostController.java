/**
 * FileName: DiscussPostController
 * Author:   XueZhenLonG
 * Date:     2021/1/31 18:06
 * Description: 对于帖子文章进行处理
 */
package com.nowcoder.community.controller;

import com.nowcoder.community.entity.Comment;
import com.nowcoder.community.entity.DiscussPost;
import com.nowcoder.community.entity.Page;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.service.CommentService;
import com.nowcoder.community.service.DiscussPostService;
import com.nowcoder.community.service.LikeService;
import com.nowcoder.community.service.UserService;
import com.nowcoder.community.util.CommunityConstant;
import com.nowcoder.community.util.CommunityUtil;
import com.nowcoder.community.util.HostHolder;
import jdk.nashorn.internal.codegen.CompileUnit;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.*;

/**
 * 〈对于帖子(文章)进行处理的控制器〉
 *
 * @author XueZhenLonG
 * @create 2021/1/31
 * @since 1.0.0
 */
@Controller
@RequestMapping("/discuss")
public class DiscussPostController implements CommunityConstant {
    //注入user的Service
    @Autowired
    private UserService userService;

    //注入操作帖子的Service
    @Autowired
    private DiscussPostService discussPostService;

    //注入当前的用户 用于绑定用户发送的帖子
    @Autowired
    private HostHolder hostHolder;

    //注入帖子的评论业务
    @Autowired
    private CommentService commentService;

    //注入点赞
    @Autowired
    private LikeService likeService;

    //对于帖子的增加操作
    @RequestMapping(path = "/add", method = RequestMethod.POST)
    @ResponseBody
    public String addDiscussPost(String title, String content) {
        //获取当前的用户
        User user = hostHolder.getUser();
        //判断当前是否为登录状态
        if (user == null) {
            return CommunityUtil.getJSONString(403, "您还没有登录,请您先登录!");
        }
        //创建帖子的实体对象,对内容进行封装
        DiscussPost post = new DiscussPost();
        post.setUserId(user.getId());
        post.setContent(content);
        post.setTitle(title);
        post.setCreateTime(new Date());

        discussPostService.addDiscussPost(post);
        //报错的情况,将来统一处理.
        return CommunityUtil.getJSONString(0, "发布成功!");
    }

    @RequestMapping(path = "/detail/{discussPostId}", method = RequestMethod.GET)
    public String getDiscussPost(@PathVariable("discussPostId") int discussPostId, Model model, Page page) {
        //查询帖子
        DiscussPost post = discussPostService.findDiscussPostById(discussPostId);
        //获取文章
        model.addAttribute("post", post);
        //获取文章作者的名字
        int userId = post.getUserId();
        User user = userService.findUserById(userId);
        model.addAttribute("user", user);
        //点赞数量,当前文章的点赞数量
        Long likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_POST, discussPostId);
        model.addAttribute("likeCount", likeCount);
        //点赞状态,当前用户是否对这片文章点过赞. 如果未登录怎返回0
        int likeStatus = hostHolder.getUser() == null ? 0 :
                likeService.findEntityLikeStatus(hostHolder.getUser().getId(), ENTITY_TYPE_POST, discussPostId);
        model.addAttribute("likeStatus", likeStatus);

        //查询评论的分页信息
        //每页显示五条
        page.setLimit(5);
        //设置路径
        page.setPath("/discuss/detail/" + discussPostId);
        //评论的总条数
        page.setRows(post.getCommentCount());
        //评论:给帖子的评论
        //回复:给评论得评论
        //评论列表
        List<Comment> commentList = commentService.findCommentsByEntity
                (ENTITY_TYPE_POST, post.getId(), page.getOffset(), page.getLimit());
        //当我们显示评论的时候,也同时显示发表评论的用户
        //评论VO列表
        List<Map<String, Object>> commentVoList = new ArrayList<>();
        if (commentList != null) {
            for (Comment comment : commentList) {
                //一个评论Vo
                Map<String, Object> commentVo = new HashMap<>();
                //评论
                commentVo.put("comment", comment);
                //作者
                //点赞数量,当前文章的点赞数量
                likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_COMMENT, comment.getId());
                commentVo.put("likeCount", likeCount);
                //点赞状态,当前用户是否对这片文章点过赞. 如果未登录怎返回0
                likeStatus = hostHolder.getUser() == null ? 0 :
                        likeService.findEntityLikeStatus(hostHolder.getUser().getId(), ENTITY_TYPE_COMMENT, comment.getId());
                commentVo.put("likeStatus", likeStatus);
                commentVo.put("user", userService.findUserById(comment.getUserId()));
                //回复列表
                List<Comment> replyList = commentService.findCommentsByEntity(
                        ENTITY_TYPE_COMMENT, comment.getId(), 0, Integer.MAX_VALUE);
                //回复vo列表
                List<Map<String, Object>> replyVoList = new ArrayList<>();
                if (replyList != null) {
                    for (Comment reply : replyList) {
                        Map<String, Object> replyVo = new HashMap<>();
                        //回复
                        replyVo.put("reply", reply);
                        //作者
                        replyVo.put("user", userService.findUserById(reply.getUserId()));
                        //回复的目标
                        User target = reply.getTargetId() == 0 ?
                                null : userService.findUserById(reply.getTargetId());
                        replyVo.put("target", target);
                        //点赞数量,当前文章的点赞数量
                        likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_COMMENT, reply.getId());
                        replyVo.put("likeCount_r", likeCount);
                        //点赞状态,当前用户是否对这片文章点过赞. 如果未登录怎返回0
                        likeStatus = hostHolder.getUser() == null ? 0 :
                                likeService.findEntityLikeStatus(hostHolder.getUser().getId(), ENTITY_TYPE_COMMENT, reply.getId());
                        replyVo.put("likeStatus_r", likeStatus);
                        //装入集合中
                        replyVoList.add(replyVo);
                    }
                }
                //把回复装到返回的评论的map中
                commentVo.put("replys", replyVoList);

                //回复的数量
                int replyCount = commentService.findCommentCount(ENTITY_TYPE_COMMENT, comment.getId());
                commentVo.put("replyCount", replyCount);

                commentVoList.add(commentVo);
            }
        }


        model.addAttribute("comments", commentVoList);
        return "/site/discuss-detail";

    }


}
