package com.nowcoder.community;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.annotation.PostConstruct;

@MapperScan("com.nowcoder.community.dao")
@SpringBootApplication
public class CommunityApplication {
    //管理bean的生命周期,用来解决elasticsearch 与 redis 底层的netty冲突
    @PostConstruct
    public void init(){
        //解决netty启动冲突的问题
        //底层请看看NettyUtils.setAvailableProcessors()
        System.setProperty("es.set.netty.runtime.available.processors","false");
    }

    public static void main(String[] args) {
        SpringApplication.run(CommunityApplication.class, args);
    }


}
