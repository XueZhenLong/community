/**
 * FileName: AlphaAspect
 * Author:   XueZhenLonG
 * Date:     2021/2/2 20:00
 * Description: 统一日志管理-AOP的小示例
 */
package com.nowcoder.community.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.stereotype.Component;

/**
 * 〈统一日志管理-AOP的小示例〉
 *
 * @author XueZhenLonG
 * @create 2021/2/2
 * @since 1.0.0
 */
//@Component
//@Aspect
public class AlphaAspect {
    //定义切点
    @Pointcut("execution(* com.nowcoder.community.service.*.*(..))")
    public void pointcut(){

    }

    //定义通知
    //前置
    @Before("pointcut()")
    public void before(){
        System.out.println("before");
    }
    //后置
    @After("pointcut()")
    public void after(){
        System.out.println("after");
    }
    //有了返回值以后
    @AfterReturning("pointcut()")
    public void afterReturning(){
        System.out.println("afterReturning");
    }
    //在抛出异常的时候
    @AfterThrowing("pointcut()")
    public void afterThrowing(){
        System.out.println("afterThrowing");
    }
    //在切入点的前后都织入
    @Around("pointcut()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable{
        System.out.println("around before");
        //调用目标组件的方法
        Object obj = joinPoint.proceed();
        System.out.println("around after");
        return obj;

    }


}
