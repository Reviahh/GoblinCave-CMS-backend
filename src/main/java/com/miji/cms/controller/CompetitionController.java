package com.miji.cms.controller;

import com.miji.cms.common.BaseResponse;
import com.miji.cms.common.ErrorCode;
import com.miji.cms.common.ResultUtils;
import com.miji.cms.exception.BusinessException;
import com.miji.cms.model.domain.Competition;
import com.miji.cms.model.request.CompetitionCreateRequest;
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
@CrossOrigin(origins = {"http://localhost:5173/","http://localhost:3000/","https://miji-frontend.vercel.app/"},allowCredentials = "true")
@Slf4j
public class CompetitionController {

    @Resource
    private CompetitionService competitionService;

    /**
     * 新增竞赛
     *
     * @param request
     * @param httpRequest
     * @return
     */
    @PostMapping("/add")
    public BaseResponse<Long> addCompetition(@RequestBody CompetitionCreateRequest request, HttpServletRequest httpRequest) {
        if (request == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "竞赛信息不能为空");
        }
        long result = competitionService.addCompetition(request,httpRequest);
        return ResultUtils.success(result);
    }

    /**
     * 更新竞赛信息
     *
     * @param request
     * @param httpRequest
     * @return
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

}