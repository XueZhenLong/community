/**
 * FileName: CommunityConstant
 * Author:   XueZhenLonG
 * Date:     2021/1/29 18:33
 * Description: 处理注册激活功能的接口
 */
package com.nowcoder.community.util;

/**
 * 〈处理注册激活功能的接口〉
 *
 * @author XueZhenLonG
 * @create 2021/1/29
 * @since 1.0.0
 */
public interface CommunityConstant {

    /**
     * 几个激活的状态
     */

    //激活成功
    int ACTIVATION_SUCCESS = 0;

    //重复激活
    int ACTIVATION_REPEAT = 1;

    //激活失败
    int ACTIVATION_FAILURE = 2;

    /**
     * 默认状态的登录状态凭证的超时时间
     */
    int DEFAULT_EXPIRED_SECONDS = 3600 * 12; //十二个小时

    /**
     * 记住状态的登录凭证超时的时间
     */
    int REMEMBER_EXPIRED_SECONDS = 3600 * 24 * 7;//七天

    /**
     * 实体类型:帖子
     */
    int ENTITY_TYPE_POST = 1;
    /**
     * 实体类型:评论
     */
    int ENTITY_TYPE_COMMENT = 2;
    /**
     * 实体类型: 人
     */
    int ENTITY_TYPE_USER = 3;

    /**
     * 事件主题:评论
     */
    String TOPIC_COMMENT = "comment";
    /**
     * 事件主题:点赞
     */
    String TOPIC_LIKE = "like";
    /**
     * 事件主题:关注
     */
    String TOPIC_FOLLOW = "follow";

    /**
     * 系统用户的id
     */
    int SYSTEM_USER_ID = 1;

    /**
     * 主题:发帖
     */
    String TOPIC_PUBLISH = "publish";

    /**
     * 主题:删帖
     */
    String TOPIC_DELETE = "delete";

    /**
     * 主题:分享
     */
    String TOPIC_SHARE = "share";

    /**
     * 用于spring security的权限常量
     * 权限:普通用户
     */
    String AUTHORITY_USER = "user";


    /**
     * 用于spring security的权限常量
     * 权限:管理员
     */
    String AUTHORITY_ADMIN = "admin";


    /**
     * 用于spring security的权限常量
     * 权限:版主
     */
    String AUTHORITY_MODERATOR = "moderator";


}
