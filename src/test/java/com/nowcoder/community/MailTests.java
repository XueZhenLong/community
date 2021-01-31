/**
 * FileName: MailTests
 * Author:   XueZhenLonG
 * Date:     2021/1/29 10:55
 * Description: 测试邮件的发送
 */
package com.nowcoder.community;

import com.nowcoder.community.util.MailClient;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

/**
 * 〈测试邮件的发送〉
 *
 * @author XueZhenLonG
 * @create 2021/1/29
 * @since 1.0.0
 */

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class MailTests {

    @Autowired
    private MailClient mailClient;

    @Autowired
    private TemplateEngine templateEngine;

    @Test
    public void testTextMail(){
        //邮件内容的发送
        mailClient.sendMail("2502486423@qq.com","Send Mail Test","我是快乐的小小薛");
    }

    @Test
    public void testHtmlMail(){
        Context context = new Context();
        context.setVariable("username","快乐小小薛");
        String content = templateEngine.process("/mail/demo", context);
        System.out.println(content);


        //执行邮件的发送
        mailClient.sendMail("2502486423@qq.com","Send HTMl Test",content);
    }

}
