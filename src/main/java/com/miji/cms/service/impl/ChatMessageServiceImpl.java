package com.miji.cms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.miji.cms.common.ErrorCode;
import com.miji.cms.exception.BusinessException;
import com.miji.cms.mapper.ChatSessionMapper;
import com.miji.cms.model.domain.ChatMessage;
import com.miji.cms.model.domain.ChatSession;
import com.miji.cms.model.domain.User;
import com.miji.cms.model.request.MessageQueryRequest;
import com.miji.cms.service.ChatMessageService;
import com.miji.cms.mapper.ChatMessageMapper;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.List;

/**
* @author 16427
* @description 针对表【chat_message(会话消息表)】的数据库操作Service实现
* @createDate 2025-12-01 14:07:34
*/
@Service
public class ChatMessageServiceImpl extends ServiceImpl<ChatMessageMapper, ChatMessage>
    implements ChatMessageService{

    @Resource
    private ChatMessageMapper messageMapper;

    @Resource
    private ChatSessionMapper sessionMapper;

    @Override
    public ChatMessage sendMessage(Long sessionId, String content, HttpServletRequest request) {
        if (sessionId == null || content == null || content.trim().isEmpty())
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数不完整");

        User loginUser = (User) request.getSession().getAttribute("userLoginState");
        if (loginUser == null) throw new BusinessException(ErrorCode.NOT_LOGIN);

        ChatSession session = sessionMapper.selectById(sessionId);
        if (session == null || session.getIsDelete() == 1)
            throw new BusinessException(ErrorCode.NULL_ERROR, "会话不存在");

        if (!session.getUser1Id().equals(loginUser.getId()) && !session.getUser2Id().equals(loginUser.getId()))
            throw new BusinessException(ErrorCode.NO_AUTH, "无权限在此会话中发送消息");

        ChatMessage m = new ChatMessage();
        m.setSessionId(sessionId);
        m.setSenderId(loginUser.getId());
        m.setContent(content);
        m.setCreateTime(new Date());
        m.setIsDelete(0);
        messageMapper.insert(m);
        return m;
    }

    @Override
    public List<ChatMessage> listMessages(MessageQueryRequest req, HttpServletRequest request) {
        if (req == null || req.getSessionId() == null)
            throw new BusinessException(ErrorCode.PARAMS_ERROR);

        User loginUser = (User) request.getSession().getAttribute("userLoginState");
        if (loginUser == null) throw new BusinessException(ErrorCode.NOT_LOGIN);

        ChatSession s = sessionMapper.selectById(req.getSessionId());
        if (s == null || s.getIsDelete() == 1) throw new BusinessException(ErrorCode.NULL_ERROR);
        if (!s.getUser1Id().equals(loginUser.getId()) && !s.getUser2Id().equals(loginUser.getId()))
            throw new BusinessException(ErrorCode.NO_AUTH, "无权限查看该会话消息");

        QueryWrapper<ChatMessage> qw = new QueryWrapper<>();
        qw.eq("sessionId", req.getSessionId()).eq("isDelete", 0).orderByAsc("createTime");
        // TODO: 分页可以使用 PageHelper 或 MyBatis-Plus 的 Page 实现，这里返回全部或部分
        return messageMapper.selectList(qw);
    }
}




