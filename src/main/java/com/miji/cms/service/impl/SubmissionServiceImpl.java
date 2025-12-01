package com.miji.cms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.miji.cms.common.ErrorCode;
import com.miji.cms.exception.BusinessException;
import com.miji.cms.mapper.CompetitionRegistrationMapper;
import com.miji.cms.mapper.CompetitionSubmissionMapper;
import com.miji.cms.mapper.TeamMemberMapper;
import com.miji.cms.mapper.TeamMapper;
import com.miji.cms.model.domain.*;
import com.miji.cms.model.request.SubmissionQueryRequest;
import com.miji.cms.model.request.SubmissionSubmitRequest;
import com.miji.cms.service.CompetitionService;
import com.miji.cms.service.FileUploadService;
import com.miji.cms.service.SubmissionService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Service
public class SubmissionServiceImpl extends ServiceImpl<CompetitionSubmissionMapper, Submission>
        implements SubmissionService {

    @Resource
    private CompetitionService competitionService;

    @Resource
    private CompetitionRegistrationMapper registrationMapper;

    @Resource
    private TeamMapper teamMapper;

    @Resource
    private TeamMemberMapper teamMemberMapper;

    @Value("${file.upload-path}")
    private String uploadPath;

    @Override
    public Long submitWork(SubmissionSubmitRequest request, MultipartFile file, HttpServletRequest httpRequest) {
        if (request == null || request.getRegistrationId() == null || file == null || file.isEmpty()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数或文件不能为空");
        }

        // 1. 获取登录用户
        User loginUser = (User) httpRequest.getSession().getAttribute("userLoginState");
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN);
        }

        // 2. 获取报名记录
        CompetitionRegistration reg = registrationMapper.selectById(request.getRegistrationId());
        if (reg == null || reg.getStatus() != 1) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "报名记录不存在或未通过审核");
        }

        // 3. 权限校验（个人赛/团队赛）
        if (reg.getUserId() != null && !reg.getUserId().equals(loginUser.getId())) {
            throw new BusinessException(ErrorCode.NO_AUTH, "您无权提交该作品");
        }
        if (reg.getTeamId() != null) {
            if (!reg.getUserId().equals(loginUser.getId())) {
                throw new BusinessException(ErrorCode.NO_AUTH, "您不是队伍成员，无权提交作品");
            }
        }

        // 4. 文件上传
        String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
        File dest = new File(uploadPath + File.separator + "submissions" + File.separator + fileName);
        dest.getParentFile().mkdirs();
        try {
            file.transferTo(dest);
        } catch (IOException e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "文件上传失败: " + e.getMessage());
        }
        String fileUrl = "/uploads/submissions/" + fileName; // 返回前端访问路径

        // 5. 检查是否已有提交（覆盖旧稿）
        Submission old = lambdaQuery()
                .eq(Submission::getRegistrationId, request.getRegistrationId())
                .eq(Submission::getIsDelete, 0)
                .one();

        Submission submission = old != null ? old : new Submission();

        submission.setCompetitionId(reg.getCompetitionId());
        submission.setRegistrationId(request.getRegistrationId());
        submission.setDescription(request.getDescription());
        submission.setFileUrl(fileUrl);
        submission.setUserId(reg.getUserId());
        submission.setTeamId(reg.getTeamId());
        submission.setStatus(0); // 待评审
        submission.setCreateTime(old == null ? new Date() : submission.getCreateTime());
        submission.setUpdateTime(new Date());

        saveOrUpdate(submission);
        return submission.getId();
    }

    /**
     * 查询提交详情
     */
    @Override
    public Submission getSubmissionDetail(Long submissionId, HttpServletRequest request) {
        Submission submission = getById(submissionId);
        if (submission == null || submission.getIsDelete() == 1) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "提交记录不存在");
        }
        return submission;
    }
    /**
     * 查询提交列表
     */
    @Override
    public List<Submission> listSubmissions(SubmissionQueryRequest request, HttpServletRequest httpRequest) {

        User loginUser = (User) httpRequest.getSession().getAttribute("userLoginState");
        if (loginUser == null) throw new BusinessException(ErrorCode.NOT_LOGIN);

        QueryWrapper<Submission> wrapper = new QueryWrapper<>();
        wrapper.eq("isDelete", 0);
        if (request.getCompetitionId() != null) {
            wrapper.eq("competitionId", request.getCompetitionId());
        }

        if (loginUser.getUserRole() == 0) {
            // 普通用户只能看自己的提交
            wrapper.eq("userId", loginUser.getId());
        }

        return list(wrapper);
    }




    /**
     * 管理员评分
     */
    @Override
    public Boolean scoreSubmission(Long submissionId, Integer score, HttpServletRequest httpRequest) {
        User loginUser = (User) httpRequest.getSession().getAttribute("userLoginState");
        if (loginUser == null) throw new BusinessException(ErrorCode.NOT_LOGIN);

        Submission submission = getById(submissionId);
        if (submission == null || submission.getIsDelete() == 1) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "提交不存在");
        }

        Competition competition = competitionService.getById(submission.getCompetitionId());
        if (competition == null || !competition.getCreatorId().equals(loginUser.getId())) {
            throw new BusinessException(ErrorCode.NO_AUTH, "无权限评分");
        }

        submission.setScore(score);
        submission.setReviewerId(loginUser.getId());
        submission.setStatus(1); // 已评分
        submission.setUpdateTime(new Date());
        return updateById(submission);
    }
}

