package com.miji.cms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.miji.cms.common.ErrorCode;
import com.miji.cms.exception.BusinessException;
import com.miji.cms.mapper.CompetitionMapper;
import com.miji.cms.mapper.TeamMapper;
import com.miji.cms.mapper.TeamMemberMapper;
import com.miji.cms.model.domain.Competition;
import com.miji.cms.model.domain.Team;
import com.miji.cms.model.domain.TeamMember;
import com.miji.cms.model.domain.User;
import com.miji.cms.model.request.TeamCreateRequest;
import com.miji.cms.model.request.TeamUpdateRequest;
import com.miji.cms.service.TeamMemberService;
import com.miji.cms.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * TeamServiceImpl 单元测试
 */
@ExtendWith(MockitoExtension.class)
class TeamServiceImplTest {

    @Mock
    private TeamMapper teamMapper;

    @Mock
    private CompetitionMapper competitionMapper;

    @Mock
    private TeamMemberMapper teamMemberMapper;

    @Mock
    private UserService userService;

    @Mock
    private TeamMemberService teamMemberService;

    @Mock
    private HttpServletRequest request;

    @Spy
    @InjectMocks
    private TeamServiceImpl teamService;

    private User loginUser;
    private Competition competition;
    private Team team;
    private TeamCreateRequest createRequest;

    @BeforeEach
    void setUp() {
        // 注入 baseMapper (TeamMapper)
        ReflectionTestUtils.setField(teamService, "baseMapper", teamMapper);

        // 初始化登录用户
        loginUser = new User();
        loginUser.setId(1L);
        loginUser.setUserName("testUser");

        // 初始化竞赛
        competition = new Competition();
        competition.setId(100L);
        competition.setMaxMembers(5);

        // 初始化队伍
        team = new Team();
        team.setId(1L);
        team.setCompetitionId(100L);
        team.setName("测试队伍");
        team.setUserId(1L);
        team.setMaxNum(5);
        team.setCurrentNum(1);
        team.setExpireTime(new Date(System.currentTimeMillis() + 86400000)); // 明天过期
        team.setIsDelete(0);

        // 初始化创建请求
        createRequest = new TeamCreateRequest();
        createRequest.setCompetitionId(100L);
        createRequest.setName("新队伍");
        createRequest.setDescription("队伍描述");
    }

    // ==================== createTeam 测试 ====================

    @Test
    void testCreateTeam_Success() {
        // Given
        when(userService.getLoginUser(request)).thenReturn(loginUser);
        when(competitionMapper.selectById(100L)).thenReturn(competition);
        when(teamMapper.insert(any(Team.class))).thenAnswer(invocation -> {
            Team t = invocation.getArgument(0);
            t.setId(1L);
            return 1;
        });
        when(teamMemberMapper.insert(any(TeamMember.class))).thenReturn(1);

        // When
        Long teamId = teamService.createTeam(createRequest, request);

        // Then
        assertNotNull(teamId);
        assertEquals(1L, teamId);
        verify(teamMapper, times(1)).insert(any(Team.class));
        verify(teamMemberMapper, times(1)).insert(any(TeamMember.class));
    }

    @Test
    void testCreateTeam_NullRequest() {
        // When & Then
        BusinessException exception = assertThrows(BusinessException.class,
                () -> teamService.createTeam(null, request));
        assertEquals(ErrorCode.PARAMS_ERROR.getCode(), exception.getCode());
        assertTrue(exception.getMessage().contains("请求参数错误"));
    }

