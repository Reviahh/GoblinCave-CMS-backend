package com.miji.cms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.miji.cms.common.ErrorCode;
import com.miji.cms.exception.BusinessException;
import com.miji.cms.mapper.TeamMapper;
import com.miji.cms.model.domain.Competition;
import com.miji.cms.model.domain.Team;
import com.miji.cms.model.domain.TeamRecruitment;
import com.miji.cms.model.domain.User;
import com.miji.cms.model.request.RecruitmentCreateRequest;
import com.miji.cms.model.request.RecruitmentQueryRequest;
import com.miji.cms.service.CompetitionService;
import com.miji.cms.service.TeamRecruitmentService;
import com.miji.cms.mapper.TeamRecruitmentMapper;
import com.miji.cms.service.UserService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.List;

/**
* @author 16427
* @description 针对表【team_recruitment(队友招募表)】的数据库操作Service实现
* @createDate 2025-12-01 14:07:05
*/
@Service
public class TeamRecruitmentServiceImpl extends ServiceImpl<TeamRecruitmentMapper, TeamRecruitment>
    implements TeamRecruitmentService{

    @Resource
    private TeamRecruitmentMapper recruitmentMapper;

    @Resource
    private TeamMapper teamMapper;

    @Resource
    private UserService userService;

    @Resource
    private CompetitionService competitionService;

    @Override
    public Long createRecruitment(RecruitmentCreateRequest req, HttpServletRequest request) {
        if (req == null || req.getCompetitionId() == null || req.getTitle() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数缺失");
        }

        // 使用统一的获取登录用户方法
        User loginUser = userService.getLoginUser(request);
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN);
        }

        // 若是代表队伍发布，校验队伍与当前用户关系
        if (req.getIsTeam() != null && req.getIsTeam() == 1) {
            if (req.getTeamId() == null) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "teamId 不能为空");
            }
            Team team = teamMapper.selectById(req.getTeamId());
            if (team == null || team.getIsDelete() == 1) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍不存在");
            }
            // 校验当前用户为队长
            if (!team.getUserId().equals(loginUser.getId())) {
                throw new BusinessException(ErrorCode.NO_AUTH, "只有队长可以代表队伍发布");
            }
        }

        // 校验竞赛存在
        Competition competition = competitionService.getById(req.getCompetitionId());
        if (competition == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "竞赛不存在");
        }

        TeamRecruitment r = new TeamRecruitment();
        r.setUserId(loginUser.getId());
        r.setCompetitionId(req.getCompetitionId());
        r.setTeamId(req.getTeamId());
        r.setIsTeam(req.getIsTeam() == null ? 0 : req.getIsTeam());
        r.setTitle(req.getTitle());
        r.setDescription(req.getDescription());
        r.setContact(req.getContact());

        // 添加：设置招募人数
        r.setMaxMembers(req.getMaxMembers());

        r.setCreateTime(new Date());
        r.setUpdateTime(new Date());
        r.setIsDelete(0);

        int result = recruitmentMapper.insert(r);
        if (result <= 0) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "发布招募令失败");
        }

        return r.getId();
    }

    @Override
    public boolean updateRecruitment(TeamRecruitment recruitment, HttpServletRequest request) {
        if (recruitment == null || recruitment.getId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = (User) request.getSession().getAttribute("userLoginState");
        if (loginUser == null) throw new BusinessException(ErrorCode.NOT_LOGIN);

        TeamRecruitment exist = recruitmentMapper.selectById(recruitment.getId());
        if (exist == null || exist.getIsDelete() == 1) throw new BusinessException(ErrorCode.NULL_ERROR);

        if (!exist.getUserId().equals(loginUser.getId())) {
            throw new BusinessException(ErrorCode.NO_AUTH, "只有发布者可以修改");
        }
        exist.setTitle(recruitment.getTitle());
        exist.setDescription(recruitment.getDescription());
        exist.setContact(recruitment.getContact());
        exist.setUpdateTime(new Date());
        return recruitmentMapper.updateById(exist) > 0;
    }

    @Override
    public boolean deleteRecruitment(Long id, HttpServletRequest request) {
        if (id == null) throw new BusinessException(ErrorCode.PARAMS_ERROR);
        User loginUser = (User) request.getSession().getAttribute("userLoginState");
        if (loginUser == null) throw new BusinessException(ErrorCode.NOT_LOGIN);

        TeamRecruitment exist = recruitmentMapper.selectById(id);
        if (exist == null || exist.getIsDelete() == 1) throw new BusinessException(ErrorCode.NULL_ERROR);
        if (!exist.getUserId().equals(loginUser.getId()) && !userService.isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH, "无权限删除");
        }

        return this.removeById(id);
    }

    @Override
    public List<TeamRecruitment> listRecruitments(RecruitmentQueryRequest req, HttpServletRequest request) {
        QueryWrapper<TeamRecruitment> qw = new QueryWrapper<>();
        qw.eq("isDelete", 0);
        if (req.getCompetitionId() != null) qw.eq("competitionId", req.getCompetitionId());
        if (req.getIsTeam() != null) qw.eq("isTeam", req.getIsTeam());
        qw.orderByDesc("createTime");
        return recruitmentMapper.selectList(qw);
    }

    @Override
    public TeamRecruitment getRecruitmentDetail(Long id, HttpServletRequest request) {
        if (id == null) throw new BusinessException(ErrorCode.PARAMS_ERROR);
        TeamRecruitment r = recruitmentMapper.selectById(id);
        if (r == null || r.getIsDelete() == 1) throw new BusinessException(ErrorCode.NULL_ERROR);
        return r;
    }
}




