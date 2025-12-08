package com.miji.cms.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.miji.cms.mapper.ChatMessageMapper;
import com.miji.cms.model.domain.ChatMessage;
import com.miji.cms.service.ChatMessageService;
import org.springframework.stereotype.Service;

/**
 * 聊天消息服务实现
 */
@Service
public class ChatMessageServiceImpl extends ServiceImpl<ChatMessageMapper, ChatMessage>
        implements ChatMessageService {
    // 继承 MyBatis-Plus 的基础实现即可
    // 业务逻辑已移至 ChatMessageController
}