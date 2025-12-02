package com.miji.cms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.miji.cms.common.ErrorCode;
import com.miji.cms.exception.BusinessException;
import com.miji.cms.model.domain.ChatSession;
import com.miji.cms.model.domain.User;
import com.miji.cms.service.ChatSessionService;
import com.miji.cms.mapper.ChatSessionMapper;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.List;

/**
* @author 16427
* @description 针对表【chat_session(一对一会话表)】的数据库操作Service实现
* @createDate 2025-12-01 14:07:30
*/
@Service
public class ChatSessionServiceImpl extends ServiceImpl<ChatSessionMapper, ChatSession>
    implements ChatSessionService{


    @Resource
    private ChatSessionMapper sessionMapper;

    @Override
    public ChatSession createSession(Long targetUserId, Long recruitmentId, HttpServletRequest request) {
        if (targetUserId == null) throw new BusinessException(ErrorCode.PARAMS_ERROR);
        User loginUser = (User) request.getSession().getAttribute("userLoginState");
        if (loginUser == null) throw new BusinessException(ErrorCode.NOT_LOGIN);
        if (loginUser.getId().equals(targetUserId)) throw new BusinessException(ErrorCode.PARAMS_ERROR, "不能和自己创建会话");

        // 查是否已有会话（任意顺序）
        QueryWrapper<ChatSession> qw = new QueryWrapper<>();
        qw.eq("isDelete", 0)
                .and(w -> w
                        .eq("user1Id", loginUser.getId()).eq("user2Id", targetUserId)
                        .or()
                        .eq("user1Id", targetUserId).eq("user2Id", loginUser.getId())
                );
        ChatSession exist = sessionMapper.selectOne(qw);
        if (exist != null) return exist;

        ChatSession s = new ChatSession();
        s.setUser1Id(loginUser.getId());
        s.setUser2Id(targetUserId);
        s.setRecruitmentId(recruitmentId);
        s.setCreateTime(new Date());
        s.setIsDelete(0);
        sessionMapper.insert(s);
        return s;
    }

    @Override
    public List<ChatSession> listMySessions(HttpServletRequest request) {
        User loginUser = (User) request.getSession().getAttribute("userLoginState");
        if (loginUser == null) throw new BusinessException(ErrorCode.NOT_LOGIN);

        QueryWrapper<ChatSession> qw = new QueryWrapper<>();
        qw.eq("isDelete", 0)
                .and(w -> w.eq("user1Id", loginUser.getId()).or().eq("user2_id", loginUser.getId()))
                .orderByDesc("createTime");
        return sessionMapper.selectList(qw);
    }

    @Override
    public ChatSession getByIdWithAuth(Long sessionId, HttpServletRequest request) {
        if (sessionId == null) throw new BusinessException(ErrorCode.PARAMS_ERROR);
        User loginUser = (User) request.getSession().getAttribute("userLoginState");
        if (loginUser == null) throw new BusinessException(ErrorCode.NOT_LOGIN);

        ChatSession s = sessionMapper.selectById(sessionId);
        if (s == null || s.getIsDelete() == 1) throw new BusinessException(ErrorCode.NULL_ERROR);
        if (!s.getUser1Id().equals(loginUser.getId()) && !s.getUser2Id().equals(loginUser.getId()))
            throw new BusinessException(ErrorCode.NO_AUTH, "无权限访问该会话");
        return s;
    }
}




