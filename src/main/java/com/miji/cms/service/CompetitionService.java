package com.miji.cms.service;

import com.miji.cms.model.domain.Competition;
import com.baomidou.mybatisplus.extension.service.IService;
import com.miji.cms.model.request.CompetitionCreateRequest;
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


}
