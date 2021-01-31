/**
 * FileName: CommunityUtil
 * Author:   XueZhenLonG
 * Date:     2021/1/29 15:36
 * Description: 工具类 用于注册的方法
 */
package com.nowcoder.community.util;

import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.DigestUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * 〈工具类 用于注册的方法〉
 *
 * @author XueZhenLonG
 * @create 2021/1/29
 * @since 1.0.0
 */
public class CommunityUtil {

    //生成一个随机的字符串
    public static String generateUUID(){
        return UUID.randomUUID().toString().replaceAll("-","");
    }


    //MD5加密
    //只能加密 不可以解密
    //hello -> abc123def456
    //hello+3sf34a ->abc123def3456abc
    public static String md5(String key){

        if (StringUtils.isBlank(key)){
            return null;
        }
        //使用spring封装的md5加密方法
        return DigestUtils.md5DigestAsHex(key.getBytes());
    }


    //用于处理发布帖子的方法
    //利用了fastjson使用AJAX技术
    //code 是返回个服务器的状态码  msg给前端的反馈消息 例如:发布成功!
    public static String getJSONString(int code, String msg, Map<String,Object> map){
        //使用fastjson提供的方法创建
        JSONObject json = new JSONObject();
        json.put("code",code);
        json.put("msg",msg);
        if (map!=null){
            for (String key :
                    map.keySet()) {
                json.put(key, map.get(key));
            }
        }
        return json.toJSONString();
    }
    public static String getJSONString(int code,String msg){
        return getJSONString(code,msg,null);
    }
    public static String getJSONString(int code){
            return getJSONString(code,null,null);
    }

    public static void main(String[] args) {
        //测试上面的方法
        Map<String, Object> map = new HashMap<>();
        map.put("name","zhangsan");
        map.put("age",25);
        System.out.println(getJSONString(0,"ok",map));

    }



}
