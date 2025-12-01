package com.miji.cms.service;

import com.miji.cms.model.domain.TeamRecruitment;
import com.baomidou.mybatisplus.extension.service.IService;
import com.miji.cms.model.request.RecruitmentCreateRequest;
import com.miji.cms.model.request.RecruitmentQueryRequest;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
* @author 16427
* @description 针对表【team_recruitment(队友招募表)】的数据库操作Service
* @createDate 2025-12-01 14:07:05
*/
public interface TeamRecruitmentService extends IService<TeamRecruitment> {

    Long createRecruitment(RecruitmentCreateRequest req, HttpServletRequest request);

    boolean updateRecruitment(TeamRecruitment recruitment, HttpServletRequest request);

    boolean deleteRecruitment(Long id, HttpServletRequest request);

    List<TeamRecruitment> listRecruitments(RecruitmentQueryRequest req, HttpServletRequest request);

    TeamRecruitment getRecruitmentDetail(Long id, HttpServletRequest request);
}
