package com.miji.cms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.miji.cms.common.ErrorCode;
import com.miji.cms.constant.UserConstant;
import com.miji.cms.exception.BusinessException;
import com.miji.cms.mapper.CompetitionRegistrationMapper;
import com.miji.cms.mapper.TeamMapper;
import com.miji.cms.mapper.TeamMemberMapper;
import com.miji.cms.model.domain.*;
import com.miji.cms.model.request.CompetitionCreateRequest;
import com.miji.cms.model.request.CompetitionRegisterRequest;
import com.miji.cms.model.request.CompetitionReviewRequest;
import com.miji.cms.model.request.CompetitionUpdateRequest;
import com.miji.cms.service.CompetitionService;
import com.miji.cms.mapper.CompetitionMapper;
import com.miji.cms.service.UserService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.Date;
import java.util.List;


/**
* @author miji
* @description 针对表【competition(竞赛信息表)】的数据库操作Service实现
* @createDate 2025-10-13 10:43:52
*/
@Service
public class CompetitionServiceImpl extends ServiceImpl<CompetitionMapper, Competition>
    implements CompetitionService{

    @Resource
    private CompetitionMapper competitionMapper;

    @Resource
    private CompetitionRegistrationMapper competitionRegistrationMapper;

    @Resource
    private TeamMapper teamMapper;

    @Resource
    private TeamMemberMapper teamMemberMapper;

    @Override
    public long addCompetition(CompetitionCreateRequest request, HttpServletRequest httpRequest) {
        // 1. 权限校验（只有管理员或教师可发布）
        User loginUser = (User) httpRequest.getSession().getAttribute(UserConstant.USER_LOGIN_STATE);
        if (loginUser == null || loginUser.getUserRole() == 0) {
            throw new BusinessException(ErrorCode.NO_AUTH, "无权限发布竞赛");
        }

        // 2. 参数校验
        if (StringUtils.isAnyBlank(request.getName(), request.getSummary(), request.getOrganizer())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数不完整");
        }

        // 3. 构造实体
        Competition competition = new Competition();
        BeanUtils.copyProperties(request, competition);
        competition.setCreatorId(loginUser.getId());

        // 4. 保存数据库
        boolean saveResult = this.save(competition);
        if (!saveResult) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "竞赛创建失败");
        }

        return competition.getId();
    }

    @Override
    public boolean updateCompetition(CompetitionUpdateRequest request, HttpServletRequest httpRequest) {
        // 1. 校验登录
        User loginUser = (User) httpRequest.getSession().getAttribute(UserConstant.USER_LOGIN_STATE);
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN, "用户未登录");
        }

        // 2. 查询竞赛
        Competition competition = this.getById(request.getId());
        if (competition == null || competition.getIsDelete() == 1) {
            throw new BusinessException(ErrorCode.NULL_ERROR, "竞赛不存在");
        }

        // 3. 校验权限：创建者
        if (!competition.getCreatorId().equals(loginUser.getId())) {
            throw new BusinessException(ErrorCode.NO_AUTH, "无权限修改该竞赛");
        }

        // 4. 更新字符串字段
        if (StringUtils.isNotBlank(request.getName())) competition.setName(request.getName());
        if (StringUtils.isNotBlank(request.getSummary())) competition.setSummary(request.getSummary());
        if (StringUtils.isNotBlank(request.getContent())) competition.setContent(request.getContent());
        if (request.getMaxMembers()!=null) competition.setMaxMembers(request.getMaxMembers());
        if (StringUtils.isNotBlank(request.getCoverUrl())) competition.setCoverUrl(request.getCoverUrl());
        if (StringUtils.isNotBlank(request.getOrganizer())) competition.setOrganizer(request.getOrganizer());

        // 5. 更新时间字段（直接赋值，@JsonFormat 会自动处理）
        if (request.getStartTime() != null) competition.setStartTime(request.getStartTime());
        if (request.getEndTime() != null) competition.setEndTime(request.getEndTime());

        // 6. 更新时间
        competition.setUpdateTime(new Date());

        // 7. 保存数据库
        return this.updateById(competition);
    }

    @Override
    public boolean deleteCompetition(Long id, HttpServletRequest request) {
        // 1. 获取登录用户
        User loginUser = (User) request.getSession().getAttribute(UserConstant.USER_LOGIN_STATE);
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN, "未登录");
        }

        // 2. 查找竞赛
        Competition competition = this.getById(id);
        if (competition == null || competition.getIsDelete() == 1) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "竞赛不存在或已删除");
        }

        // 3. 权限判断
        if (!loginUser.getId().equals(competition.getCreatorId())) {
            throw new BusinessException(ErrorCode.NO_AUTH, "无权限删除该竞赛");
        }

        // 4. 执行逻辑删除
        boolean update = this.removeById(id);
        if (!update) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "删除失败");
        }

        return true;
    }

    @Override
    public List<Competition> listCompetitions(String name) {
        QueryWrapper<Competition> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("isDelete", 0);
        if (StringUtils.isNotBlank(name)) {
            queryWrapper.like("name", name);
        }
        queryWrapper.orderByDesc("createTime");
        return this.list(queryWrapper);
    }

    @Override
    public Competition getCompetitionById(Long id) {
        Competition competition = competitionMapper.selectById(id);
        if (competition == null || competition.getIsDelete() == 1) {
            throw new BusinessException(ErrorCode.NULL_ERROR, "竞赛不存在或已删除");
        }
        return competition;
    }


    @Override
    public boolean registerCompetition(CompetitionRegisterRequest request, HttpServletRequest httpRequest) {
        User loginUser = (User) httpRequest.getSession().getAttribute(UserConstant.USER_LOGIN_STATE);
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN, "未登录");
        }

        Long competitionId = request.getCompetitionId();
        Long teamId = request.getTeamId();

        // 校验竞赛是否存在
        Competition competition = this.getById(competitionId);
        if (competition == null || competition.getIsDelete() == 1) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "竞赛不存在");
        }

        boolean isTeamCompetition = competition.getMaxMembers() > 1;

        /**
         * ================================
         *  若为个人赛 —— 自动创建一个队伍
         * ================================
         */
        if (!isTeamCompetition) {
            // 1. 查是否已有个人队伍（避免重复创建）
            QueryWrapper<Team> qw = new QueryWrapper<>();
            qw.eq("userId", loginUser.getId())
                    .eq("isDelete", 0);

            Team existTeam = teamMapper.selectOne(qw);

            if (existTeam != null) {
                teamId = existTeam.getId();
            } else {
                // 2. 创建新的个人队伍
                Team newTeam = new Team();
                newTeam.setName(loginUser.getUserName() + "的个人队伍");
                newTeam.setUserId(loginUser.getId());
                newTeam.setMaxNum(1);            // 个人赛队伍人数=1
                newTeam.setDescription("个人参赛队伍");
                newTeam.setIsDelete(0);
                newTeam.setCreateTime(new Date());
                newTeam.setUpdateTime(new Date());

                teamMapper.insert(newTeam);
                teamId = newTeam.getId();

                // 插入队伍成员
                TeamMember member = new TeamMember();
                member.setTeamId(teamId);
                member.setUserId(loginUser.getId());
                member.setRole(1);  // 队长
                member.setCreateTime(new Date());
                member.setUpdateTime(new Date());
                teamMemberMapper.insert(member);
            }
        }

        /**
         * ======================================
         * 不论个人赛、团队赛 —— 统一校验队伍合法性
         * ======================================
         */
        Team team = teamMapper.selectById(teamId);
        if (team == null || team.getIsDelete() == 1) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍不存在");
        }

        // 校验是否为队长
        if (!team.getUserId().equals(loginUser.getId())) {
            throw new BusinessException(ErrorCode.NO_AUTH, "只有队长可以代表队伍报名");
        }

        /**
         * =======================================
         * 检查是否已报名
         * =======================================
         */
        QueryWrapper<CompetitionRegistration> existQuery = new QueryWrapper<>();
        existQuery.eq("competitionId", competitionId)
                .eq("teamId", teamId);

        if (competitionRegistrationMapper.selectCount(existQuery) > 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "该队伍已报名此竞赛");
        }

        /**
         * =======================================
         *  插入报名记录（统一风格）
         * =======================================
         */
        CompetitionRegistration registration = new CompetitionRegistration();
        registration.setCompetitionId(competitionId);
        registration.setTeamId(teamId);
        registration.setUserId(loginUser.getId());
        registration.setStatus(0); // 待审核
        registration.setCreateTime(new Date());
        registration.setUpdateTime(new Date());

        return competitionRegistrationMapper.insert(registration) > 0;
    }


    @Override
    public boolean reviewRegistration(CompetitionReviewRequest request, HttpServletRequest httpRequest) {
        User loginUser = (User) httpRequest.getSession().getAttribute(UserConstant.USER_LOGIN_STATE);
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN, "未登录");
        }

        Long registrationId = request.getRegistrationId();
        Integer status = request.getStatus(); // 1-通过，2-拒绝

        if (status == null || (status != 1 && status != 2)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "状态参数错误");
        }

        // 获取报名信息
        CompetitionRegistration registration = competitionRegistrationMapper.selectById(registrationId);
        if (registration == null || registration.getIsDelete() == 1) {
            throw new BusinessException(ErrorCode.NULL_ERROR, "报名记录不存在");
        }

        // 获取对应的竞赛
        Competition competition = this.getById(registration.getCompetitionId());
        if (competition == null || competition.getIsDelete() == 1) {
            throw new BusinessException(ErrorCode.NULL_ERROR, "竞赛不存在");
        }

        // 权限验证：必须是竞赛创建者
        if (!competition.getCreatorId().equals(loginUser.getId())) {
            throw new BusinessException(ErrorCode.NO_AUTH, "只有竞赛创建者才能审核报名");
        }

        // 已审核的不能重复审核
        if (registration.getStatus() != 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "该报名已审核过");
        }

        // 更新状态
        registration.setStatus(status);
        registration.setUpdateTime(new Date());

        int rows = competitionRegistrationMapper.updateById(registration);
        return rows > 0;
    }

    @Override
    public List<CompetitionRegistration> listCompetitionRegistrations(Long competitionId, HttpServletRequest httpRequest) {
        User loginUser = (User) httpRequest.getSession().getAttribute(UserConstant.USER_LOGIN_STATE);
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN, "未登录");
        }

        // 获取竞赛信息
        Competition competition = this.getById(competitionId);
        if (competition == null || competition.getIsDelete() == 1) {
            throw new BusinessException(ErrorCode.NULL_ERROR, "竞赛不存在");
        }

        // 权限验证：仅创建者可查看报名列表
        if (!competition.getCreatorId().equals(loginUser.getId())) {
            throw new BusinessException(ErrorCode.NO_AUTH, "只有竞赛创建者可查看报名列表");
        }

        // 查询报名信息
        LambdaQueryWrapper<CompetitionRegistration> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CompetitionRegistration::getCompetitionId, competitionId)
                .orderByDesc(CompetitionRegistration::getCreateTime);

        List<CompetitionRegistration> registrationList = competitionRegistrationMapper.selectList(queryWrapper);
        return registrationList;
    }

    @Override
    public List<Competition> listMyCompetitions(HttpServletRequest httpRequest) {
        User loginUser = (User) httpRequest.getSession().getAttribute(UserConstant.USER_LOGIN_STATE);
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN, "未登录");
        }

        // 查询用户已通过审核的报名记录
        LambdaQueryWrapper<CompetitionRegistration> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CompetitionRegistration::getUserId, loginUser.getId())
                .eq(CompetitionRegistration::getStatus, 1) // 只查询已通过的报名
                .orderByDesc(CompetitionRegistration::getUpdateTime);

        List<CompetitionRegistration> registrationList = competitionRegistrationMapper.selectList(queryWrapper);

        // 提取竞赛ID列表
        if (registrationList == null || registrationList.isEmpty()) {
            return Collections.emptyList();
        }

        List<Long> competitionIds = registrationList.stream()
                .map(CompetitionRegistration::getCompetitionId)
                .distinct()
                .collect(java.util.stream.Collectors.toList());

        // 批量查询竞赛信息 (isDelete is handled by @TableLogic)
        LambdaQueryWrapper<Competition> competitionQueryWrapper = new LambdaQueryWrapper<>();
        competitionQueryWrapper.in(Competition::getId, competitionIds)
                .orderByDesc(Competition::getCreateTime);

        return this.list(competitionQueryWrapper);
    }


}




