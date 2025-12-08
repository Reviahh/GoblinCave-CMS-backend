package com.miji.cms.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.miji.cms.model.domain.ChatMessage;

/**
 * 聊天消息服务接口
 */
public interface ChatMessageService extends IService<ChatMessage> {
    // 继承 MyBatis-Plus 的基础 CRUD 方法即可
    // 业务逻辑已移至 ChatMessageController
}