package com.miji.cms.service;

import com.miji.cms.model.domain.Competition;
import com.baomidou.mybatisplus.extension.service.IService;
import com.miji.cms.model.request.CompetitionCreateRequest;

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



}
