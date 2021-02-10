/**
 * FileName: LikeController
 * Author:   XueZhenLonG
 * Date:     2021/2/3 16:31
 * Description:
 */
package com.nowcoder.community.controller;

import com.nowcoder.community.entity.Comment;
import com.nowcoder.community.entity.DiscussPost;
import com.nowcoder.community.entity.Event;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.event.EventProducer;
import com.nowcoder.community.service.LikeService;
import com.nowcoder.community.util.CommunityConstant;
import com.nowcoder.community.util.CommunityUtil;
import com.nowcoder.community.util.HostHolder;
import com.nowcoder.community.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.Map;

/**
 * 〈〉
 *
 * @author XueZhenLonG
 * @create 2021/2/3
 * @since 1.0.0
 */
@Controller
public class LikeController implements CommunityConstant {

    @Autowired
    private LikeService likeService;

    @Autowired
    private HostHolder hostHolder;

    //注入kafka消息的生产者
    @Autowired
    private EventProducer eventProducer;

    //注入RedisTemplate,用于热帖排行,把帖子的id存储到redis中
    @Autowired
    private RedisTemplate redisTemplate;

    @RequestMapping(path = "/like",method = RequestMethod.POST)
    @ResponseBody
    public String like(int entityType, int entityId, int entityUserId,int postId){
        //先获取当前用户
        User user = hostHolder.getUser();
        //点赞
        likeService.like(user.getId(),entityType,entityId,entityUserId);
        //数量
        long likeCount = likeService.findEntityLikeCount(entityType,entityId);
        //状态
        int LikeStatus = likeService.findEntityLikeStatus(user.getId(),entityType,entityId);
        //将数量和状态使用Map进行封装,返回给前端
        Map<String,Object> map = new HashMap<>();
        map.put("likeCount",likeCount);
        map.put("likeStatus",LikeStatus);

        //---------------------------------------------
        //点赞评论之后,开始kafka通知的行为
        //触发点赞事件 (点赞的时候我们才通知用户)
        if (LikeStatus == 1){
            Event event = new Event()
                    .setTopic(TOPIC_LIKE)
                    .setUserId(hostHolder.getUser().getId())
                    .setEntityType(entityType)
                    .setEntityId(entityId)
                    .setEntityUserId(entityUserId)
                    .setData("postId",postId);
            //让生产者开始产生事件
            eventProducer.fireEvent(event);
        }
        //---------------------------------------------

        if (entityType == ENTITY_TYPE_POST){
            //---------------------------------------------------------------
            //用于热帖排序,我们发布的时候计算帖子的分数
            String redisKey = RedisKeyUtil.getPostScoreKey();
            //我们需要去重,有序 所以采用Set结构进行存储
            redisTemplate.opsForSet().add(redisKey,postId);
        }

        //把封装好的数据使用JSon格式返回
        return CommunityUtil.getJSONString(0,null,map);
    }

}