    @Test
    void testCreateTeam_NullCompetitionId() {
        // Given
        createRequest.setCompetitionId(null);

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class,
                () -> teamService.createTeam(createRequest, request));
        assertEquals(ErrorCode.PARAMS_ERROR.getCode(), exception.getCode());
        assertTrue(exception.getMessage().contains("请求参数错误"));
    }

    @Test
    void testCreateTeam_NotLogin() {
        // Given
        when(userService.getLoginUser(request)).thenReturn(null);

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class,
                () -> teamService.createTeam(createRequest, request));
        assertEquals(ErrorCode.NOT_LOGIN.getCode(), exception.getCode());
    }

    @Test
    void testCreateTeam_CompetitionNotExist() {
        // Given
        when(userService.getLoginUser(request)).thenReturn(loginUser);
        when(competitionMapper.selectById(100L)).thenReturn(null);

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class,
                () -> teamService.createTeam(createRequest, request));
        assertEquals(ErrorCode.NULL_ERROR.getCode(), exception.getCode());
        assertTrue(exception.getMessage().contains("请求数据为空"));
    }

    @Test
    void testCreateTeam_IndividualCompetition() {
        // Given
        competition.setMaxMembers(1);
        when(userService.getLoginUser(request)).thenReturn(loginUser);
        when(competitionMapper.selectById(100L)).thenReturn(competition);

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class,
                () -> teamService.createTeam(createRequest, request));
        assertEquals(ErrorCode.SYSTEM_ERROR.getCode(), exception.getCode());
        assertTrue(exception.getMessage().contains("系统内部异常"));
    }

    @Test
    void testCreateTeam_EmptyTeamName() {
        // Given
        createRequest.setName("");
        when(userService.getLoginUser(request)).thenReturn(loginUser);
        when(competitionMapper.selectById(100L)).thenReturn(competition);

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class,
                () -> teamService.createTeam(createRequest, request));
        assertEquals(ErrorCode.PARAMS_ERROR.getCode(), exception.getCode());
        assertTrue(exception.getMessage().contains("请求参数错误"));
    }

    // ==================== joinTeam 测试 ====================

    @Test
    void testJoinTeam_Success() {
        // Given
        when(userService.getLoginUser(request)).thenReturn(loginUser);
        // 使用 doReturn 来避免调用真实的 getById 方法
        doReturn(team).when(teamService).getById(1L);
        // 第一次调用返回0(未加入),第二次调用返回2(当前队伍人数)
        when(teamMemberService.count(any(QueryWrapper.class))).thenReturn(0L).thenReturn(2L);
        when(teamMemberService.save(any(TeamMember.class))).thenReturn(true);

        // When
        boolean result = teamService.joinTeam(1L, request);

        // Then
        assertTrue(result);
        verify(teamMemberService, times(1)).save(any(TeamMember.class));
    }

    @Test
    void testJoinTeam_TeamNotExist() {
        // Given
        when(userService.getLoginUser(request)).thenReturn(loginUser);
        doReturn(null).when(teamService).getById(1L);

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class,
                () -> teamService.joinTeam(1L, request));
        assertEquals(ErrorCode.NULL_ERROR.getCode(), exception.getCode());
    }

    @Test
    void testJoinTeam_TeamDeleted() {
        // Given
        team.setIsDelete(1);
        when(userService.getLoginUser(request)).thenReturn(loginUser);
        doReturn(team).when(teamService).getById(1L);

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class,
                () -> teamService.joinTeam(1L, request));
        assertEquals(ErrorCode.NULL_ERROR.getCode(), exception.getCode());
    }

    @Test
    void testJoinTeam_TeamExpired() {
        // Given
        team.setExpireTime(new Date(System.currentTimeMillis() - 86400000)); // 昨天过期
        when(userService.getLoginUser(request)).thenReturn(loginUser);
        doReturn(team).when(teamService).getById(1L);

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class,
                () -> teamService.joinTeam(1L, request));
        assertEquals(ErrorCode.FORBIDDEN.getCode(), exception.getCode());
        assertTrue(exception.getMessage().contains("禁止操作"));
    }

    @Test
    void testJoinTeam_AlreadyJoined() {
        // Given
        when(userService.getLoginUser(request)).thenReturn(loginUser);
        doReturn(team).when(teamService).getById(1L);
        when(teamMemberService.count(any(QueryWrapper.class))).thenReturn(1L);

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class,
                () -> teamService.joinTeam(1L, request));
        assertEquals(ErrorCode.PARAMS_ERROR.getCode(), exception.getCode());
        assertTrue(exception.getMessage().contains("请求参数错误"));
    }

    @Test
    void testJoinTeam_TeamFull() {
        // Given
        team.setMaxNum(3);
        when(userService.getLoginUser(request)).thenReturn(loginUser);
        doReturn(team).when(teamService).getById(1L);
        when(teamMemberService.count(any(QueryWrapper.class))).thenReturn(0L).thenReturn(3L);

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class,
                () -> teamService.joinTeam(1L, request));
        assertEquals(ErrorCode.PARAMS_ERROR.getCode(), exception.getCode());
        assertTrue(exception.getMessage().contains("请求参数错误"));
    }

    // ==================== quitTeam 测试 ====================

    @Test
    void testQuitTeam_Success() {
        // Given
        TeamMember member = new TeamMember();
        member.setId(10L);
        member.setUserId(1L);
        member.setTeamId(1L);
        member.setRole(0); // 普通成员

        when(userService.getLoginUser(request)).thenReturn(loginUser);
        when(teamMemberService.getOne(any(QueryWrapper.class))).thenReturn(member);
        when(teamMemberService.removeById(10L)).thenReturn(true);

        // When
        boolean result = teamService.quitTeam(1L, request);

        // Then
        assertTrue(result);
        verify(teamMemberService, times(1)).removeById(10L);
    }

    @Test
    void testQuitTeam_NotInTeam() {
        // Given
        when(userService.getLoginUser(request)).thenReturn(loginUser);
        when(teamMemberService.getOne(any(QueryWrapper.class))).thenReturn(null);

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class,
                () -> teamService.quitTeam(1L, request));
        assertEquals(ErrorCode.PARAMS_ERROR.getCode(), exception.getCode());
        assertTrue(exception.getMessage().contains("请求参数错误"));
    }

    @Test
    void testQuitTeam_LeaderCannotQuit() {
        // Given
        TeamMember member = new TeamMember();
        member.setId(10L);
        member.setUserId(1L);
        member.setTeamId(1L);
        member.setRole(1); // 队长

        when(userService.getLoginUser(request)).thenReturn(loginUser);
        when(teamMemberService.getOne(any(QueryWrapper.class))).thenReturn(member);

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class,
                () -> teamService.quitTeam(1L, request));
        assertEquals(ErrorCode.PARAMS_ERROR.getCode(), exception.getCode());
        assertTrue(exception.getMessage().contains("请求参数错误"));
    }

    // ==================== deleteTeam 测试 ====================

    @Test
    void testDeleteTeam_Success_AsLeader() {
        // Given
        when(userService.getLoginUser(request)).thenReturn(loginUser);
        doReturn(team).when(teamService).getById(1L);
        when(teamMemberMapper.delete(any(QueryWrapper.class))).thenReturn(2);
        // 需要mock removeById，因为代码调用的是this.removeById()
        doReturn(true).when(teamService).removeById(1L);

        // When
        boolean result = teamService.deleteTeam(1L, request);

        // Then
        assertTrue(result);
        verify(teamMemberMapper, times(1)).delete(any(QueryWrapper.class));
        verify(teamService, times(1)).removeById(1L);
    }
    @Test
    void testDeleteTeam_Success_AsAdmin() {
        // Given
        User admin = new User();
        admin.setId(2L); // 不同的用户ID
        when(userService.getLoginUser(request)).thenReturn(admin);
        doReturn(team).when(teamService).getById(1L);
        when(userService.isAdmin(request)).thenReturn(true);
        doReturn(true).when(teamService).removeById(1L);
        when(teamMemberMapper.delete(any(QueryWrapper.class))).thenReturn(2);

        // When
        boolean result = teamService.deleteTeam(1L, request);

        // Then
        assertTrue(result);
    }

    @Test
    void testDeleteTeam_TeamNotExist() {
        // Given
        when(userService.getLoginUser(request)).thenReturn(loginUser);
        doReturn(null).when(teamService).getById(1L);

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class,
                () -> teamService.deleteTeam(1L, request));
        assertEquals(ErrorCode.NULL_ERROR.getCode(), exception.getCode());
    }

    @Test
    void testDeleteTeam_NoPermission() {
        // Given
        User otherUser = new User();
        otherUser.setId(2L);
        when(userService.getLoginUser(request)).thenReturn(otherUser);
        doReturn(team).when(teamService).getById(1L);
        when(userService.isAdmin(request)).thenReturn(false);

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class,
                () -> teamService.deleteTeam(1L, request));
        assertEquals(ErrorCode.NO_AUTH.getCode(), exception.getCode());
    }

    // ==================== updateTeam 测试 ====================

    @Test
    void testUpdateTeam_Success() {
        // Given
        TeamUpdateRequest updateRequest = new TeamUpdateRequest();
        updateRequest.setId(1L);
        updateRequest.setName("更新后的队伍名");

        when(userService.getLoginUser(request)).thenReturn(loginUser);
        doReturn(team).when(teamService).getById(1L);
        // 不需要mock isAdmin，因为队长本身就有权限
        when(teamMapper.updateById(any(Team.class))).thenReturn(1);

        // When
        boolean result = teamService.updateTeam(updateRequest, request);

        // Then
        assertTrue(result);
        verify(teamMapper, times(1)).updateById(any(Team.class));
    }
    @Test
    void testUpdateTeam_TeamNotExist() {
        // Given
        TeamUpdateRequest updateRequest = new TeamUpdateRequest();
        updateRequest.setId(1L);

        when(userService.getLoginUser(request)).thenReturn(loginUser);
        doReturn(null).when(teamService).getById(1L);

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class,
                () -> teamService.updateTeam(updateRequest, request));
        assertEquals(ErrorCode.NULL_ERROR.getCode(), exception.getCode());
    }

    @Test
    void testUpdateTeam_NoPermission() {
        // Given
        TeamUpdateRequest updateRequest = new TeamUpdateRequest();
        updateRequest.setId(1L);

        User otherUser = new User();
        otherUser.setId(2L);
        when(userService.getLoginUser(request)).thenReturn(otherUser);
        doReturn(team).when(teamService).getById(1L);
        when(userService.isAdmin(request)).thenReturn(false);

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class,
                () -> teamService.updateTeam(updateRequest, request));
        assertEquals(ErrorCode.NO_AUTH.getCode(), exception.getCode());
    }

    // ==================== getTeamDetail 测试 ====================

    @Test
    void testGetTeamDetail_Success() {
        // Given
        List<TeamMember> members = Arrays.asList(
                createTeamMember(1L, 1L, 1L, 1),
                createTeamMember(2L, 1L, 2L, 0)
        );
        doReturn(team).when(teamService).getById(1L);
        when(teamMemberService.list(any(QueryWrapper.class))).thenReturn(members);

        // When
        Map<String, Object> result = teamService.getTeamDetail(1L);

        // Then
        assertNotNull(result);
        assertEquals(team, result.get("team"));
        assertEquals(members, result.get("members"));
    }

    @Test
    void testGetTeamDetail_TeamNotExist() {
        // Given
        doReturn(null).when(teamService).getById(1L);

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class,
                () -> teamService.getTeamDetail(1L));
        assertEquals(ErrorCode.NULL_ERROR.getCode(), exception.getCode());
    }

    @Test
    void testGetTeamDetail_TeamDeleted() {
        // Given
        team.setIsDelete(1);
        doReturn(team).when(teamService).getById(1L);

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class,
                () -> teamService.getTeamDetail(1L));
        assertEquals(ErrorCode.NULL_ERROR.getCode(), exception.getCode());
    }

    // ==================== listTeams 测试 ====================

    @Test
    void testListTeams_Success() {
        // Given
        List<Team> teams = Arrays.asList(team);
        List<TeamMember> members = Arrays.asList(createTeamMember(1L, 1L, 1L, 1));

        doReturn(teams).when(teamService).list(any(QueryWrapper.class));
        when(teamMemberService.list(any(QueryWrapper.class))).thenReturn(members);

        // When
        List<Map<String, Object>> result = teamService.listTeams(100L);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).get("id"));
        assertEquals("测试队伍", result.get(0).get("name"));
        assertNotNull(result.get(0).get("members"));
    }

    @Test
    void testListTeams_EmptyResult() {
        // Given
        doReturn(new ArrayList<>()).when(teamService).list(any(QueryWrapper.class));

        // When
        List<Map<String, Object>> result = teamService.listTeams(100L);

        // Then
        assertNotNull(result);
        assertEquals(0, result.size());
    }

    // ==================== listMyTeams 测试 ====================

    @Test
    void testListMyTeams_Success() {
        // Given
        List<Team> createdTeams = Arrays.asList(team);
        List<TeamMember> memberships = Arrays.asList(createTeamMember(1L, 2L, 1L, 0));

        Team joinedTeam = new Team();
        joinedTeam.setId(2L);
        joinedTeam.setName("加入的队伍");
        joinedTeam.setCompetitionId(100L);
        joinedTeam.setUserId(3L);

        when(userService.getLoginUser(request)).thenReturn(loginUser);
        doReturn(createdTeams).when(teamService).list(any(QueryWrapper.class));
        when(teamMemberMapper.selectList(any(QueryWrapper.class))).thenReturn(memberships);
        doReturn(Arrays.asList(joinedTeam)).when(teamService).listByIds(anySet());
        when(teamMemberService.list(any(QueryWrapper.class))).thenReturn(new ArrayList<>());

        // When
        List<Map<String, Object>> result = teamService.listMyTeams(request);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
    }

    @Test
    void testListMyTeams_OnlyCreatedTeams() {
        // Given
        List<Team> createdTeams = Arrays.asList(team);

        when(userService.getLoginUser(request)).thenReturn(loginUser);
        doReturn(createdTeams).when(teamService).list(any(QueryWrapper.class));
        when(teamMemberMapper.selectList(any(QueryWrapper.class))).thenReturn(new ArrayList<>());
        when(teamMemberService.list(any(QueryWrapper.class))).thenReturn(new ArrayList<>());

        // When
        List<Map<String, Object>> result = teamService.listMyTeams(request);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
    }

    // ==================== createTeam边界测试 ====================

    @Test
    void testCreateTeam_InsertFailed() {
        when(userService.getLoginUser(request)).thenReturn(loginUser);
        when(competitionMapper.selectById(100L)).thenReturn(competition);
        when(teamMapper.insert(any(Team.class))).thenReturn(0);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> teamService.createTeam(createRequest, request));
        assertEquals(ErrorCode.SYSTEM_ERROR.getCode(), exception.getCode());
    }

    @Test
    void testCreateTeam_WithDescription() {
        createRequest.setDescription("详细描述");
        
        when(userService.getLoginUser(request)).thenReturn(loginUser);
        when(competitionMapper.selectById(100L)).thenReturn(competition);
        when(teamMapper.insert(any(Team.class))).thenAnswer(invocation -> {
            Team t = invocation.getArgument(0);
            t.setId(1L);
            return 1;
        });
        when(teamMemberMapper.insert(any(TeamMember.class))).thenReturn(1);

        Long teamId = teamService.createTeam(createRequest, request);

        assertNotNull(teamId);
        assertEquals(1L, teamId);
    }

    @Test
    void testCreateTeam_NullName() {
        createRequest.setName(null);
        when(userService.getLoginUser(request)).thenReturn(loginUser);
        when(competitionMapper.selectById(100L)).thenReturn(competition);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> teamService.createTeam(createRequest, request));
        assertEquals(ErrorCode.PARAMS_ERROR.getCode(), exception.getCode());
    }

    // ==================== joinTeam边界测试 ====================

    @Test
    void testJoinTeam_NotLogin() {
        when(userService.getLoginUser(request)).thenThrow(new BusinessException(ErrorCode.NOT_LOGIN));

        assertThrows(BusinessException.class, () -> teamService.joinTeam(1L, request));
    }

    @Test
    void testJoinTeam_NullExpireTime() {
        team.setExpireTime(null);
        when(userService.getLoginUser(request)).thenReturn(loginUser);
        doReturn(team).when(teamService).getById(1L);
        when(teamMemberService.count(any(QueryWrapper.class))).thenReturn(0L).thenReturn(2L);
        when(teamMemberService.save(any(TeamMember.class))).thenReturn(true);

        boolean result = teamService.joinTeam(1L, request);

        assertTrue(result);
    }

    // ==================== quitTeam边界测试 ====================

    @Test
    void testQuitTeam_NotLogin() {
        when(userService.getLoginUser(request)).thenThrow(new BusinessException(ErrorCode.NOT_LOGIN));

        assertThrows(BusinessException.class, () -> teamService.quitTeam(1L, request));
    }

    @Test
    void testQuitTeam_RemoveFailed() {
        TeamMember member = new TeamMember();
        member.setId(10L);
        member.setUserId(1L);
        member.setTeamId(1L);
        member.setRole(0);

        when(userService.getLoginUser(request)).thenReturn(loginUser);
        when(teamMemberService.getOne(any(QueryWrapper.class))).thenReturn(member);
        when(teamMemberService.removeById(10L)).thenReturn(false);

        boolean result = teamService.quitTeam(1L, request);

        assertFalse(result);
    }

    // ==================== deleteTeam边界测试 ====================

    @Test
    void testDeleteTeam_NotLogin() {
        when(userService.getLoginUser(request)).thenThrow(new BusinessException(ErrorCode.NOT_LOGIN));

        assertThrows(BusinessException.class, () -> teamService.deleteTeam(1L, request));
    }

    @Test
    void testDeleteTeam_RemoveFailed() {
        when(userService.getLoginUser(request)).thenReturn(loginUser);
        doReturn(team).when(teamService).getById(1L);
        when(teamMemberMapper.delete(any(QueryWrapper.class))).thenReturn(2);
        doReturn(false).when(teamService).removeById(1L);

        boolean result = teamService.deleteTeam(1L, request);

        assertFalse(result);
    }

    // ==================== updateTeam边界测试 ====================

    @Test
    void testUpdateTeam_NotLogin() {
        when(userService.getLoginUser(request)).thenThrow(new BusinessException(ErrorCode.NOT_LOGIN));

        TeamUpdateRequest updateRequest = new TeamUpdateRequest();
        updateRequest.setId(1L);

        assertThrows(BusinessException.class, () -> teamService.updateTeam(updateRequest, request));
    }

    @Test
    void testUpdateTeam_NullId() {
        TeamUpdateRequest updateRequest = new TeamUpdateRequest();
        updateRequest.setId(null);

        when(userService.getLoginUser(request)).thenReturn(loginUser);
        doReturn(null).when(teamService).getById(null);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> teamService.updateTeam(updateRequest, request));
        assertEquals(ErrorCode.NULL_ERROR.getCode(), exception.getCode());
    }

    @Test
    void testUpdateTeam_Success_AsAdmin() {
        TeamUpdateRequest updateRequest = new TeamUpdateRequest();
        updateRequest.setId(1L);
        updateRequest.setName("管理员更新的名称");

        User admin = new User();
        admin.setId(2L);
        when(userService.getLoginUser(request)).thenReturn(admin);
        doReturn(team).when(teamService).getById(1L);
        when(userService.isAdmin(request)).thenReturn(true);
        when(teamMapper.updateById(any(Team.class))).thenReturn(1);

        boolean result = teamService.updateTeam(updateRequest, request);

        assertTrue(result);
    }

    @Test
    void testUpdateTeam_Failed() {
        TeamUpdateRequest updateRequest = new TeamUpdateRequest();
        updateRequest.setId(1L);
        updateRequest.setName("更新后的队伍名");

        when(userService.getLoginUser(request)).thenReturn(loginUser);
        doReturn(team).when(teamService).getById(1L);
        when(teamMapper.updateById(any(Team.class))).thenReturn(0);

        boolean result = teamService.updateTeam(updateRequest, request);

        assertFalse(result);
    }

    // ==================== listMyTeams边界测试 ====================

    @Test
    void testListMyTeams_NotLogin() {
        when(userService.getLoginUser(request)).thenThrow(new BusinessException(ErrorCode.NOT_LOGIN));

        assertThrows(BusinessException.class, () -> teamService.listMyTeams(request));
    }

    @Test
    void testListMyTeams_EmptyJoinedTeams() {
        when(userService.getLoginUser(request)).thenReturn(loginUser);
        doReturn(new ArrayList<>()).when(teamService).list(any(QueryWrapper.class));
        when(teamMemberMapper.selectList(any(QueryWrapper.class))).thenReturn(new ArrayList<>());

        List<Map<String, Object>> result = teamService.listMyTeams(request);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    // ==================== listTeams边界测试 ====================

    @Test
    void testListTeams_WithMultipleTeams() {
        Team team2 = new Team();
        team2.setId(2L);
        team2.setName("第二个队伍");
        team2.setCompetitionId(100L);
        team2.setUserId(2L);
        team2.setMaxNum(5);
        team2.setCurrentNum(3);

        List<Team> teams = Arrays.asList(team, team2);

        doReturn(teams).when(teamService).list(any(QueryWrapper.class));
        when(teamMemberService.list(any(QueryWrapper.class))).thenReturn(new ArrayList<>());

        List<Map<String, Object>> result = teamService.listTeams(100L);

        assertNotNull(result);
        assertEquals(2, result.size());
    }

    // ==================== 实体测试 ====================

    @Test
    void testTeam_AllFields() {
        Team t = new Team();
        Date now = new Date();

        t.setId(1L);
        t.setCompetitionId(100L);
        t.setName("测试队伍");
        t.setDescription("队伍描述");
        t.setUserId(10L);
        t.setMaxNum(5);
        t.setCurrentNum(3);
        t.setExpireTime(now);
        t.setCreateTime(now);
        t.setUpdateTime(now);
        t.setIsDelete(0);

        assertEquals(1L, t.getId());
        assertEquals(100L, t.getCompetitionId());
        assertEquals("测试队伍", t.getName());
        assertEquals("队伍描述", t.getDescription());
        assertEquals(10L, t.getUserId());
        assertEquals(5, t.getMaxNum());
        assertEquals(3, t.getCurrentNum());
        assertEquals(now, t.getExpireTime());
        assertEquals(now, t.getCreateTime());
        assertEquals(now, t.getUpdateTime());
        assertEquals(0, t.getIsDelete());
    }

    @Test
    void testTeamMember_AllFields() {
        TeamMember m = new TeamMember();
        Date now = new Date();

        m.setId(1L);
        m.setTeamId(10L);
        m.setUserId(20L);
        m.setRole(1);
        m.setCreateTime(now);
        m.setUpdateTime(now);
        m.setIsDelete(0);

        assertEquals(1L, m.getId());
        assertEquals(10L, m.getTeamId());
        assertEquals(20L, m.getUserId());
        assertEquals(1, m.getRole());
        assertEquals(now, m.getCreateTime());
        assertEquals(now, m.getUpdateTime());
        assertEquals(0, m.getIsDelete());
    }

    // ==================== 辅助方法 ====================

    private TeamMember createTeamMember(Long id, Long teamId, Long userId, Integer role) {
        TeamMember member = new TeamMember();
        member.setId(id);
        member.setTeamId(teamId);
        member.setUserId(userId);
        member.setRole(role);
        return member;
    }
}