/**
 * FileName: LikeService
 * Author:   XueZhenLonG
 * Date:     2021/2/3 16:13
 * Description: 集成redis点赞功能
 */
package com.nowcoder.community.service;

import com.nowcoder.community.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.expression.Operation;
import org.springframework.stereotype.Service;

/**
 * 〈集成redis点赞功能〉
 *
 * @author XueZhenLonG
 * @create 2021/2/3
 * @since 1.0.0
 */
@Service
public class LikeService {

    @Autowired
    private RedisTemplate redisTemplate;

    //点赞
    public void like(int userId, int entityType, int entityId, int entityUserId) {
        //使用事务, 进行重构
        redisTemplate.execute(new SessionCallback() {
            @Override
            public Object execute(RedisOperations redisOperations) throws DataAccessException {
                //拼接 存入redis的数据key
                String entityLikeKey = RedisKeyUtil.getEntityLikeKey(entityType, entityId);
                String userLikeKey = RedisKeyUtil.getUserLikeKey(entityUserId);
                //进行判断,是否点过赞
                boolean isMember = redisOperations.opsForSet().isMember(entityLikeKey,userId);
                //事务的开始
                redisOperations.multi();
                //进行判断
                if (isMember){
                    //如果点过赞,那么执行remove操作,取消点赞信息
                    redisOperations.opsForSet().remove(entityLikeKey,userId);
                    //使点赞数量-1
                    redisOperations.opsForValue().decrement(userLikeKey);
                }else {
                    //如果没有点过赞,那么在rides中增加用户的点赞信息
                    redisOperations.opsForSet().add(entityLikeKey,userId);
                    //使点赞数量+1
                    redisOperations.opsForValue().increment(userLikeKey);
                }
                return redisOperations.exec();
            }
        });
    }

    //查询某实体点赞的数量
    public long findEntityLikeCount(int entityType,int entityId){
        String entityLikeKey = RedisKeyUtil.getEntityLikeKey(entityType, entityId);
        return redisTemplate.opsForSet().size(entityLikeKey);
    }

    //查询某人对某实体的点赞状态
    public int findEntityLikeStatus(int userId,int entityType,int entityId){
        String entityLikeKey = RedisKeyUtil.getEntityLikeKey(entityType, entityId);
        return redisTemplate.opsForSet().isMember(entityLikeKey,userId) ? 1 : 0;
    }

    //查询某个用户获得赞的数量
    public int findUserLikeCount(int userId){
        //拼接 存入redis的数据key
        String userLikeKey = RedisKeyUtil.getUserLikeKey(userId);
        //在redis中进行查询,获得用户获得的点赞数
        Integer count =(Integer)redisTemplate.opsForValue().get(userLikeKey);
        //对返回的count进行判断
        return count == null ? 0 : count.intValue();

    }



}
