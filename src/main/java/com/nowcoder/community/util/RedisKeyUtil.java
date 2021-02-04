/**
 * FileName: RedisKeyUtil
 * Author:   XueZhenLonG
 * Date:     2021/2/3 15:57
 * Description:
 */
package com.nowcoder.community.util;

/**
 * 〈〉
 *
 * @author XueZhenLonG
 * @create 2021/2/3
 * @since 1.0.0
 */
public class RedisKeyUtil {
    private static final String SPLIT = ":";
    //文章的赞
    private static final String PREFIX_ENTITY_LIKE = "like:entity";
    //用户的赞
    private static final String PREFIX_USER_LIKE = "like:user";
    //粉丝->follower 目标->followee 举例 A是B的粉丝,A的目标是B
    private static final String PREFIX_FOLLOWEE = "followee";
    private static final String PREFIX_FOLLOWER = "follower";

    //某个实体的赞
    //like:entity:entityType:entityId ->set(userId)
    public static String getEntityLikeKey(int entityType, int entityId) {
        return PREFIX_ENTITY_LIKE + entityType + SPLIT + entityId;
    }

    //某个用户的赞
    //like:user:userId ->int
    public static String getUserLikeKey(int userId){
        return PREFIX_USER_LIKE + SPLIT + userId;
    }

    //某个用户关注的实体 用户和实体的关系
    // zset 是redis中的有序集合 now 以当前时间作为分数进行排序
    // followee:userId:entityType -> zset(entityId,now)
    public static String getFolloweeKey(int userId, int entityType){
        return PREFIX_FOLLOWEE + SPLIT + userId + SPLIT + entityType;
    }

    //某个实体拥有的粉丝
    //follower:entityType:entityId ->zset(userId,now)
    public static String getFollowerKey(int entityType, int entityId){
        return PREFIX_FOLLOWER + SPLIT + entityType + SPLIT + entityId;
    }


}
