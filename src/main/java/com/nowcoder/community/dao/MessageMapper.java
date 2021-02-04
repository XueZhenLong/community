/**
 * FileName: MessageMapper
 * Author:   XueZhenLonG
 * Date:     2021/2/1 20:02
 * Description: 操作私信
 */
package com.nowcoder.community.dao;

import com.nowcoder.community.entity.Message;
import org.apache.ibatis.annotations.Mapper;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * 〈操作私信〉
 *
 * @author XueZhenLonG
 * @create 2021/2/1
 * @since 1.0.0
 */
@Mapper
public interface MessageMapper {

    //查询当前用户的会话列表,针对每个会话只返回一条最新的私信.
    List<Message> selectConversations(int userId, int offset, int limit);

    //查询当前用户的会话数量
    int selectConversationCount(int userId);

    //查询某个会话所包含的私信列表
    List<Message> selectLetters(String conversationId, int offset, int limit);

    //查询某个会话所包含的私信数量
    int selectLetterCount(String conversationId);

    //查询未读私信的数量
    int selectLetterUnreadCount(int userId,String conversationId);

    //新增一个消息
    int insertMessage(Message message);

    //把未读的消息转换成已读消息
    int updateStatus(List<Integer> ids,int status);


}
