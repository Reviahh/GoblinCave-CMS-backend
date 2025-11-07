package com.miji.cms.service;

import com.miji.cms.model.domain.Competition;
import com.baomidou.mybatisplus.extension.service.IService;
import com.miji.cms.model.domain.CompetitionRegistration;
import com.miji.cms.model.request.CompetitionCreateRequest;
import com.miji.cms.model.request.CompetitionRegisterRequest;
import com.miji.cms.model.request.CompetitionReviewRequest;
import com.miji.cms.model.request.CompetitionUpdateRequest;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
* @author 16427
* @description 针对表【competition(竞赛信息表)】的数据库操作Service
* @createDate 2025-10-13 10:43:52
*/
public interface CompetitionService extends IService<Competition> {
    /**
     * 新增竞赛
     */
    long addCompetition(CompetitionCreateRequest request, HttpServletRequest httpRequest);

    /**
     * 更新竞赛信息
     * @param request 更新请求体
     * @param httpRequest 用于获取登录用户信息
     * @return true 成功 / false 失败
     */
    boolean updateCompetition(CompetitionUpdateRequest request, HttpServletRequest httpRequest);


    /**
     * 删除竞赛
     * @param id 竞赛ID
     * @param request Http请求
     * @return 是否成功
     */
    boolean deleteCompetition(Long id, HttpServletRequest request);

    /**
     * 查询竞赛
     *
     * @param name
     * @return
     */
    List<Competition> listCompetitions(String name);

    /**
     * 根据ID获取竞赛详情
     *
     * @param id 竞赛ID
     * @return 竞赛实体
     */
    Competition getCompetitionById(Long id);

    /**
     * 用户/队伍报名竞赛
     *
     * @param httpRequest 竞赛ID
     * @param request HTTP请求
     * @return 是否报名成功
     */
    boolean registerCompetition(CompetitionRegisterRequest request, HttpServletRequest httpRequest);


    /**
     * 审核报名
     *
     * @param request
     * @param httpRequest
     * @return
     */
    boolean reviewRegistration(CompetitionReviewRequest request, HttpServletRequest httpRequest);


    /**
     * 报名列表查询
     *
     * @param competitionId
     * @param httpRequest
     * @return
     */
    List<CompetitionRegistration> listCompetitionRegistrations(Long competitionId, HttpServletRequest httpRequest);

}
