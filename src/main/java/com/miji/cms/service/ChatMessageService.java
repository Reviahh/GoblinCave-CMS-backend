package com.miji.cms.service;

import com.miji.cms.model.domain.ChatMessage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.miji.cms.model.request.MessageQueryRequest;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
* @author 16427
* @description 针对表【chat_message(会话消息表)】的数据库操作Service
* @createDate 2025-12-01 14:07:34
*/
public interface ChatMessageService extends IService<ChatMessage> {

    ChatMessage sendMessage(Long sessionId, String content, HttpServletRequest request);

    List<ChatMessage> listMessages(MessageQueryRequest req, HttpServletRequest request);
}
