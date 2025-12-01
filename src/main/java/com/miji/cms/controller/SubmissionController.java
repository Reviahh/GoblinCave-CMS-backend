package com.miji.cms.controller;

import com.miji.cms.common.BaseResponse;
import com.miji.cms.common.ErrorCode;
import com.miji.cms.common.ResultUtils;
import com.miji.cms.exception.BusinessException;
import com.miji.cms.model.domain.Submission;
import com.miji.cms.model.request.SubmissionSubmitRequest;
import com.miji.cms.model.request.SubmissionQueryRequest;
import com.miji.cms.service.SubmissionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 提交接口
 */
@RestController
@RequestMapping("/submission")
@Slf4j
public class SubmissionController {

    @Resource
    private SubmissionService submissionService;

    /**
     * 提交作品（覆盖旧稿）
     */
    @PostMapping("/submit")
    public BaseResponse<Long> submitWork(
            @RequestParam("file") MultipartFile file,
            @RequestParam("competitionId") Long competitionId,
            @RequestParam("registrationId") Long registrationId,
            @RequestParam("description") String description,
            HttpServletRequest httpRequest) {

        SubmissionSubmitRequest request = new SubmissionSubmitRequest();
        request.setCompetitionId(competitionId);
        request.setRegistrationId(registrationId);
        request.setDescription(description);

        Long submissionId = submissionService.submitWork(request, file, httpRequest);
        return ResultUtils.success(submissionId);
    }

    /**
     * 查询提交列表
     */
    @PostMapping("/list")
    public BaseResponse<List<Submission>> listSubmissions(
            @RequestBody SubmissionQueryRequest request,
            HttpServletRequest httpRequest) {

        List<Submission> list = submissionService.listSubmissions(request, httpRequest);
        return ResultUtils.success(list);
    }

    /**
     * 获取提交详情
     */
    @GetMapping("/detail")
    public BaseResponse<Submission> getSubmissionDetail(
            @RequestParam Long submissionId,
            HttpServletRequest request) {

        Submission submission = submissionService.getSubmissionDetail(submissionId, request);
        return ResultUtils.success(submission);
    }

    /**
     * 管理员评分
     */
    @PostMapping("/score")
    public BaseResponse<Boolean> scoreSubmission(
            @RequestParam Long submissionId,
            @RequestParam Integer score,
            HttpServletRequest httpRequest) {

        Boolean result = submissionService.scoreSubmission(submissionId, score, httpRequest);
        return ResultUtils.success(result);
    }
}
