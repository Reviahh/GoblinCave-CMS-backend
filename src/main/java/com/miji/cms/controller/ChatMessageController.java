package com.miji.cms.controller;

import com.miji.cms.common.BaseResponse;
import com.miji.cms.common.ResultUtils;
import com.miji.cms.model.domain.ChatMessage;
import com.miji.cms.model.request.MessageQueryRequest;
import com.miji.cms.model.request.MessageSendRequest;
import com.miji.cms.service.ChatMessageService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 聊天消息接口
 */
@RestController
@RequestMapping("/chat/message")
@CrossOrigin(origins = {"http://localhost:5173/","http://localhost:3000/","https://miji-frontend.vercel.app/"},allowCredentials = "true")
public class ChatMessageController {

    @Resource
    private ChatMessageService messageService;

    /**
     * 发送消息
     * @param req
     * @param httpRequest
     * @return
     */
    @PostMapping("/send")
    public BaseResponse<ChatMessage> send(@RequestBody MessageSendRequest req, HttpServletRequest httpRequest) {
        ChatMessage m = messageService.sendMessage(req.getSessionId(), req.getContent(), httpRequest);
        return ResultUtils.success(m);
    }

    /**
     * 获取消息列表
     * @param req
     * @param httpRequest
     * @return
     */
    @PostMapping("/list")
    public BaseResponse<List<ChatMessage>> list(@RequestBody MessageQueryRequest req, HttpServletRequest httpRequest) {
        List<ChatMessage> msgs = messageService.listMessages(req, httpRequest);
        return ResultUtils.success(msgs);
    }
}
