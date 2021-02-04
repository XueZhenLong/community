/**
 * FileName: CommentMapper
 * Author:   XueZhenLonG
 * Date:     2021/2/1 14:08
 * Description: 操作用户评论的mapper
 */
package com.nowcoder.community.dao;

import com.nowcoder.community.entity.Comment;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 〈操作用户评论的mapper〉
 *
 * @author XueZhenLonG
 * @create 2021/2/1
 * @since 1.0.0
 */
@Mapper
public interface CommentMapper {


    //进行分页的操作
    //1.查询文章所有的评论
    //分页的条件offset limit 每页显示行数的限制
    List<Comment> selectCommentsByEntity(int entityType, int entityId,int offset,int limit);


    //2.用来查询用户评论的总数
    int selectCountByEntity(int entityType, int entityId);

    //添加评论的方法
    int insertComment(Comment comment);


}
