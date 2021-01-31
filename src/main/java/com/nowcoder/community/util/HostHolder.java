/**
 * FileName: HostHolder
 * Author:   XueZhenLonG
 * Date:     2021/1/30 16:31
 * Description:
 */
package com.nowcoder.community.util;

import com.nowcoder.community.entity.User;
import org.springframework.stereotype.Component;

/**
 * 〈持有用户的信息,用于代替session对象〉
 *
 * @author XueZhenLonG
 * @create 2021/1/30
 * @since 1.0.0
 */
@Component
public class HostHolder {
    //ThreadLocal用于实现线程的隔离
    private ThreadLocal<User> users = new ThreadLocal<>();

    public void setUser(User user){
        users.set(user);
    }

    public User getUser(){
        return users.get();
    }

    public void clear(){
        users.remove();
    }

}
