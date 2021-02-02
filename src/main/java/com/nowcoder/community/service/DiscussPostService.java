package com.nowcoder.community.service;

import com.nowcoder.community.dao.DiscussPostMapper;
import com.nowcoder.community.entity.DiscussPost;
import com.nowcoder.community.util.SensitiveFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import java.util.List;

@Service
public class DiscussPostService {
    //注入对文章的操作方法
    @Autowired
    private DiscussPostMapper discussPostMapper;

    //注入过滤敏感词的工具
    @Autowired
    private SensitiveFilter sensitiveFilter;

    //根据用户的用户的id,在数据库中查询用户的文章
    public List<DiscussPost> findDiscussPosts(int userId, int offset, int limit) {
        return discussPostMapper.selectDiscussPosts(userId, offset, limit);
    }

    //根据用户的id,在数据库中查询,用户的文章数量
    public int findDiscussPostRows(int userId) {
        return discussPostMapper.selectDiscussPostRows(userId);
    }

    //添加文章,并做敏感词汇的过滤
    public int addDiscussPost(DiscussPost post){
        if (post==null){
            throw new IllegalArgumentException("参数不能为空!");
        }
        //转译HTML标记,使用springMVC提供的工具
        post.setTitle(HtmlUtils.htmlEscape(post.getTitle()));
        post.setTitle(HtmlUtils.htmlEscape(post.getContent()));
        //对文章的标题和内容进行敏感词的过滤
        post.setTitle(sensitiveFilter.filter(post.getTitle()));
        post.setContent(sensitiveFilter.filter(post.getContent()));
        //调用discussPostMapper进行文章的插入,并返回
        return discussPostMapper.insertDiscussPost(post);
    }

    //根据id查询帖子
    public DiscussPost findDiscussPostById(int id){
        return discussPostMapper.selectDiscussPostById(id);
    }

    public int updateCommentCount(int id, int commentCount){
        return discussPostMapper.updateCommentCount(id,commentCount);
    }

}
