package com.miji.cms.controller;

import com.miji.cms.common.BaseResponse;
import com.miji.cms.common.ErrorCode;
import com.miji.cms.common.ResultUtils;
import com.miji.cms.exception.BusinessException;
import com.miji.cms.model.domain.Competition;
import com.miji.cms.model.domain.CompetitionRegistration;
import com.miji.cms.model.request.CompetitionCreateRequest;
import com.miji.cms.model.request.CompetitionRegisterRequest;
import com.miji.cms.model.request.CompetitionReviewRequest;
import com.miji.cms.model.request.CompetitionUpdateRequest;
import com.miji.cms.service.CompetitionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 竞赛接口
 *
 * @author miji
 */
@RestController
@RequestMapping("/competition")
@CrossOrigin(origins = { "http://localhost:5173/", "http://localhost:3000/",
        "https://miji-frontend.vercel.app/" }, allowCredentials = "true")
@Slf4j
public class CompetitionController {

    @Resource
    private CompetitionService competitionService;

    /**
     * 新增竞赛
     */
    @PostMapping("/add")
    public BaseResponse<Long> addCompetition(@RequestBody CompetitionCreateRequest request,
                                             HttpServletRequest httpRequest) {
        if (request == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "竞赛信息不能为空");
        }
        long result = competitionService.addCompetition(request, httpRequest);
        return ResultUtils.success(result);
    }

    /**
     * 更新竞赛信息
     */
    @PostMapping("/update")
    public BaseResponse<Boolean> updateCompetition(
            @RequestBody CompetitionUpdateRequest request,
            HttpServletRequest httpRequest) {

        if (request == null || request.getId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "竞赛ID不能为空");
        }

        boolean result = competitionService.updateCompetition(request, httpRequest);
        return ResultUtils.success(result);
    }

    /**
     * 删除竞赛
     */
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteCompetition(@RequestParam Long id, HttpServletRequest request) {
        if (id == null || id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "无效的竞赛ID");
        }

        boolean result = competitionService.deleteCompetition(id, request);
        return ResultUtils.success(result);
    }

    /**
     * 查询竞赛列表
     */
    @GetMapping("/list")
    public BaseResponse<List<Competition>> listCompetitions(@RequestParam(required = false) String name) {
        return ResultUtils.success(competitionService.listCompetitions(name));
    }

    /**
     * 获取竞赛详情（添加空值检查）
     */
    @GetMapping("/detail")
    public BaseResponse<Competition> getCompetitionDetail(@RequestParam Long id) {
        if (id == null || id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "竞赛ID不合法");
        }

        Competition competition = competitionService.getCompetitionById(id);

        // 添加空值检查
        if (competition == null) {
            throw new BusinessException(ErrorCode.NULL_ERROR, "竞赛不存在或已被删除");
        }

        return ResultUtils.success(competition);
    }

    /**
     * 竞赛报名
     */
    @PostMapping("/register")
    public BaseResponse<Boolean> registerCompetition(
            @RequestBody CompetitionRegisterRequest request,
            HttpServletRequest httpRequest) {

        if (request == null || request.getCompetitionId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "竞赛ID不能为空");
        }

        boolean result = competitionService.registerCompetition(request, httpRequest);
        return ResultUtils.success(result);
    }

    /**
     * 审核报名
     */
    @PostMapping("/review")
    public BaseResponse<Boolean> reviewRegistration(@RequestBody CompetitionReviewRequest request,
                                                    HttpServletRequest httpRequest) {
        if (request == null || request.getRegistrationId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "报名ID不能为空");
        }

        boolean result = competitionService.reviewRegistration(request, httpRequest);
        return ResultUtils.success(result);
    }

    /**
     * 获取竞赛报名列表（仅创建者可见）
     */
    @GetMapping("/registration/list")
    public BaseResponse<List<CompetitionRegistration>> listCompetitionRegistrations(
            @RequestParam Long competitionId,
            HttpServletRequest httpRequest) {

        if (competitionId == null || competitionId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "无效的竞赛ID");
        }

        List<CompetitionRegistration> registrations = competitionService.listCompetitionRegistrations(competitionId,
                httpRequest);

        return ResultUtils.success(registrations);
    }

    /**
     * 获取当前用户已报名的竞赛列表
     */
    @GetMapping("/my")
    public BaseResponse<List<Competition>> listMyCompetitions(HttpServletRequest httpRequest) {
        List<Competition> myCompetitions = competitionService.listMyCompetitions(httpRequest);
        return ResultUtils.success(myCompetitions);
    }
}
