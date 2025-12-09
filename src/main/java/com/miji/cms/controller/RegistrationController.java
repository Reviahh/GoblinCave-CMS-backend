package com.miji.cms.controller;

import com.miji.cms.common.BaseResponse;
import com.miji.cms.common.ErrorCode;
import com.miji.cms.common.ResultUtils;
import com.miji.cms.exception.BusinessException;
import com.miji.cms.model.domain.Registration;
import com.miji.cms.model.domain.User;
import com.miji.cms.service.RegistrationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/competition/registration")
@CrossOrigin(origins = {"http://localhost:5173/", "http://localhost:3000/"}, allowCredentials = "true")
@Slf4j
public class RegistrationController {

    @Resource
    private RegistrationService registrationService;

    private static final String USER_LOGIN_STATE = "userLoginState";

    /**
     * 获取当前用户在指定竞赛的报名状态
     */
    @GetMapping("/my-status")
    public BaseResponse<Map<String, Object>> getMyRegistrationStatus(
            @RequestParam Long competitionId,
            HttpServletRequest request) {

        // 获取当前登录用户
        User currentUser = (User) request.getSession().getAttribute(USER_LOGIN_STATE);
        if (currentUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN);
        }

        Long userId = currentUser.getId();
        Registration registration = registrationService.getMyRegistration(userId, competitionId);

        Map<String, Object> result = new HashMap<>();
        if (registration == null) {
            result.put("registered", false);
            result.put("status", null);
            result.put("teamId", null);
            result.put("registrationId", null);
        } else {
            result.put("registered", true);
            result.put("status", registration.getStatus());
            result.put("teamId", registration.getTeamId());
            result.put("registrationId", registration.getId());
        }

        return ResultUtils.success(result);
    }

    /**
     * 报名竞赛
     */
    @PostMapping("/register")
    public BaseResponse<Long> register(
            @RequestParam Long competitionId,
            @RequestParam(required = false) Long teamId,
            HttpServletRequest request) {

        User currentUser = (User) request.getSession().getAttribute(USER_LOGIN_STATE);
        if (currentUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN);
        }

        // 检查是否已报名
        Registration existing = registrationService.getMyRegistration(currentUser.getId(), competitionId);
        if (existing != null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "您已报名该竞赛");
        }

        Registration registration = new Registration();
        registration.setUserId(currentUser.getId());
        registration.setCompetitionId(competitionId);
        registration.setTeamId(teamId);
        registration.setStatus(0); // 待审核

        registrationService.save(registration);
        return ResultUtils.success(registration.getId());
    }
}