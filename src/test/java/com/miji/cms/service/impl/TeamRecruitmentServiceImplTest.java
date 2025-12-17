package com.miji.cms.service.impl;

import com.miji.cms.common.ErrorCode;
import com.miji.cms.exception.BusinessException;
import com.miji.cms.mapper.TeamMapper;
import com.miji.cms.mapper.TeamRecruitmentMapper;
import com.miji.cms.model.domain.Competition;
import com.miji.cms.model.domain.Team;
import com.miji.cms.model.domain.TeamRecruitment;
import com.miji.cms.model.domain.User;
import com.miji.cms.model.request.RecruitmentCreateRequest;
import com.miji.cms.model.request.RecruitmentQueryRequest;
import com.miji.cms.service.CompetitionService;
import com.miji.cms.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.util.ReflectionTestUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * TeamRecruitmentServiceImpl 单元测试
 */
@ExtendWith(MockitoExtension.class)
class TeamRecruitmentServiceImplTest {

    @Mock
    private TeamRecruitmentMapper recruitmentMapper;

    @Mock
    private TeamMapper teamMapper;

    @Mock
    private UserService userService;

    @Mock
    private CompetitionService competitionService;

    @Spy
    @InjectMocks
    private TeamRecruitmentServiceImpl recruitmentService;

    private User loginUser;
    private User adminUser;
    private Competition competition;
    private Team team;
    private TeamRecruitment recruitment;
    private RecruitmentCreateRequest createRequest;
    private MockHttpServletRequest httpRequest;

    @BeforeEach
    void setUp() {
        // 注入 baseMapper
        ReflectionTestUtils.setField(recruitmentService, "baseMapper", recruitmentMapper);

        // 初始化登录用户
        loginUser = new User();
        loginUser.setId(1L);
        loginUser.setUserName("testUser");
        loginUser.setUserRole(0);

        // 初始化管理员用户
        adminUser = new User();
        adminUser.setId(2L);
        adminUser.setUserName("adminUser");
        adminUser.setUserRole(1);

        // 初始化竞赛
        competition = new Competition();
        competition.setId(100L);
        competition.setName("测试竞赛");

        // 初始化队伍
        team = new Team();
        team.setId(10L);
        team.setUserId(1L); // 队长ID
        team.setName("测试队伍");
        team.setIsDelete(0);

        // 初始化招募令
        recruitment = new TeamRecruitment();
        recruitment.setId(1L);
        recruitment.setUserId(1L);
        recruitment.setCompetitionId(100L);
        recruitment.setTeamId(null);
        recruitment.setIsTeam(0);
        recruitment.setTitle("寻找队友");
        recruitment.setDescription("寻找一起参赛的队友");
        recruitment.setContact("QQ:123456");
        recruitment.setMaxMembers(3);
        recruitment.setStatus(0);
        recruitment.setCreateTime(new Date());
        recruitment.setUpdateTime(new Date());
        recruitment.setIsDelete(0);

        // 初始化创建请求
        createRequest = new RecruitmentCreateRequest();
        createRequest.setCompetitionId(100L);
        createRequest.setTitle("招募队友");
        createRequest.setDescription("寻找队友一起参赛");
        createRequest.setContact("微信:test123");
        createRequest.setIsTeam(0);
        createRequest.setMaxMembers(2);

        // 初始化 HTTP 请求
        httpRequest = new MockHttpServletRequest();
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("userLoginState", loginUser);
        httpRequest.setSession(session);
    }

    // ==================== createRecruitment 测试 ====================

    @Test
    void testCreateRecruitment_Success_Personal() {
        // Given
        when(userService.getLoginUser(any())).thenReturn(loginUser);
        when(competitionService.getById(100L)).thenReturn(competition);
        when(recruitmentMapper.insert(any(TeamRecruitment.class))).thenAnswer(invocation -> {
            TeamRecruitment r = invocation.getArgument(0);
            r.setId(1L);
            return 1;
        });

        // When
        Long recruitmentId = recruitmentService.createRecruitment(createRequest, httpRequest);

        // Then
        assertNotNull(recruitmentId);
        assertEquals(1L, recruitmentId);
        verify(recruitmentMapper, times(1)).insert(any(TeamRecruitment.class));
    }

