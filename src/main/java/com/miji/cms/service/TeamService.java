package com.miji.cms.service;

import com.miji.cms.model.domain.Team;
import com.baomidou.mybatisplus.extension.service.IService;
import com.miji.cms.model.request.TeamCreateRequest;

import javax.servlet.http.HttpServletRequest;

/**
* @author miji
* @description 针对表【team(队伍表)】的数据库操作Service
* @createDate 2025-10-24 13:55:11
*/
public interface TeamService extends IService<Team> {

    /**
     * 创建队伍
     *
     * @param request
     * @param httpRequest
     * @return
     */
    Long createTeam(TeamCreateRequest request, HttpServletRequest httpRequest);
}
