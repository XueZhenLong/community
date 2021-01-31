/**
 * FileName: SensitiveTest
 * Author:   XueZhenLonG
 * Date:     2021/1/31 16:34
 * Description: 敏感词测试类
 */
package com.nowcoder.community;

import com.nowcoder.community.util.SensitiveFilter;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * 〈敏感词测试类〉
 *
 * @author XueZhenLonG
 * @create 2021/1/31
 * @since 1.0.0
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class SensitiveTests {

    @Autowired
    private SensitiveFilter sensitiveFilter;

    @Test
    public void testSensitiveFilter(){
        String text = "我要开@票给快乐的小小薛,因为他要赌@博! 哈哈哈哈哈哈哈!";
        text = sensitiveFilter.filter(text);
        System.out.println(text);
    }

}
