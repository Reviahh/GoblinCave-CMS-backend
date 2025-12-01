package com.miji.cms.service;

import com.miji.cms.model.domain.ChatSession;
import com.baomidou.mybatisplus.extension.service.IService;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
* @author 16427
* @description 针对表【chat_session(一对一会话表)】的数据库操作Service
* @createDate 2025-12-01 14:07:30
*/
public interface ChatSessionService extends IService<ChatSession> {

    ChatSession createSession(Long targetUserId, Long recruitmentId, HttpServletRequest request);
    List<ChatSession> listMySessions(HttpServletRequest request);
    ChatSession getByIdWithAuth(Long sessionId, HttpServletRequest request);
}
