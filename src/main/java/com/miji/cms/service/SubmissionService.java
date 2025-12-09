package com.miji.cms.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.miji.cms.model.domain.Submission;
import com.miji.cms.model.request.SubmissionQueryRequest;
import com.miji.cms.model.request.SubmissionRankVO;
import com.miji.cms.model.request.SubmissionSubmitRequest;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

public interface SubmissionService extends IService<Submission> {

    /**
     * 提交作品（覆盖旧稿）并上传文件
     */
    Long submitWork(SubmissionSubmitRequest request, MultipartFile file, HttpServletRequest httpRequest);

    /**
     * 列出提交作品
     */
    List<Submission> listSubmissions(SubmissionQueryRequest request, HttpServletRequest httpRequest);

    /**
     * 获取提交详情
     */
    Submission getSubmissionDetail(Long submissionId, HttpServletRequest httpRequest);

    /**
     * 评分提交作品
     */
    Boolean scoreSubmission(Long submissionId, Integer score, HttpServletRequest httpRequest);

    /**
     * 竞赛成绩榜单
     */
    List<SubmissionRankVO> getCompetitionRank(Long competitionId);

    /**
     * 导出成绩表
     */
    void exportCompetitionScore(Long competitionId, HttpServletResponse response);

    /**
     * 查询成绩详情
     */
    SubmissionRankVO getScoreDetail(Long submissionId);
}