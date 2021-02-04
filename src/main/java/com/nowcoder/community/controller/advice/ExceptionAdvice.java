/**
 * FileName: ExceptionAdvice
 * Author:   XueZhenLonG
 * Date:     2021/2/2 18:06
 * Description:
 */
package com.nowcoder.community.controller.advice;

import com.nowcoder.community.util.CommunityUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * 〈〉
 *
 * @author XueZhenLonG
 * @create 2021/2/2
 * @since 1.0.0
 */
//处理所有的错误情况,扫描所有带有Controller注解的类
@ControllerAdvice(annotations = Controller.class)
public class ExceptionAdvice {
    //注入日志组件
    @Autowired
    private static final Logger logger = LoggerFactory.getLogger(ExceptionAdvice.class);

    //表明这是处理所有异常的方法 Exception.class是所有异常的父类 因此...
    @ExceptionHandler({Exception.class})
    public void handleException(Exception e, HttpServletRequest request, HttpServletResponse response) throws IOException {
        logger.error("服务器发生异常:"+ e.getMessage());
        //遍历每一个错误信息
        for (StackTraceElement element: e.getStackTrace()){
            logger.error(element.toString());
        }
        //给浏览器一个响应
        //判断这是一个异步请求还是普通请求 固定的技巧 通过request判断
        String xRequestedWith = request.getHeader("x-requested-with");
        //如果是一个异步请求
        if ("XMLHttpRequest".equals(xRequestedWith)){
            response.setContentType("application/plain;charset=utf-8");
            PrintWriter writer = response.getWriter();
            writer.write(CommunityUtil.getJSONString(1,"服务器异常!"));
        }else {
            response.sendRedirect(request.getContextPath()+"/error");
        }

    }




}
