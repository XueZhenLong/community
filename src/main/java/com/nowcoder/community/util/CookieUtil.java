/**
 * FileName: CookieUtil
 * Author:   XueZhenLonG
 * Date:     2021/1/30 16:16
 * Description:
 */
package com.nowcoder.community.util;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

/**
 * 〈〉
 *
 * @author XueZhenLonG
 * @create 2021/1/30
 * @since 1.0.0
 */
public class CookieUtil {
    //静态方法小工具,根据name返回一
    public static String getValue(HttpServletRequest request,String name){
        if (request ==null || name ==null){
            throw new IllegalArgumentException("参数为空");
        }

        Cookie[] cookies = request.getCookies();
        if (cookies!=null){
            for (Cookie cookie :
                    cookies) {
                if (cookie.getName().equals(name)){
                    return cookie.getValue();
                }
            }
        }

        return null;
    }
}
