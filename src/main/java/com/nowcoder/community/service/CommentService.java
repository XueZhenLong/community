/**
 * FileName: CommentService
 * Author:   XueZhenLonG
 * Date:     2021/2/1 14:18
 * Description: 操作用户评论的业务层
 */
package com.nowcoder.community.service;

import com.nowcoder.community.dao.CommentMapper;
import com.nowcoder.community.dao.DiscussPostMapper;
import com.nowcoder.community.entity.Comment;
import com.nowcoder.community.util.CommunityConstant;
import com.nowcoder.community.util.SensitiveFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.util.HtmlUtils;

import java.util.List;

/**
 * 〈操作用户评论的业务层〉
 *
 * @author XueZhenLonG
 * @create 2021/2/1
 * @since 1.0.0
 */
@Service
public class CommentService implements CommunityConstant {

    @Autowired
    private CommentMapper commentMapper;

    //注入敏感词过滤的组件,用于增加评论
    @Autowired
    private SensitiveFilter sensitiveFilter;

    @Autowired
    private DiscussPostService discussPostService;

    //查询文章所有的评论
    public List<Comment> findCommentsByEntity(int entityType, int entityId, int offset, int limit){
         return commentMapper.selectCommentsByEntity(entityType,entityId,offset,limit);
    }
    //查询文章的评论数量
    public int findCommentCount(int entityType, int entityId){
        return commentMapper.selectCountByEntity(entityType,entityId);
    }

    //处理增加评论的业务,需要进行事务的管理 使用声明式事务
    @Transactional(isolation = Isolation.READ_COMMITTED,propagation = Propagation.REQUIRED)
    public int addComment(Comment comment){
        if (comment==null){
            throw new IllegalArgumentException("参数不能为空!");
        }
        //事务1
        //对内容进行过滤,添加评论
        //利用spring封装的方法,对html的标签进行过滤
        comment.setContent(HtmlUtils.htmlEscape(comment.getContent()));
        //使用自定义的过滤器,对敏感词进行过滤
        comment.setContent(sensitiveFilter.filter(comment.getContent()));
        //把评论存入库中
        int rows = commentMapper.insertComment(comment);


        //事务2
        //更新评论的数量,只有增加评论的时候,我们才更新评论的数量
        if(comment.getEntityType()==ENTITY_TYPE_POST){
            int count = commentMapper.selectCountByEntity(comment.getEntityType(), comment.getEntityId());
            discussPostService.updateCommentCount(comment.getEntityId(),count);

        }
        return rows;
    }


}
