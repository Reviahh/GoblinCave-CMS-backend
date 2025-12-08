package com.miji.cms.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.miji.cms.common.BaseResponse;
import com.miji.cms.common.ErrorCode;
import com.miji.cms.common.ResultUtils;
import com.miji.cms.exception.BusinessException;
import com.miji.cms.model.domain.ChatMessage;
import com.miji.cms.model.domain.TeamRecruitment;
import com.miji.cms.model.domain.User;
import com.miji.cms.model.request.RecruitmentMessageRequest;
import com.miji.cms.service.ChatMessageService;
import com.miji.cms.service.TeamRecruitmentService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.List;

import static com.miji.cms.constant.UserConstant.USER_LOGIN_STATE;

/**
 * 聊天消息接口（仅公开留言）
 */
@RestController
@RequestMapping("/chat/message")
@CrossOrigin(origins = { "http://localhost:5173/", "http://localhost:3000/",
        "https://miji-frontend.vercel.app/" }, allowCredentials = "true")
public class ChatMessageController {

    @Resource
    private ChatMessageService messageService;

    @Resource
    private TeamRecruitmentService recruitmentService;

    /**
     * 发送留言到招募帖（公开留言板）
     *
     * @param req         - { recruitmentId, content }
     * @param httpRequest
     * @return 新创建的消息
     */
    @PostMapping("/recruitment")
    public BaseResponse<ChatMessage> sendRecruitmentMessage(
            @RequestBody RecruitmentMessageRequest req,
            HttpServletRequest httpRequest) {
        // 1. 验证参数
        if (req.getRecruitmentId() == null || req.getRecruitmentId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "招募帖ID不合法");
        }
        if (req.getContent() == null || req.getContent().trim().isEmpty()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "留言内容不能为空");
        }

        // 2. 获取当前登录用户
        User currentUser = (User) httpRequest.getSession().getAttribute(USER_LOGIN_STATE);
        if (currentUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN, "未登录");
        }

        // 3. 验证招募帖是否存在
        TeamRecruitment recruitment = recruitmentService.getById(req.getRecruitmentId());
        if (recruitment == null) {
            throw new BusinessException(ErrorCode.NULL_ERROR, "招募帖不存在");
        }

        // 4. 创建消息
        ChatMessage message = new ChatMessage();
        message.setRecruitmentId(req.getRecruitmentId());
        message.setSenderId(currentUser.getId());
        message.setSenderName(
                currentUser.getUserName() != null ? currentUser.getUserName() : currentUser.getUserAccount());
        message.setContent(req.getContent().trim());
        message.setCreateTime(new Date());

        // 5. 保存到数据库
        boolean success = messageService.save(message);
        if (!success) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "发送消息失败");
        }

        return ResultUtils.success(message);
    }

    /**
     * 获取招募帖的留言列表
     *
     * @param recruitmentId - 招募帖ID
     * @return 留言列表（按时间正序）
     */
    @GetMapping("/recruitment/list")
    public BaseResponse<List<ChatMessage>> listRecruitmentMessages(
            @RequestParam Long recruitmentId) {
        // 1. 验证参数
        if (recruitmentId == null || recruitmentId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "招募帖ID不合法");
        }

        // 2. 查询该招募帖的所有留言，按时间正序
        QueryWrapper<ChatMessage> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("recruitmentId", recruitmentId)
                .eq("isDelete", 0)
                .orderByAsc("createTime");

        List<ChatMessage> messages = messageService.list(queryWrapper);
        return ResultUtils.success(messages);
    }
}
