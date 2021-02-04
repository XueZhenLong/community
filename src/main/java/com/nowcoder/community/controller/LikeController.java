/**
 * FileName: LikeController
 * Author:   XueZhenLonG
 * Date:     2021/2/3 16:31
 * Description:
 */
package com.nowcoder.community.controller;

import com.nowcoder.community.entity.User;
import com.nowcoder.community.service.LikeService;
import com.nowcoder.community.util.CommunityUtil;
import com.nowcoder.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
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
public class LikeController {

    @Autowired
    private LikeService likeService;

    @Autowired
    private HostHolder hostHolder;

    @RequestMapping(path = "/like",method = RequestMethod.POST)
    @ResponseBody
    public String like(int entityType, int entityId, int entityUserId){
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
        //把封装好的数据使用JSon格式返回
        return CommunityUtil.getJSONString(0,null,map);
    }

}
