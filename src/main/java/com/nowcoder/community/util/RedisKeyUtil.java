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
    //优化项目验证码功能
    private static final String PREFIX_KAPTCHA = "kaptcha";
    //优化项目的登录凭证
    private static final String PREFIX_TICKET = "ticket";
    //优化查询用户信息
    private static final String PREFIX_USER = "user";
    //统计网站访问人数
    private static final String PREFIX_UV = "uv";
    //统计网站日活跃人数
    private static final String PREFIX_DAU = "dau";

    //某个实体的赞
    //like:entity:entityType:entityId ->set(userId)
    public static String getEntityLikeKey(int entityType, int entityId) {
        return PREFIX_ENTITY_LIKE + entityType + SPLIT + entityId;
    }

    //某个用户的赞
    //like:user:userId ->int
    public static String getUserLikeKey(int userId) {
        return PREFIX_USER_LIKE + SPLIT + userId;
    }

    //某个用户关注的实体 用户和实体的关系
    // zset 是redis中的有序集合 now 以当前时间作为分数进行排序
    // followee:userId:entityType -> zset(entityId,now)
    public static String getFolloweeKey(int userId, int entityType) {
        return PREFIX_FOLLOWEE + SPLIT + userId + SPLIT + entityType;
    }

    //某个实体拥有的粉丝
    //follower:entityType:entityId ->zset(userId,now)
    public static String getFollowerKey(int entityType, int entityId) {
        return PREFIX_FOLLOWER + SPLIT + entityType + SPLIT + entityId;
    }

    //登录验证码
    public static String getKaptchaKey(String owner) {
        return PREFIX_KAPTCHA + SPLIT + owner;
    }

    //登录的凭证
    public static String getTicketKey(String ticket) {
        return PREFIX_TICKET + SPLIT + ticket;
    }

    //用户信息
    public static String getUserKey(int userId) {
        return PREFIX_TICKET + SPLIT + userId;
    }


    //单日uv
    public static String getUVKey(String date) {
        return PREFIX_UV + SPLIT + date;
    }

    //区间UV 从xx-xxx之间的UV
    public static String getUVKey(String startDate, String endDate) {
        return PREFIX_UV + SPLIT + startDate + SPLIT + endDate;
    }

    //单日活跃用户
    public static String getDAUKey(String date){
        return PREFIX_DAU + SPLIT + date;
    }

    //区间活跃用户 从xx-xxx之间的DAU
    public static String getDAUKey(String startDate, String endDate) {
        return PREFIX_DAU + SPLIT + startDate + SPLIT + endDate;
    }



}