    @Test
    void testCreateRecruitment_Success_Team() {
        // Given
        createRequest.setIsTeam(1);
        createRequest.setTeamId(10L);

        when(userService.getLoginUser(any())).thenReturn(loginUser);
        when(teamMapper.selectById(10L)).thenReturn(team);
        when(competitionService.getById(100L)).thenReturn(competition);
        when(recruitmentMapper.insert(any(TeamRecruitment.class))).thenAnswer(invocation -> {
            TeamRecruitment r = invocation.getArgument(0);
            r.setId(1L);
            return 1;
        });

        // When
        Long recruitmentId = recruitmentService.createRecruitment(createRequest, httpRequest);

        // Then
        assertNotNull(recruitmentId);
        assertEquals(1L, recruitmentId);
    }

    @Test
    void testCreateRecruitment_NullRequest() {
        // When & Then
        BusinessException exception = assertThrows(BusinessException.class,
                () -> recruitmentService.createRecruitment(null, httpRequest));
        assertEquals(ErrorCode.PARAMS_ERROR.getCode(), exception.getCode());
    }

    @Test
    void testCreateRecruitment_NullCompetitionId() {
        // Given
        createRequest.setCompetitionId(null);

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class,
                () -> recruitmentService.createRecruitment(createRequest, httpRequest));
        assertEquals(ErrorCode.PARAMS_ERROR.getCode(), exception.getCode());
    }

    @Test
    void testCreateRecruitment_NullTitle() {
        // Given
        createRequest.setTitle(null);

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class,
                () -> recruitmentService.createRecruitment(createRequest, httpRequest));
        assertEquals(ErrorCode.PARAMS_ERROR.getCode(), exception.getCode());
    }

    @Test
    void testCreateRecruitment_NotLogin() {
        // Given
        when(userService.getLoginUser(any())).thenReturn(null);

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class,
                () -> recruitmentService.createRecruitment(createRequest, httpRequest));
        assertEquals(ErrorCode.NOT_LOGIN.getCode(), exception.getCode());
    }

    @Test
    void testCreateRecruitment_TeamNotFound() {
        // Given
        createRequest.setIsTeam(1);
        createRequest.setTeamId(999L);

        when(userService.getLoginUser(any())).thenReturn(loginUser);
        when(teamMapper.selectById(999L)).thenReturn(null);

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class,
                () -> recruitmentService.createRecruitment(createRequest, httpRequest));
        assertEquals(ErrorCode.PARAMS_ERROR.getCode(), exception.getCode());
    }

    @Test
    void testCreateRecruitment_TeamDeleted() {
        // Given
        createRequest.setIsTeam(1);
        createRequest.setTeamId(10L);
        team.setIsDelete(1);

        when(userService.getLoginUser(any())).thenReturn(loginUser);
        when(teamMapper.selectById(10L)).thenReturn(team);

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class,
                () -> recruitmentService.createRecruitment(createRequest, httpRequest));
        assertEquals(ErrorCode.PARAMS_ERROR.getCode(), exception.getCode());
    }

    @Test
    void testCreateRecruitment_NotTeamCaptain() {
        // Given
        createRequest.setIsTeam(1);
        createRequest.setTeamId(10L);
        team.setUserId(999L); // 队长不是当前用户

        when(userService.getLoginUser(any())).thenReturn(loginUser);
        when(teamMapper.selectById(10L)).thenReturn(team);

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class,
                () -> recruitmentService.createRecruitment(createRequest, httpRequest));
        assertEquals(ErrorCode.NO_AUTH.getCode(), exception.getCode());
    }

    @Test
    void testCreateRecruitment_CompetitionNotFound() {
        // Given
        when(userService.getLoginUser(any())).thenReturn(loginUser);
        when(competitionService.getById(100L)).thenReturn(null);

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class,
                () -> recruitmentService.createRecruitment(createRequest, httpRequest));
        assertEquals(ErrorCode.PARAMS_ERROR.getCode(), exception.getCode());
    }

    @Test
    void testCreateRecruitment_TeamIdRequired() {
        // Given - 代表队伍发布但没有传 teamId
        createRequest.setIsTeam(1);
        createRequest.setTeamId(null);

        when(userService.getLoginUser(any())).thenReturn(loginUser);

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class,
                () -> recruitmentService.createRecruitment(createRequest, httpRequest));
        assertEquals(ErrorCode.PARAMS_ERROR.getCode(), exception.getCode());
    }

    // ==================== updateRecruitment 测试 ====================

    @Test
    void testUpdateRecruitment_Success() {
        // Given
        TeamRecruitment updateRequest = new TeamRecruitment();
        updateRequest.setId(1L);
        updateRequest.setTitle("更新后的标题");
        updateRequest.setDescription("更新后的描述");
        updateRequest.setContact("新联系方式");

        when(recruitmentMapper.selectById(1L)).thenReturn(recruitment);
        when(recruitmentMapper.updateById(any(TeamRecruitment.class))).thenReturn(1);

        // When
        boolean result = recruitmentService.updateRecruitment(updateRequest, httpRequest);

        // Then
        assertTrue(result);
        verify(recruitmentMapper, times(1)).updateById(any(TeamRecruitment.class));
    }

    @Test
    void testUpdateRecruitment_NullRequest() {
        // When & Then
        BusinessException exception = assertThrows(BusinessException.class,
                () -> recruitmentService.updateRecruitment(null, httpRequest));
        assertEquals(ErrorCode.PARAMS_ERROR.getCode(), exception.getCode());
    }

    @Test
    void testUpdateRecruitment_NullId() {
        // Given
        TeamRecruitment updateRequest = new TeamRecruitment();
        updateRequest.setTitle("标题");

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class,
                () -> recruitmentService.updateRecruitment(updateRequest, httpRequest));
        assertEquals(ErrorCode.PARAMS_ERROR.getCode(), exception.getCode());
    }

    @Test
    void testUpdateRecruitment_NotLogin() {
        // Given
        TeamRecruitment updateRequest = new TeamRecruitment();
        updateRequest.setId(1L);
        MockHttpServletRequest noLoginRequest = new MockHttpServletRequest();

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class,
                () -> recruitmentService.updateRecruitment(updateRequest, noLoginRequest));
        assertEquals(ErrorCode.NOT_LOGIN.getCode(), exception.getCode());
    }

    @Test
    void testUpdateRecruitment_NotFound() {
        // Given
        TeamRecruitment updateRequest = new TeamRecruitment();
        updateRequest.setId(999L);

        when(recruitmentMapper.selectById(999L)).thenReturn(null);

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class,
                () -> recruitmentService.updateRecruitment(updateRequest, httpRequest));
        assertEquals(ErrorCode.NULL_ERROR.getCode(), exception.getCode());
    }

    @Test
    void testUpdateRecruitment_Deleted() {
        // Given
        TeamRecruitment updateRequest = new TeamRecruitment();
        updateRequest.setId(1L);
        recruitment.setIsDelete(1);

        when(recruitmentMapper.selectById(1L)).thenReturn(recruitment);

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class,
                () -> recruitmentService.updateRecruitment(updateRequest, httpRequest));
        assertEquals(ErrorCode.NULL_ERROR.getCode(), exception.getCode());
    }

    @Test
    void testUpdateRecruitment_NoAuth() {
        // Given
        TeamRecruitment updateRequest = new TeamRecruitment();
        updateRequest.setId(1L);
        recruitment.setUserId(999L); // 不是当前用户发布的

        when(recruitmentMapper.selectById(1L)).thenReturn(recruitment);

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class,
                () -> recruitmentService.updateRecruitment(updateRequest, httpRequest));
        assertEquals(ErrorCode.NO_AUTH.getCode(), exception.getCode());
    }

    // ==================== deleteRecruitment 测试 ====================

    @Test
    void testDeleteRecruitment_Success_ByOwner() {
        // Given
        when(recruitmentMapper.selectById(1L)).thenReturn(recruitment);
        doReturn(true).when(recruitmentService).removeById(1L);

        // When
        boolean result = recruitmentService.deleteRecruitment(1L, httpRequest);

        // Then
        assertTrue(result);
    }

    @Test
    void testDeleteRecruitment_Success_ByAdmin() {
        // Given
        recruitment.setUserId(999L); // 不是当前用户发布的
        MockHttpSession adminSession = new MockHttpSession();
        adminSession.setAttribute("userLoginState", adminUser);
        httpRequest.setSession(adminSession);

        when(recruitmentMapper.selectById(1L)).thenReturn(recruitment);
        when(userService.isAdmin(any(HttpServletRequest.class))).thenReturn(true);
        doReturn(true).when(recruitmentService).removeById(1L);

        // When
        boolean result = recruitmentService.deleteRecruitment(1L, httpRequest);

        // Then
        assertTrue(result);
    }

    @Test
    void testDeleteRecruitment_NullId() {
        // When & Then
        BusinessException exception = assertThrows(BusinessException.class,
                () -> recruitmentService.deleteRecruitment(null, httpRequest));
        assertEquals(ErrorCode.PARAMS_ERROR.getCode(), exception.getCode());
    }

    @Test
    void testDeleteRecruitment_NotLogin() {
        // Given
        MockHttpServletRequest noLoginRequest = new MockHttpServletRequest();

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class,
                () -> recruitmentService.deleteRecruitment(1L, noLoginRequest));
        assertEquals(ErrorCode.NOT_LOGIN.getCode(), exception.getCode());
    }

    @Test
    void testDeleteRecruitment_NotFound() {
        // Given
        when(recruitmentMapper.selectById(999L)).thenReturn(null);

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class,
                () -> recruitmentService.deleteRecruitment(999L, httpRequest));
        assertEquals(ErrorCode.NULL_ERROR.getCode(), exception.getCode());
    }

    @Test
    void testDeleteRecruitment_NoAuth() {
        // Given
        recruitment.setUserId(999L); // 不是当前用户发布的

        when(recruitmentMapper.selectById(1L)).thenReturn(recruitment);
        when(userService.isAdmin(any(HttpServletRequest.class))).thenReturn(false);

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class,
                () -> recruitmentService.deleteRecruitment(1L, httpRequest));
        assertEquals(ErrorCode.NO_AUTH.getCode(), exception.getCode());
    }

    // ==================== listRecruitments 测试 ====================

    @Test
    void testListRecruitments_Success() {
        // Given
        RecruitmentQueryRequest queryRequest = new RecruitmentQueryRequest();
        queryRequest.setCompetitionId(100L);

        List<TeamRecruitment> recruitmentList = Arrays.asList(recruitment);
        when(recruitmentMapper.selectList(any())).thenReturn(recruitmentList);

        // When
        List<TeamRecruitment> result = recruitmentService.listRecruitments(queryRequest, httpRequest);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("寻找队友", result.get(0).getTitle());
    }

    @Test
    void testListRecruitments_FilterByIsTeam() {
        // Given
        RecruitmentQueryRequest queryRequest = new RecruitmentQueryRequest();
        queryRequest.setIsTeam(1);

        when(recruitmentMapper.selectList(any())).thenReturn(Arrays.asList());

        // When
        List<TeamRecruitment> result = recruitmentService.listRecruitments(queryRequest, httpRequest);

        // Then
        assertNotNull(result);
        assertEquals(0, result.size());
    }

    // ==================== getRecruitmentDetail 测试 ====================

    @Test
    void testGetRecruitmentDetail_Success() {
        // Given
        when(recruitmentMapper.selectById(1L)).thenReturn(recruitment);

        // When
        TeamRecruitment result = recruitmentService.getRecruitmentDetail(1L, httpRequest);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("寻找队友", result.getTitle());
    }

    @Test
    void testGetRecruitmentDetail_NullId() {
        // When & Then
        BusinessException exception = assertThrows(BusinessException.class,
                () -> recruitmentService.getRecruitmentDetail(null, httpRequest));
        assertEquals(ErrorCode.PARAMS_ERROR.getCode(), exception.getCode());
    }

    @Test
    void testGetRecruitmentDetail_NotFound() {
        // Given
        when(recruitmentMapper.selectById(999L)).thenReturn(null);

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class,
                () -> recruitmentService.getRecruitmentDetail(999L, httpRequest));
        assertEquals(ErrorCode.NULL_ERROR.getCode(), exception.getCode());
    }

    @Test
    void testGetRecruitmentDetail_Deleted() {
        // Given
        recruitment.setIsDelete(1);
        when(recruitmentMapper.selectById(1L)).thenReturn(recruitment);

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class,
                () -> recruitmentService.getRecruitmentDetail(1L, httpRequest));
        assertEquals(ErrorCode.NULL_ERROR.getCode(), exception.getCode());
    }

    // ==================== 实体类测试 ====================

    @Test
    void testTeamRecruitment_ContentMapping() {
        // 测试 content 和 description 的映射
        TeamRecruitment r = new TeamRecruitment();
        r.setContent("测试内容");
        assertEquals("测试内容", r.getDescription());
        assertEquals("测试内容", r.getContent());

        r.setDescription("另一个描述");
        assertEquals("另一个描述", r.getContent());
    }

    // ==================== 创建招募令边界测试 ====================
    @Test
    void testCreateRecruitment_NullDescription() {
        createRequest.setDescription(null);
        
        when(userService.getLoginUser(any())).thenReturn(loginUser);
        when(competitionService.getById(100L)).thenReturn(competition);
        when(recruitmentMapper.insert(any(TeamRecruitment.class))).thenAnswer(invocation -> {
            TeamRecruitment r = invocation.getArgument(0);
            r.setId(1L);
            return 1;
        });

        Long recruitmentId = recruitmentService.createRecruitment(createRequest, httpRequest);

        assertNotNull(recruitmentId);
    }

    @Test
    void testCreateRecruitment_NullContact() {
        createRequest.setContact(null);
        
        when(userService.getLoginUser(any())).thenReturn(loginUser);
        when(competitionService.getById(100L)).thenReturn(competition);
        when(recruitmentMapper.insert(any(TeamRecruitment.class))).thenAnswer(invocation -> {
            TeamRecruitment r = invocation.getArgument(0);
            r.setId(1L);
            return 1;
        });

        Long recruitmentId = recruitmentService.createRecruitment(createRequest, httpRequest);

        assertNotNull(recruitmentId);
    }

    @Test
    void testCreateRecruitment_NullIsTeam() {
        createRequest.setIsTeam(null);
        
        when(userService.getLoginUser(any())).thenReturn(loginUser);
        when(competitionService.getById(100L)).thenReturn(competition);
        when(recruitmentMapper.insert(any(TeamRecruitment.class))).thenAnswer(invocation -> {
            TeamRecruitment r = invocation.getArgument(0);
            r.setId(1L);
            return 1;
        });

        Long recruitmentId = recruitmentService.createRecruitment(createRequest, httpRequest);

        assertNotNull(recruitmentId);
    }

    @Test
    void testCreateRecruitment_InsertFailed() {
        when(userService.getLoginUser(any())).thenReturn(loginUser);
        when(competitionService.getById(100L)).thenReturn(competition);
        when(recruitmentMapper.insert(any(TeamRecruitment.class))).thenReturn(0);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> recruitmentService.createRecruitment(createRequest, httpRequest));
        assertEquals(ErrorCode.SYSTEM_ERROR.getCode(), exception.getCode());
    }

    // ==================== 更新招募令边界测试 ====================
    @Test
    void testUpdateRecruitment_UpdateFailed() {
        TeamRecruitment updateRequest = new TeamRecruitment();
        updateRequest.setId(1L);
        updateRequest.setTitle("更新后的标题");

        when(recruitmentMapper.selectById(1L)).thenReturn(recruitment);
        when(recruitmentMapper.updateById(any(TeamRecruitment.class))).thenReturn(0);

        boolean result = recruitmentService.updateRecruitment(updateRequest, httpRequest);

        assertFalse(result);
    }

    @Test
    void testUpdateRecruitment_PartialUpdate() {
        TeamRecruitment updateRequest = new TeamRecruitment();
        updateRequest.setId(1L);
        updateRequest.setTitle("新标题");
        // description和contact为null

        when(recruitmentMapper.selectById(1L)).thenReturn(recruitment);
        when(recruitmentMapper.updateById(any(TeamRecruitment.class))).thenReturn(1);

        boolean result = recruitmentService.updateRecruitment(updateRequest, httpRequest);

        assertTrue(result);
    }

    // ==================== 删除招募令边界测试 ====================
    @Test
    void testDeleteRecruitment_DeletedRecruitment() {
        recruitment.setIsDelete(1);
        when(recruitmentMapper.selectById(1L)).thenReturn(recruitment);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> recruitmentService.deleteRecruitment(1L, httpRequest));
        assertEquals(ErrorCode.NULL_ERROR.getCode(), exception.getCode());
    }

    @Test
    void testDeleteRecruitment_RemoveFailed() {
        when(recruitmentMapper.selectById(1L)).thenReturn(recruitment);
        doReturn(false).when(recruitmentService).removeById(1L);

        boolean result = recruitmentService.deleteRecruitment(1L, httpRequest);

        assertFalse(result);
    }

    // ==================== 查询招募令列表边界测试 ====================
    @Test
    void testListRecruitments_NullQueryRequest() {
        RecruitmentQueryRequest queryRequest = new RecruitmentQueryRequest();
        // 所有条件为null

        when(recruitmentMapper.selectList(any())).thenReturn(Arrays.asList(recruitment));

        List<TeamRecruitment> result = recruitmentService.listRecruitments(queryRequest, httpRequest);

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void testListRecruitments_EmptyResult() {
        RecruitmentQueryRequest queryRequest = new RecruitmentQueryRequest();
        queryRequest.setCompetitionId(999L);

        when(recruitmentMapper.selectList(any())).thenReturn(Arrays.asList());

        List<TeamRecruitment> result = recruitmentService.listRecruitments(queryRequest, httpRequest);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    // ==================== 招募令实体测试 ====================
    @Test
    void testTeamRecruitment_AllFields() {
        TeamRecruitment r = new TeamRecruitment();
        Date now = new Date();
        
        r.setId(1L);
        r.setUserId(10L);
        r.setCompetitionId(100L);
        r.setTeamId(5L);
        r.setIsTeam(1);
        r.setTitle("测试标题");
        r.setDescription("测试描述");
        r.setContact("QQ:123456");
        r.setMaxMembers(3);
        r.setStatus(0);
        r.setCreateTime(now);
        r.setUpdateTime(now);
        r.setIsDelete(0);

        assertEquals(1L, r.getId());
        assertEquals(10L, r.getUserId());
        assertEquals(100L, r.getCompetitionId());
        assertEquals(5L, r.getTeamId());
        assertEquals(1, r.getIsTeam());
        assertEquals("测试标题", r.getTitle());
        assertEquals("测试描述", r.getDescription());
        assertEquals("QQ:123456", r.getContact());
        assertEquals(3, r.getMaxMembers());
        assertEquals(0, r.getStatus());
        assertEquals(now, r.getCreateTime());
        assertEquals(now, r.getUpdateTime());
        assertEquals(0, r.getIsDelete());
    }
}
