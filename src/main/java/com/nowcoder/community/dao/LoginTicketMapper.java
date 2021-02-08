/**
 * FileName: LoginTicketMapper
 * Author:   XueZhenLonG
 * Date:     2021/1/30 13:08
 * Description: 用于用户登录
 */
package com.nowcoder.community.dao;

import com.nowcoder.community.entity.LoginTicket;
import org.apache.ibatis.annotations.*;
import org.apache.ibatis.annotations.Mapper;

/**
 * 〈用于用户登录〉
 *
 * @author XueZhenLonG
 * @create 2021/1/30
 * @since 1.0.0
 */
@Mapper
@Deprecated //表示 这个不推荐使用了.
public interface LoginTicketMapper{


    //增加数据,插入一个凭证
    @Insert({
            "insert into login_ticket(user_id,ticket,status,expired) ",
            "values(#{userId},#{ticket},#{status},#{expired})"
    })
    @Options(useGeneratedKeys = true,keyProperty = "id")
    int insertLoginTicket(LoginTicket loginTicket);

    //登录验证,通过ticket查询整条数据
    @Select({
            "select id,user_id,ticket,status,expired ",
            "from login_ticket where ticket=#{ticket}"
    })
    LoginTicket selectByTicket(String ticket);

    //改变loginTicket的状态 练习动态sql
    @Update({
            "<script>",
            "update login_ticket set status=#{status} where ticket=#{ticket} ",
            "<if test=\"ticket!=null\"> ",
            "and 1=1 ",
            "</if>",
            "</script>"
    })
    int updateStatus(String ticket, int status);

}
