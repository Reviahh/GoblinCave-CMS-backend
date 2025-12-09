package com.miji.cms.service.impl;

import com.alibaba.excel.EasyExcel;
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
import com.miji.cms.model.request.SubmissionRankVO;
import com.miji.cms.model.request.SubmissionSubmitRequest;
import com.miji.cms.service.*;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class SubmissionServiceImpl extends ServiceImpl<CompetitionSubmissionMapper, Submission>
        implements SubmissionService {

    @Resource
    private UserService userService;

    @Resource
    private TeamService teamService;

    @Resource
    private CompetitionService competitionService;

    @Resource
    private CompetitionRegistrationMapper competitionRegistrationMapper;

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
        CompetitionRegistration reg = competitionRegistrationMapper.selectById(request.getRegistrationId());
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
        String fileUrl = "/uploads/submissions/" + fileName;

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
        submission.setStatus(0);
        submission.setCreateTime(old == null ? new Date() : submission.getCreateTime());
        submission.setUpdateTime(new Date());

        saveOrUpdate(submission);
        return submission.getId();
    }

    @Override
    public Submission getSubmissionDetail(Long submissionId, HttpServletRequest httpRequest) {
        Submission submission = getById(submissionId);
        if (submission == null || submission.getIsDelete() == 1) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "提交记录不存在");
        }
        return submission;
    }

    @Override
    public List<Submission> listSubmissions(SubmissionQueryRequest request, HttpServletRequest httpRequest) {
        User loginUser = (User) httpRequest.getSession().getAttribute("userLoginState");
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN);
        }

        QueryWrapper<Submission> wrapper = new QueryWrapper<>();
        wrapper.eq("isDelete", 0);
        if (request.getCompetitionId() != null) {
            wrapper.eq("competitionId", request.getCompetitionId());
        }

        if (loginUser.getUserRole() == 0) {
            wrapper.eq("userId", loginUser.getId());
        }

        return list(wrapper);
    }

    @Override
    public Boolean scoreSubmission(Long submissionId, Integer score, HttpServletRequest httpRequest) {
        User loginUser = (User) httpRequest.getSession().getAttribute("userLoginState");
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN);
        }

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
        submission.setStatus(1);
        submission.setUpdateTime(new Date());
        return updateById(submission);
    }

    @Override
    public List<SubmissionRankVO> getCompetitionRank(Long competitionId) {
        List<Submission> list = this.lambdaQuery()
                .eq(Submission::getCompetitionId, competitionId)
                .isNotNull(Submission::getScore)
                .orderByDesc(Submission::getScore)
                .list();

        return list.stream().map(this::convertToRankVO).collect(Collectors.toList());
    }

    @Override
    public void exportCompetitionScore(Long competitionId, HttpServletResponse response) {
        List<SubmissionRankVO> data = getCompetitionRank(competitionId);

        try {
            response.setContentType("application/vnd.ms-excel");
            response.setCharacterEncoding("utf-8");
            String fileName = URLEncoder.encode("竞赛成绩表-" + competitionId, "UTF-8");
            response.setHeader("Content-Disposition", "attachment;filename=" + fileName + ".xlsx");

            EasyExcel.write(response.getOutputStream(), SubmissionRankVO.class)
                    .sheet("成绩榜")
                    .doWrite(data);

        } catch (Exception e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "导出 Excel 失败");
        }
    }

    @Override
    public SubmissionRankVO getScoreDetail(Long submissionId) {
        Submission submission = this.getById(submissionId);
        if (submission == null) {
            throw new BusinessException(ErrorCode.NULL_ERROR, "提交记录不存在");
        }
        return convertToRankVO(submission);
    }

    private SubmissionRankVO convertToRankVO(Submission submission) {
        SubmissionRankVO vo = new SubmissionRankVO();
        BeanUtils.copyProperties(submission, vo);

        if (submission.getUserId() != null) {
            User user = userService.getById(submission.getUserId());
            if (user != null) {
                vo.setSubmitUserName(user.getUserName());
            }
        }

        if (submission.getTeamId() != null) {
            Team team = teamService.getById(submission.getTeamId());
            if (team != null) {
                vo.setTeamName(team.getName());
            }
        }

        return vo;
    }
}