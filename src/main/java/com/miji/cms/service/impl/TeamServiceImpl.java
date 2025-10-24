package com.miji.cms.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.miji.cms.common.ErrorCode;
import com.miji.cms.exception.BusinessException;
import com.miji.cms.mapper.CompetitionMapper;
import com.miji.cms.mapper.TeamMemberMapper;
import com.miji.cms.model.domain.Competition;
import com.miji.cms.model.domain.Team;
import com.miji.cms.model.domain.TeamMember;
import com.miji.cms.model.domain.User;
import com.miji.cms.model.request.TeamCreateRequest;
import com.miji.cms.service.TeamService;
import com.miji.cms.mapper.TeamMapper;
import com.miji.cms.service.UserService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Date;

/**
* @author 16427
* @description 针对表【team(队伍表)】的数据库操作Service实现
* @createDate 2025-10-24 13:55:11
*/
@Service
public class TeamServiceImpl extends ServiceImpl<TeamMapper, Team>
    implements TeamService{

    @Resource
    private CompetitionMapper competitionMapper;

    @Resource
    private TeamMemberMapper teamMemberMapper;

    @Resource
    private UserService userService;

    @Override
    public Long createTeam(TeamCreateRequest request, HttpServletRequest httpRequest) {
        if (request == null || request.getCompetitionId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "竞赛ID不能为空");
        }

        // 获取当前登录用户
        User loginUser = userService.getLoginUser(httpRequest);
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN, "请先登录");
        }

        // 获取竞赛信息
        Competition competition = competitionMapper.selectById(request.getCompetitionId());
        if (competition == null) {
            throw new BusinessException(ErrorCode.NULL_ERROR, "竞赛不存在");
        }

        // 校验竞赛是否允许创建队伍
        if (competition.getMaxMembers() == 1) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "该竞赛为个人赛，不允许创建队伍");
        }

        // 校验参数
        if (StringUtils.isAnyBlank(request.getName())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍名称不能为空");
        }

        // 创建队伍
        Team team = new Team();
        team.setCompetitionId(request.getCompetitionId());
        team.setName(request.getName());
        team.setDescription(request.getDescription());
        team.setUserId(loginUser.getId());
        team.setMaxNum(competition.getMaxMembers());
        team.setExpireTime(request.getExpireTime());
        team.setCreateTime(new Date());
        team.setUpdateTime(new Date());
        team.setCurrentNum(1);

        int insertResult = baseMapper.insert(team);
        if (insertResult <= 0) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "创建队伍失败");
        }

        // 将队长加入队伍成员表
        TeamMember teamMember = new TeamMember();
        teamMember.setTeamId(team.getId());
        teamMember.setUserId(loginUser.getId());
        teamMember.setRole(1); // 队长
        teamMemberMapper.insert(teamMember);

        return team.getId();
    }
}




