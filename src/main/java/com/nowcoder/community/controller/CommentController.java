/**
 * FileName: CommentController
 * Author:   XueZhenLonG
 * Date:     2021/2/1 17:47
 * Description: 对于帖子中评论操作
 */
package com.nowcoder.community.controller;

import com.nowcoder.community.entity.Comment;
import com.nowcoder.community.entity.DiscussPost;
import com.nowcoder.community.entity.Event;
import com.nowcoder.community.event.EventProducer;
import com.nowcoder.community.service.CommentService;
import com.nowcoder.community.service.DiscussPostService;
import com.nowcoder.community.util.CommunityConstant;
import com.nowcoder.community.util.HostHolder;
import org.apache.catalina.Host;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.xml.crypto.Data;
import java.util.Date;

/**
 * 〈对于帖子中评论操作〉
 *
 * @author XueZhenLonG
 * @create 2021/2/1
 * @since 1.0.0
 */
@Controller
@RequestMapping("/comment")
public class CommentController implements CommunityConstant {

    @Autowired
    private CommentService commentService;

    @Autowired
    private HostHolder hostHolder;

    //注入kafka消息的生产者
    @Autowired
    private EventProducer eventProducer;

    //注入帖子的Service, 用于查询帖子id
    @Autowired
    private DiscussPostService discussPostService;

    /**
     * 添加对于帖子,评论 的评论
     * @param discussPostId
     * @param comment
     * @return
     */
    @RequestMapping(path = "/add/{discussPostId}", method = RequestMethod.POST)
    public String addComment(@PathVariable("discussPostId") int discussPostId, Comment comment) {
        comment.setUserId(hostHolder.getUser().getId());
        comment.setStatus(0);
        comment.setCreatTime(new Date());
        commentService.addComment(comment);
        //---------------------------------------------
        //添加评论之后,开始kafka通知的行为
        Event event = new Event()
                .setTopic(TOPIC_COMMENT)
                .setUserId(hostHolder.getUser().getId())
                .setEntityType(comment.getEntityType())
                .setEntityId(comment.getEntityId())
                .setData("postId", discussPostId);
        //判断一下,comment的类型
        //我们评论的时候有种类型, 帖子,评论
        if (comment.getEntityType() == ENTITY_TYPE_POST) {
            //评论的实体的id,即评论的目标
            DiscussPost target = discussPostService.findDiscussPostById(comment.getEntityId());
            event.setEntityUserId(target.getUserId());
        } else if (comment.getEntityType() == ENTITY_TYPE_COMMENT) {
            //评论的实体的id,即评论的目标
            Comment target = commentService.findCommentById(comment.getEntityId());
            event.setEntityUserId(target.getUserId());
        }
        //调用生产者,去处理事件
        eventProducer.fireEvent(event);
        //---------------------------------------------



        //帖子的评论发布成功后,将新发布的评论 加入到Elasticsearch服务器中
        //---------------------------------------------------------------
        //触发发帖事件
        if(comment.getEntityType() == ENTITY_TYPE_POST){
            event = new Event()
                    .setTopic(TOPIC_PUBLISH)
                    .setUserId(comment.getUserId())
                    .setEntityType(ENTITY_TYPE_POST)
                    .setEntityId(discussPostId);
            //触发事件
            eventProducer.fireEvent(event);
        }
        //---------------------------------------------------------------


        return "redirect:/discuss/detail/" + discussPostId;
    }
}
