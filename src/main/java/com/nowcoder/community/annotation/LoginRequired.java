package com.nowcoder.community.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 自定义注解,实现检查登录状态 权限 后续的拦截在拦截器中实现
 */
@Target(ElementType.METHOD) //注解可以应用的类型 : 方法
@Retention(RetentionPolicy.RUNTIME)//注解生生效的时间 : 运行时有效
public @interface LoginRequired {

}
