/**
 * FileName: MessageController
 * Author:   XueZhenLonG
 * Date:     2021/2/1 20:52
 * Description: 处理私信消息
 */
package com.nowcoder.community.controller;

import com.alibaba.fastjson.JSONObject;
import com.nowcoder.community.entity.Message;
import com.nowcoder.community.entity.Page;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.service.MessageService;
import com.nowcoder.community.service.UserService;
import com.nowcoder.community.util.CommunityConstant;
import com.nowcoder.community.util.CommunityUtil;
import com.nowcoder.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.util.HtmlUtils;

import java.util.*;

@Controller
public class MessageController implements CommunityConstant {

    @Autowired
    private MessageService messageService;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private UserService userService;

    // 私信列表
    @RequestMapping(path = "/letter/list", method = RequestMethod.GET)
    public String getLetterList(Model model, Page page) {
        User user = hostHolder.getUser();
        // 分页信息
        page.setLimit(5);
        page.setPath("/letter/list");
        page.setRows(messageService.findConversationCount(user.getId()));

        // 会话列表
        List<Message> conversationList = messageService.findConversations(
                user.getId(), page.getOffset(), page.getLimit());
        List<Map<String, Object>> conversations = new ArrayList<>();
        if (conversationList != null) {
            for (Message message : conversationList) {
                Map<String, Object> map = new HashMap<>();
                map.put("conversation", message);
                map.put("letterCount", messageService.findLetterCount(message.getConversationId()));
                map.put("unreadCount", messageService.findLetterUnreadCount(user.getId(), message.getConversationId()));
                int targetId = user.getId() == message.getFromId() ? message.getToId() : message.getFromId();
                map.put("target", userService.findUserById(targetId));

                conversations.add(map);
            }
        }
        model.addAttribute("conversations", conversations);

        // 查询未读消息数量
        int letterUnreadCount = messageService.findLetterUnreadCount(user.getId(), null);
        model.addAttribute("letterUnreadCount", letterUnreadCount);

        //查询未读通知的数量
        int noticeUnreadCount = messageService.findNoticeUnreadCount(user.getId(), null);
        model.addAttribute("noticeUnreadCount", noticeUnreadCount);

        return "/site/letter";
    }


    @RequestMapping(path = "/letter/detail/{conversationId}", method = RequestMethod.GET)
    public String getLetterDetail(@PathVariable("conversationId") String conversationId, Page page, Model model) {
        // 分页信息
        page.setLimit(5);
        page.setPath("/letter/detail/" + conversationId);
        page.setRows(messageService.findLetterCount(conversationId));

        // 私信列表
        List<Message> letterList = messageService.findLetters(conversationId, page.getOffset(), page.getLimit());
        List<Map<String, Object>> letters = new ArrayList<>();
        if (letterList != null) {
            for (Message message : letterList) {
                Map<String, Object> map = new HashMap<>();
                map.put("letter", message);
                map.put("fromUser", userService.findUserById(message.getFromId()));
                letters.add(map);
            }
        }
        model.addAttribute("letters", letters);

        // 私信目标
        model.addAttribute("target", getLetterTarget(conversationId));

        // 设置已读
        List<Integer> ids = getLetterIds(letterList);
        if (!ids.isEmpty()) {
            messageService.readMessage(ids);
        }

        return "/site/letterDetail";
    }


    private User getLetterTarget(String conversationId) {
        String[] ids = conversationId.split("_");
        int id0 = Integer.parseInt(ids[0]);
        int id1 = Integer.parseInt(ids[1]);

        if (hostHolder.getUser().getId() == id0) {
            return userService.findUserById(id1);
        } else {
            return userService.findUserById(id0);
        }
    }


    //获取用户未读的消息,用于把消息状态变为已读
    private List<Integer> getLetterIds(List<Message> letterList) {
        List<Integer> ids = new ArrayList<>();

        if (letterList != null) {
            for (Message message : letterList) {
                if (hostHolder.getUser().getId() == message.getToId() && message.getStatus() == 0) {
                    ids.add(message.getId());
                }
            }
        }
        return ids;
    }


    @RequestMapping(path = "/letter/send", method = RequestMethod.POST)
    @ResponseBody
    public String sendLetter(String toName, String content) {
        //异步请求错误
        // Integer.valueOf("ABC");
        User target = userService.findUserByName(toName);
        if (target == null) {
            return CommunityUtil.getJSONString(1, "目标用户不存在!");
        }

        Message message = new Message();
        message.setFromId(hostHolder.getUser().getId());
        message.setToId(target.getId());
        if (message.getFromId() < message.getToId()) {
            message.setConversationId(message.getFromId() + "_" + message.getToId());
        } else {
            message.setConversationId(message.getToId() + "_" + message.getFromId());
        }
        message.setContent(content);
        message.setCreateTime(new Date());
        messageService.addMessage(message);
        return CommunityUtil.getJSONString(0);
    }

