package com.miji.cms.controller;

import com.miji.cms.common.BaseResponse;
import com.miji.cms.common.ErrorCode;
import com.miji.cms.common.ResultUtils;
import com.miji.cms.exception.BusinessException;
import com.miji.cms.model.domain.Competition;
import com.miji.cms.model.request.CompetitionCreateRequest;
import com.miji.cms.service.CompetitionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/competition")
@CrossOrigin(origins = {"http://localhost:5173/","http://localhost:3000/","https://miji-frontend.vercel.app/"},allowCredentials = "true")
@Slf4j
public class CompetitionController {

    @Resource
    private CompetitionService competitionService;

    /**
     * 新增竞赛（管理员权限）
     */
    @PostMapping("/add")
    public BaseResponse<Long> addCompetition(@RequestBody CompetitionCreateRequest request, HttpServletRequest httpRequest) {
        if (request == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "竞赛信息不能为空");
        }
        long result = competitionService.addCompetition(request,httpRequest);
        return ResultUtils.success(result);
    }


}