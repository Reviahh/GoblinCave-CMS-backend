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
    /**
     * 发送留言到招募帖（公开留言板）
     * @param req - { recruitmentId, content }
     * @param httpRequest
     * @return 新创建的消息
     */
    @PostMapping("/recruitment")
    public BaseResponse<ChatMessage> sendRecruitmentMessage(
            @RequestBody RecruitmentMessageRequest req,
            HttpServletRequest httpRequest
    ) {
        // 获取当前用户
        User currentUser = (User) httpRequest.getSession().getAttribute(USER_LOGIN_STATE);
        if (currentUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN, "未登录");
        }

        // 验证招募帖是否存在
        TeamRecruitment recruitment = recruitmentService.getById(req.getRecruitmentId());
        if (recruitment == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "招募帖不存在");
        }

        // 创建消息
        ChatMessage message = new ChatMessage();
        message.setRecruitmentId(req.getRecruitmentId());
        message.setSenderId(currentUser.getId());
        message.setSenderName(currentUser.getUserName());
        message.setContent(req.getContent());
        message.setCreateTime(new Date());

        boolean success = messageService.save(message);
        if (!success) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "发送消息失败");
        }

        return ResultUtils.success(message);
    }

    /**
     * 获取招募帖的留言列表
     * @param recruitmentId - 招募帖ID
     * @return 留言列表（按时间正序）
     */
    @GetMapping("/recruitment/list")
    public BaseResponse<List<ChatMessage>> listRecruitmentMessages(
            @RequestParam Long recruitmentId
    ) {
        if (recruitmentId == null || recruitmentId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "招募帖ID不合法");
        }

        // 查询该招募帖的所有留言，按时间正序
        QueryWrapper<ChatMessage> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("recruitmentId", recruitmentId)
                .orderByAsc("createTime");

        List<ChatMessage> messages = messageService.list(queryWrapper);
        return ResultUtils.success(messages);
    }
}
