package com.miji.cms.service;

import com.miji.cms.model.domain.Team;
import com.baomidou.mybatisplus.extension.service.IService;
import com.miji.cms.model.request.TeamCreateRequest;
import com.miji.cms.model.request.TeamUpdateRequest;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

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

    /**
     * 加入队伍
     *
     * @param teamId
     * @param request
     * @return
     */
    boolean joinTeam(Long teamId, HttpServletRequest request);

    /**
     * 退出队伍
     *
     * @param teamId
     * @param request
     * @return
     */
    boolean quitTeam(Long teamId, HttpServletRequest request);

    /**
     * 查询队伍详情
     *
     * @param teamId
     * @return
     */
    Map<String, Object> getTeamDetail(Long teamId);
    /**
     * 删除/解散队伍
     */
    boolean deleteTeam(Long id, HttpServletRequest request);

    /**
     * 更新队伍信息
     */
    boolean updateTeam(TeamUpdateRequest request, HttpServletRequest httpRequest);

    /**
     * 获取竞赛下的队伍列表
     */
    List<Map<String, Object>> listTeams(Long competitionId);

    /**
     * 获取我参与的队伍列表
     */
    List<Map<String, Object>> listMyTeams(HttpServletRequest request);
}
