/**
 * FileName: FollowService
 * Author:   XueZhenLonG
 * Date:     2021/2/4 15:34
 * Description: 处理用户关注功能
 */
package com.nowcoder.community.service;

import com.nowcoder.community.entity.User;
import com.nowcoder.community.util.CommunityConstant;
import com.nowcoder.community.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * 〈处理用户关注功能〉
 *
 * @author XueZhenLonG
 * @create 2021/2/4
 * @since 1.0.0
 */
@Service
public class FollowService implements CommunityConstant {
    //注入Redis工具
    @Autowired
    private RedisTemplate redisTemplate;
    //用于查询用户关注列表
    @Autowired
    private UserService userService;

    //用户的添加关注功能
    public void follow(int userId, int entityType, int entityId) {
        //存的时候,我们要存储两份数据 目标,粉丝. 所以要保证事务
        redisTemplate.execute(new SessionCallback() {
            @Override
            public Object execute(RedisOperations redisOperations) throws DataAccessException {
                //先构造两个redis的key
                String followeeKey = RedisKeyUtil.getFolloweeKey(userId, entityType);
                String followerKey = RedisKeyUtil.getFollowerKey(entityType, entityId);
                //启用事务
                redisOperations.multi();
                //添加操作
                redisOperations.opsForZSet().add(followeeKey,entityId,System.currentTimeMillis());
                redisOperations.opsForZSet().add(followerKey,userId, System.currentTimeMillis());

                //提交事务
                return redisOperations.exec();
            }
        });
    }

    //用户的取消关注功能
    public void unfollow(int userId, int entityType, int entityId) {
        //存的时候,我们要存储两份数据 目标,粉丝. 所以要保证事务
        redisTemplate.execute(new SessionCallback() {
            @Override
            public Object execute(RedisOperations redisOperations) throws DataAccessException {
                //先构造两个redis的key
                String followeeKey = RedisKeyUtil.getFolloweeKey(userId, entityType);
                String followerKey = RedisKeyUtil.getFollowerKey(entityType, entityId);
                //启用事务
                redisOperations.multi();
                //删除操作
                redisOperations.opsForZSet().remove(followeeKey,entityId);
                redisOperations.opsForZSet().remove(followerKey,userId);

                //提交事务
                return redisOperations.exec();
            }
        });
    }


    //查询关注的实体的数量
    public long findFolloweeCount(int userId, int entityType){
        String followeeKey = RedisKeyUtil.getFolloweeKey(userId,entityType);
        return redisTemplate.opsForZSet().zCard(followeeKey);
    }

    //查询实体的粉丝的数量
    public long findFollowerCount(int entityType, int entityId){
        String followerKey = RedisKeyUtil.getFollowerKey(entityType, entityId);
        return redisTemplate.opsForZSet().zCard(followerKey);
    }

    //查询当前用户是否已经关注当前实体
    public boolean hasFollowed(int userId,int entityId,int entityType){
        String followeeKey = RedisKeyUtil.getFolloweeKey(userId, entityType);
        //查询是否有这个key
        return redisTemplate.opsForZSet().score(followeeKey,entityId) !=null;
    }

    //查询某用户关注的人
    public List<Map<String,Object>> findFollowees(int userId, int offset, int limit){
        //只查询人
        String followeeKey = RedisKeyUtil.getFolloweeKey(userId, ENTITY_TYPE_USER);
        //在Redis中查询 返回来的是有序的集合
        Set<Integer> targetIds = redisTemplate.opsForZSet().reverseRange(followeeKey, offset, offset + limit - 1);
        //判断targetIds
        if (targetIds == null) {
            return null;
        }
        ArrayList<Map<String, Object>> list = new ArrayList<>();
        for (Integer targetId :
                targetIds) {
            HashMap<String, Object> map = new HashMap<>();
            //通过用户的id,确定关注的人
            User user = userService.findUserById(targetId);
            map.put("user",user);
            //查询关注的时间
            Double score = redisTemplate.opsForZSet().score(followeeKey, targetId);
            map.put("followTime",new Date(score.longValue()));
            //把封装好的map放入list中
            list.add(map);
        }
        return list;
    }
    //查询某用户的粉丝
    public List<Map<String, Object>> findFollowers(int userId, int offset, int limit) {
        String followerKey = RedisKeyUtil.getFollowerKey(ENTITY_TYPE_USER, userId);
        Set<Integer> targetIds = redisTemplate.opsForZSet().reverseRange(followerKey, offset, offset + limit - 1);

        if (targetIds == null) {
            return null;
        }

        List<Map<String, Object>> list = new ArrayList<>();
        for (Integer targetId : targetIds) {
            Map<String, Object> map = new HashMap<>();
            User user = userService.findUserById(targetId);
            map.put("user", user);
            Double score = redisTemplate.opsForZSet().score(followerKey, targetId);
            map.put("followTime", new Date(score.longValue()));
            list.add(map);
        }

        return list;
    }

}
