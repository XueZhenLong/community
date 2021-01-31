/**
 * FileName: SensitiveFilter
 * Author:   XueZhenLonG
 * Date:     2021/1/31 15:47
 * Description: 敏感词过滤过滤器
 */
package com.nowcoder.community.util;

import com.sun.org.apache.bcel.internal.generic.IfInstruction;
import org.apache.commons.lang3.CharUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.tomcat.util.security.Escape;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;


/**
 * 〈敏感词过滤过滤器〉
 *
 * @author XueZhenLonG
 * @create 2021/1/31
 * @since 1.0.0
 */
@Component
public class SensitiveFilter {

    //实例化日志
    private static final Logger logger = LoggerFactory.getLogger(SensitiveFilter.class);

    //用于替换敏感词的常量
    private static final String REPLACEMENT = "***";

    //初始化一个根节点
    private TrieNode rootNode = new TrieNode();

    @PostConstruct //这是一个初始化方法注释,当容器调用过滤器的时候,这个方法会自动的调用
    public void init(){
        try (
                //这是try方式是JDK7的新特性 可以自动的关闭io操作
                //通过获取类加载器,读取配置文件
              InputStream is = this.getClass().getClassLoader().getResourceAsStream("sensitive-words.txt");
              //放入缓冲流中
              BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        ) {
            //读取
            String keyword;
            while ((keyword = reader.readLine())!=null){
                //把敏感词添加到前缀树去
                this.addKeyword(keyword);
            }

        }catch (IOException e){
            logger.error("加载敏感词文件失败"+e);
        }

    }
    //将一个敏感词添加到前缀树中去
    private void addKeyword(String keyword) {
        //默认指向前缀树的根节点
        TrieNode tempNode = rootNode;
        //遍历keyword 将敏感词 添加到前缀树中
        for (int i = 0; i < keyword.length(); i++) {
            //逐个的获取敏感词的字符
            char c = keyword.charAt(i);
            //调用getSubNode 获取子节点
            TrieNode subNode = tempNode.getSubNode(c);
            //如果没有子节点,我们要创造子节点,把敏感词挂在当前节点之下
            if (subNode==null){
                //初始化子节点
                subNode = new TrieNode();
                //把字符c的数据放入创建好的子节点中
                tempNode.addSubNode(c,subNode);
            }

            //指向子节点,进入下一轮循环
            tempNode = subNode;
            //设置结束的标识
            if (i == keyword.length()-1){
                //如果走到了最后一个,那么设置setKeywordEnd为true,设置结尾的标志.
                tempNode.setKeywordEnd(true);
            }
        }
    }

    /**
     * 过滤敏感词
     * @param text 等待过滤的文本
     * @return  过滤后的文本
     */
    public String filter(String text){
        if (StringUtils.isBlank(text)){
            return null;
        }
        //指针1 指向的树
        TrieNode tempNode = rootNode;
        //指针2 指向的字符串的首位
        int begin = 0;
        //指针3 指向的是字符串的首位, 他是他会波动
        int position = 0;
        //我们要返回的结果 因为他便于追加
        StringBuilder sb = new StringBuilder();

        //遍历
        while (position < text.length()){
            char c = text.charAt(position);
            //跳过符号 例如 C_N_M 这个也是敏感词,所以我们跳过符号.
            if (isSymbol(c)){
                //若指针1处于根节点,我们就将此符号计入结果,让指针2向下走一步
                if (tempNode == rootNode){
                    sb.append(c);
                    begin++;
                }
                //无论符号在开头或者中间,指针3都会向下走一步
                position++;
                continue;
            }
            //检查下级节点
            tempNode = tempNode.getSubNode(c);
            if (tempNode ==null){
                //以begin开头的字符串不是敏感词
                sb.append(text.charAt(begin));
                //进入下一个位置
                position = ++ begin;
                //指针1重新指向根节点
                tempNode = rootNode;
            }else if (tempNode.isKeywordEnd()){
                //发现了敏感词,将begin-position字符串替换掉
                sb.append(REPLACEMENT);
                //进入到下一个位置
                begin = ++position;
                //指针1重新指向根节点
                tempNode = rootNode;
            }else {
                //在疑似敏感词的途中
                //继续检查下一个字符
                position++;
            }

        }
        //将最后一批字符计入结果 (疑似敏感词但是不是)
        sb.append(text.substring(begin));
        return sb.toString();

    }
    //判断字符是否为符号
    private boolean isSymbol(Character c) {
        //isAsciiAlphanumeric CharUtils类中封住的判断字符是否为特殊符号的方法.
        //0x2E80-0x9FFF 是东亚文字的范围
        return !CharUtils.isAsciiAlphanumeric(c) && (c <0x2E80 || c> 0x9FFF);
    }

    //前缀树--内部类
    private class TrieNode {
        //关键词结束的标识
        private boolean isKeywordEnd = false;

        //子节点(key是下级节点的字符,value是下级节点)
        private Map<Character, TrieNode> subNodes = new HashMap<>();

        public boolean isKeywordEnd() {
            return isKeywordEnd;
        }

        public void setKeywordEnd(boolean keywordEnd) {
            isKeywordEnd = keywordEnd;
        }

        //添加子节点
        public void addSubNode(Character c, TrieNode node) {
            subNodes.put(c, node);
        }

        //获取子节点
        public TrieNode getSubNode(Character c) {
            return subNodes.get(c);
        }
    }

}
