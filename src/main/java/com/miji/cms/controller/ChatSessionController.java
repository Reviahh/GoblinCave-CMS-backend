package com.miji.cms.controller;

import com.miji.cms.common.BaseResponse;
import com.miji.cms.common.ResultUtils;
import com.miji.cms.model.domain.ChatSession;
import com.miji.cms.model.request.ChatCreateRequest;
import com.miji.cms.service.ChatSessionService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 聊天会话接口
 */
@RestController
@RequestMapping("/chat/session")
public class ChatSessionController {

    @Resource
    private ChatSessionService sessionService;

    /**
     * 创建会话
     * @param req
     * @param httpRequest
     * @return
     */
    @PostMapping("/create")
    public BaseResponse<ChatSession> create(@RequestBody ChatCreateRequest req, HttpServletRequest httpRequest) {
        ChatSession s = sessionService.createSession(req.getTargetUserId(), req.getRecruitmentId(), httpRequest);
        return ResultUtils.success(s);
    }

    /**
     * 获取会话列表
     * @param httpRequest
     * @return
     */
    @GetMapping("/list")
    public BaseResponse<List<ChatSession>> list(HttpServletRequest httpRequest) {
        List<ChatSession> list = sessionService.listMySessions(httpRequest);
        return ResultUtils.success(list);
    }

    /**
     * 获取会话详情
     * @param sessionId
     * @param httpRequest
     * @return
     */
    @GetMapping("/detail")
    public BaseResponse<ChatSession> detail(@RequestParam Long sessionId, HttpServletRequest httpRequest) {
        ChatSession s = sessionService.getByIdWithAuth(sessionId, httpRequest);
        return ResultUtils.success(s);
    }
}
