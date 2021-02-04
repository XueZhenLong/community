/**
 * FileName: RedisConfig
 * Author:   XueZhenLonG
 * Date:     2021/2/3 14:48
 * Description: 配置redis的配置类
 */
package com.nowcoder.community.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.TooManyClusterRedirectionsException;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;

/**
 * 〈配置redis的配置类〉
 *
 * @author XueZhenLonG
 * @create 2021/2/3
 * @since 1.0.0
 */
@Configuration
public class RedisConfig {

    @Bean
    public RedisTemplate<String,Object> redisTemplate(RedisConnectionFactory factory){
        //首先注入连接工厂,才能访问数据库
        //实例化
        RedisTemplate<String,Object> template = new RedisTemplate<>();
        //使其具有访问数据库的能力
        template.setConnectionFactory(factory);

        //配置序列化的方式(数据访问的方式)
        //设置key的序列化方式
        template.setKeySerializer(RedisSerializer.string());
        //设置value的序列化方式
        template.setValueSerializer(RedisSerializer.json());
        //设置hash的key的序列化方式
        template.setHashKeySerializer(RedisSerializer.string());
        //设置hash的value的序列化方式
        template.setHashValueSerializer(RedisSerializer.json());
        //设置生效
        template.afterPropertiesSet();
        return template;

    }

}
