/**
 * FileName: ServiceLogAspect
 * Author:   XueZhenLonG
 * Date:     2021/2/2 20:12
 * Description: ServiceLogAspect
 */
package com.nowcoder.community.aspect;

import org.aopalliance.intercept.Joinpoint;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 〈ServiceLogAspect〉
 *
 * @author XueZhenLonG
 * @create 2021/2/2
 * @since 1.0.0
 */
@Component
@Aspect
public class ServiceLogAspect {

    private static final Logger logger = LoggerFactory.getLogger(ServiceLogAspect.class);
    //定义切点
    @Pointcut("execution(* com.nowcoder.community.service.*.*(..))")
    public void pointcut(){
    }

    @Before("pointcut()")
    public void before(JoinPoint joinPoint){
        //用户[1.2.3.4],在[2021-2-2 20:14:59],访问了[com.nowcoder.community.service.xxx()]
        //获取request对象
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attributes.getRequest();
        String ip = request.getRemoteHost();
        String now = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        //获取运行方法的名字
        String target = joinPoint.getSignature().getDeclaringTypeName() +"."+joinPoint.getSignature().getName();
        //把他们拼接起来
        logger.info(String.format("用户[%s],在[%s],访问了[%s].",ip,now,target));
    }
}