    //显示通知的列表
    @RequestMapping(path = "/notice/list", method = RequestMethod.GET)
    public String getNoticeList(Model model) {
        //获取当前用户
        User user = hostHolder.getUser();


        //查询评论类的通知
        Message message = messageService.findLatestNotice(user.getId(), TOPIC_COMMENT);
        //使用Map用来封装聚合的消息数据,最终被模板所引用,显示数据来源的对象
        Map<String, Object> messageVO = new HashMap<>();
        messageVO.put("message", message);
        if (message != null) {
            //将我们从数据库中查询到的聚合数据,使用HtmlUtils 进行反向转译
            String content = HtmlUtils.htmlUnescape(message.getContent());
            //将利用JSONObject.parseObject() 将content 转换为map对象
            HashMap<String, Object> data = JSONObject.parseObject(content, HashMap.class);
            //取出data中的数据,向messageVO中设置数据
            messageVO.put("user", userService.findUserById((Integer) data.get("userId")));
            messageVO.put("entityType", data.get("entityType"));
            messageVO.put("entityId", data.get("entityId"));
            messageVO.put("postId", data.get("postId"));
            //查询这一类通知的总数量
            int count = messageService.findNoticeCount(user.getId(), TOPIC_COMMENT);
            messageVO.put("count", count);
            //查询消息的未读数量
            int unread = messageService.findNoticeUnreadCount(user.getId(), TOPIC_COMMENT);
            messageVO.put("unread", unread);
        }
        //把messageVO传给模板
        model.addAttribute("commentNotice", messageVO);


        //查询点赞类的通知
        message = messageService.findLatestNotice(user.getId(), TOPIC_LIKE);
        //使用Map用来封装聚合的消息数据,最终被模板所引用,显示数据来源的对象
        messageVO = new HashMap<>();
        messageVO.put("message", message);
        if (message != null) {
            //将我们从数据库中查询到的聚合数据,使用HtmlUtils 进行反向转译
            String content = HtmlUtils.htmlUnescape(message.getContent());
            //将利用JSONObject.parseObject() 将content 转换为map对象
            HashMap<String, Object> data = JSONObject.parseObject(content, HashMap.class);
            //取出data中的数据,向messageVO中设置数据
            messageVO.put("user", userService.findUserById((Integer) data.get("userId")));
            messageVO.put("entityType", data.get("entityType"));
            messageVO.put("entityId", data.get("entityId"));
            messageVO.put("postId", data.get("postId"));
            //查询这一类通知的总数量
            int count = messageService.findNoticeCount(user.getId(), TOPIC_LIKE);
            messageVO.put("count", count);
            //查询消息的未读数量
            int unread = messageService.findNoticeUnreadCount(user.getId(), TOPIC_LIKE);
            messageVO.put("unread", unread);
        }
        //把messageVO传给模板
        model.addAttribute("likeNotice", messageVO);


        //查询关注类的通知
        message = messageService.findLatestNotice(user.getId(), TOPIC_FOLLOW);
        //使用Map用来封装聚合的消息数据,最终被模板所引用,显示数据来源的对象
        messageVO = new HashMap<>();
        messageVO.put("message", message);
        if (message != null) {
            //将我们从数据库中查询到的聚合数据,使用HtmlUtils 进行反向转译
            String content = HtmlUtils.htmlUnescape(message.getContent());
            //将利用JSONObject.parseObject() 将content 转换为map对象
            HashMap<String, Object> data = JSONObject.parseObject(content, HashMap.class);
            //取出data中的数据,向messageVO中设置数据
            messageVO.put("user", userService.findUserById((Integer) data.get("userId")));
            messageVO.put("entityType", data.get("entityType"));
            messageVO.put("entityId", data.get("entityId"));
            //查询这一类通知的总数量
            int count = messageService.findNoticeCount(user.getId(), TOPIC_FOLLOW);
            messageVO.put("count", count);
            //查询消息的未读数量
            int unread = messageService.findNoticeUnreadCount(user.getId(), TOPIC_FOLLOW);
            messageVO.put("unread", unread);
        }
        //把messageVO传给模板
        model.addAttribute("followNotice", messageVO);


        //-------------------------------------------------------------------
        //查询未读消息数量
        int letterUnreadCount = messageService.findLetterUnreadCount(user.getId(), null);
        model.addAttribute("letterUnreadCount", letterUnreadCount);

        //查询未读通知的数量
        int noticeUnreadCount = messageService.findNoticeUnreadCount(user.getId(), null);
        model.addAttribute("noticeUnreadCount", noticeUnreadCount);

        //返回到模板
        return "/site/notice";
    }

    //通知的详情
    @RequestMapping(path = "/notice/detail/{topic}", method = RequestMethod.GET)
    public String getNoticeDetail(@PathVariable("topic") String topic, Page page, Model model) {
        //获取当前用户
        User user = hostHolder.getUser();
        //设置分页条件信息
        //每页显示五条数据
        page.setLimit(5);
        //设置路径
        page.setPath("/notice/detail/" + topic);
        //设置行数
        page.setRows(messageService.findNoticeCount(user.getId(), topic));

        //查询消息
        List<Message> noticeList = messageService.findNotices(user.getId(), topic, page.getOffset(), page.getLimit());
        //声明一个集合,存储聚合的数据
        List<Map<String, Object>> noticeVoList = new ArrayList<>();
        if (noticeList != null) {
            for (Message notice
                    : noticeList) {
                HashMap<String, Object> map = new HashMap<>();
                //通知
                map.put("notice", notice);
                //内容 使用HtmlUtils进行反转译处理
                String content = HtmlUtils.htmlUnescape(notice.getContent());
                //把数据转换为HashMap
                Map<String, Object> data = JSONObject.parseObject(content, HashMap.class);
                //把转换出来的数据 封装到map中
                map.put("user", userService.findUserById((Integer) data.get("userId")));
                map.put("entityType", data.get("entityType"));
                map.put("entityId", data.get("entityId"));
                map.put("postId", data.get("postId"));
                //通知的作者
                map.put("fromUser", userService.findUserById(notice.getFromId()));

                noticeVoList.add(map);
            }
        }
        model.addAttribute("notices", noticeVoList);

        //设置已读
        //获取需要设置为已读的id
        List<Integer> ids = getLetterIds(noticeList);
        if (!ids.isEmpty()) {
            //设置为已读
            messageService.readMessage(ids);
        }
        //返回
        return "/site/notice-detail";
    }


}
