package com.miji.cms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.miji.cms.common.ErrorCode;
import com.miji.cms.constant.UserConstant;
import com.miji.cms.exception.BusinessException;
import com.miji.cms.mapper.CompetitionMapper;
import com.miji.cms.mapper.CompetitionRegistrationMapper;
import com.miji.cms.mapper.TeamMapper;
import com.miji.cms.mapper.TeamMemberMapper;
import com.miji.cms.model.domain.*;
import com.miji.cms.model.request.CompetitionCreateRequest;
import com.miji.cms.model.request.CompetitionRegisterRequest;
import com.miji.cms.model.request.CompetitionReviewRequest;
import com.miji.cms.model.request.CompetitionUpdateRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * CompetitionServiceImpl 单元测试
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("竞赛服务测试")
class CompetitionServiceImplTest {

    @Mock
    private CompetitionMapper competitionMapper;

    @Mock
    private CompetitionRegistrationMapper competitionRegistrationMapper;

    @Mock
    private TeamMapper teamMapper;

    @Mock
    private TeamMemberMapper teamMemberMapper;

    @Mock
    private HttpServletRequest httpRequest;

    @Mock
    private HttpSession session;

    @Spy
    @InjectMocks
    private CompetitionServiceImpl competitionService;

    private User adminUser;
    private User normalUser;
    private Competition testCompetition;

    @BeforeEach
    void setUp() {
        adminUser = new User();
        adminUser.setId(1L);
        adminUser.setUserName("admin");
        adminUser.setUserRole(1);

        normalUser = new User();
        normalUser.setId(2L);
        normalUser.setUserName("user");
        normalUser.setUserRole(0);

        testCompetition = new Competition();
        testCompetition.setId(1L);
        testCompetition.setName("测试竞赛");
        testCompetition.setSummary("这是一个测试竞赛");
        testCompetition.setOrganizer("测试组织");
        testCompetition.setCreatorId(1L);
        testCompetition.setMaxMembers(5);
        testCompetition.setIsDelete(0);
        testCompetition.setCreateTime(new Date());
    }

    @Nested
    @DisplayName("创建竞赛测试")
    class AddCompetitionTests {

        @Test
        @DisplayName("管理员成功创建竞赛")
        void testAddCompetition_Success() {
            when(httpRequest.getSession()).thenReturn(session);
            when(session.getAttribute(UserConstant.USER_LOGIN_STATE)).thenReturn(adminUser);

            doAnswer(invocation -> {
                Competition comp = invocation.getArgument(0);
                comp.setId(1L);
                return true;
            }).when(competitionService).save(any(Competition.class));

            CompetitionCreateRequest request = new CompetitionCreateRequest();
            request.setName("新竞赛");
            request.setSummary("竞赛描述");
            request.setOrganizer("组织者");

            long result = competitionService.addCompetition(request, httpRequest);

            assertEquals(1L, result);
            verify(competitionService, times(1)).save(any(Competition.class));
        }

        @Test
        @DisplayName("普通用户创建竞赛失败 - 无权限")
        void testAddCompetition_NoAuth() {
            when(httpRequest.getSession()).thenReturn(session);
            when(session.getAttribute(UserConstant.USER_LOGIN_STATE)).thenReturn(normalUser);

            CompetitionCreateRequest request = new CompetitionCreateRequest();
            request.setName("新竞赛");
            request.setSummary("竞赛描述");
            request.setOrganizer("组织者");

            BusinessException exception = assertThrows(BusinessException.class,
                    () -> competitionService.addCompetition(request, httpRequest));
            assertEquals(ErrorCode.NO_AUTH.getCode(), exception.getCode());
        }

        @Test
        @DisplayName("未登录用户创建竞赛失败")
        void testAddCompetition_NotLogin() {
            when(httpRequest.getSession()).thenReturn(session);
            when(session.getAttribute(UserConstant.USER_LOGIN_STATE)).thenReturn(null);

            CompetitionCreateRequest request = new CompetitionCreateRequest();
            request.setName("新竞赛");

            BusinessException exception = assertThrows(BusinessException.class,
                    () -> competitionService.addCompetition(request, httpRequest));
            assertEquals(ErrorCode.NO_AUTH.getCode(), exception.getCode());
        }

        @Test
        @DisplayName("参数不完整创建竞赛失败")
        void testAddCompetition_InvalidParams() {
            when(httpRequest.getSession()).thenReturn(session);
            when(session.getAttribute(UserConstant.USER_LOGIN_STATE)).thenReturn(adminUser);

            CompetitionCreateRequest request = new CompetitionCreateRequest();
            request.setName("");
            request.setSummary("描述");
            request.setOrganizer("组织者");

            BusinessException exception = assertThrows(BusinessException.class,
                    () -> competitionService.addCompetition(request, httpRequest));
            assertEquals(ErrorCode.PARAMS_ERROR.getCode(), exception.getCode());
        }
    }

    @Nested
    @DisplayName("更新竞赛测试")
    class UpdateCompetitionTests {

        @Test
        @DisplayName("创建者成功更新竞赛")
        void testUpdateCompetition_Success() {
            when(httpRequest.getSession()).thenReturn(session);
            when(session.getAttribute(UserConstant.USER_LOGIN_STATE)).thenReturn(adminUser);
            doReturn(testCompetition).when(competitionService).getById(1L);
            doReturn(true).when(competitionService).updateById(any(Competition.class));

            CompetitionUpdateRequest request = new CompetitionUpdateRequest();
            request.setId(1L);
            request.setName("更新后的竞赛名称");

            boolean result = competitionService.updateCompetition(request, httpRequest);

            assertTrue(result);
            verify(competitionService, times(1)).updateById(any(Competition.class));
        }

        @Test
        @DisplayName("非创建者更新竞赛失败")
        void testUpdateCompetition_NoAuth() {
            when(httpRequest.getSession()).thenReturn(session);
            when(session.getAttribute(UserConstant.USER_LOGIN_STATE)).thenReturn(normalUser);
            doReturn(testCompetition).when(competitionService).getById(1L);

            CompetitionUpdateRequest request = new CompetitionUpdateRequest();
            request.setId(1L);
            request.setName("更新后的竞赛名称");

            BusinessException exception = assertThrows(BusinessException.class,
                    () -> competitionService.updateCompetition(request, httpRequest));
            assertEquals(ErrorCode.NO_AUTH.getCode(), exception.getCode());
        }

        @Test
        @DisplayName("更新不存在的竞赛失败")
        void testUpdateCompetition_NotFound() {
            when(httpRequest.getSession()).thenReturn(session);
            when(session.getAttribute(UserConstant.USER_LOGIN_STATE)).thenReturn(adminUser);
            doReturn(null).when(competitionService).getById(999L);

            CompetitionUpdateRequest request = new CompetitionUpdateRequest();
            request.setId(999L);
            request.setName("更新后的竞赛名称");

            BusinessException exception = assertThrows(BusinessException.class,
                    () -> competitionService.updateCompetition(request, httpRequest));
            assertEquals(ErrorCode.NULL_ERROR.getCode(), exception.getCode());
        }
    }

    @Nested
    @DisplayName("删除竞赛测试")
    class DeleteCompetitionTests {

        @Test
        @DisplayName("创建者成功删除竞赛")
        void testDeleteCompetition_Success() {
            when(httpRequest.getSession()).thenReturn(session);
            when(session.getAttribute(UserConstant.USER_LOGIN_STATE)).thenReturn(adminUser);
            doReturn(testCompetition).when(competitionService).getById(1L);
            doReturn(true).when(competitionService).removeById(1L);

            boolean result = competitionService.deleteCompetition(1L, httpRequest);

            assertTrue(result);
            verify(competitionService, times(1)).removeById(1L);
        }

        @Test
        @DisplayName("非创建者删除竞赛失败")
        void testDeleteCompetition_NoAuth() {
            when(httpRequest.getSession()).thenReturn(session);
            when(session.getAttribute(UserConstant.USER_LOGIN_STATE)).thenReturn(normalUser);
            doReturn(testCompetition).when(competitionService).getById(1L);

            BusinessException exception = assertThrows(BusinessException.class,
                    () -> competitionService.deleteCompetition(1L, httpRequest));
            assertEquals(ErrorCode.NO_AUTH.getCode(), exception.getCode());
        }
    }

    @Nested
    @DisplayName("查询竞赛测试")
    class ListCompetitionsTests {

        @Test
        @DisplayName("查询所有竞赛列表")
        void testListCompetitions_All() {
            List<Competition> competitions = Arrays.asList(testCompetition);
            doReturn(competitions).when(competitionService).list(any(QueryWrapper.class));

            List<Competition> result = competitionService.listCompetitions(null);

            assertNotNull(result);
            assertEquals(1, result.size());
        }

        @Test
        @DisplayName("根据名称查询竞赛")
        void testListCompetitions_ByName() {
            List<Competition> competitions = Arrays.asList(testCompetition);
            doReturn(competitions).when(competitionService).list(any(QueryWrapper.class));

            List<Competition> result = competitionService.listCompetitions("测试");

            assertNotNull(result);
            assertEquals(1, result.size());
        }

        @Test
        @DisplayName("根据ID查询竞赛")
        void testGetCompetitionById_Success() {
            when(competitionMapper.selectById(1L)).thenReturn(testCompetition);

            Competition result = competitionService.getCompetitionById(1L);

            assertNotNull(result);
            assertEquals("测试竞赛", result.getName());
        }

        @Test
        @DisplayName("查询不存在的竞赛抛出异常")
        void testGetCompetitionById_NotFound() {
            when(competitionMapper.selectById(999L)).thenReturn(null);

            BusinessException exception = assertThrows(BusinessException.class,
                    () -> competitionService.getCompetitionById(999L));
            assertEquals(ErrorCode.NULL_ERROR.getCode(), exception.getCode());
        }
    }

    @Nested
    @DisplayName("竞赛报名测试")
    class RegisterCompetitionTests {

        @Test
        @DisplayName("未登录用户报名失败")
        void testRegisterCompetition_NotLogin() {
            when(httpRequest.getSession()).thenReturn(session);
            when(session.getAttribute(UserConstant.USER_LOGIN_STATE)).thenReturn(null);

            CompetitionRegisterRequest request = new CompetitionRegisterRequest();
            request.setCompetitionId(1L);

            BusinessException exception = assertThrows(BusinessException.class,
                    () -> competitionService.registerCompetition(request, httpRequest));
            assertEquals(ErrorCode.NOT_LOGIN.getCode(), exception.getCode());
        }

        @Test
        @DisplayName("报名不存在的竞赛失败")
        void testRegisterCompetition_CompetitionNotFound() {
            when(httpRequest.getSession()).thenReturn(session);
            when(session.getAttribute(UserConstant.USER_LOGIN_STATE)).thenReturn(normalUser);
            doReturn(null).when(competitionService).getById(999L);

            CompetitionRegisterRequest request = new CompetitionRegisterRequest();
            request.setCompetitionId(999L);

            BusinessException exception = assertThrows(BusinessException.class,
                    () -> competitionService.registerCompetition(request, httpRequest));
            assertEquals(ErrorCode.PARAMS_ERROR.getCode(), exception.getCode());
        }

        @Test
        @DisplayName("报名已删除的竞赛失败")
        void testRegisterCompetition_CompetitionDeleted() {
            Competition deletedCompetition = new Competition();
            deletedCompetition.setId(1L);
            deletedCompetition.setIsDelete(1);

            when(httpRequest.getSession()).thenReturn(session);
            when(session.getAttribute(UserConstant.USER_LOGIN_STATE)).thenReturn(normalUser);
            doReturn(deletedCompetition).when(competitionService).getById(1L);

            CompetitionRegisterRequest request = new CompetitionRegisterRequest();
            request.setCompetitionId(1L);

            BusinessException exception = assertThrows(BusinessException.class,
                    () -> competitionService.registerCompetition(request, httpRequest));
            assertEquals(ErrorCode.PARAMS_ERROR.getCode(), exception.getCode());
        }

        @Test
        @DisplayName("团队赛队伍不存在报名失败")
        void testRegisterCompetition_TeamNotFound() {
            // 团队赛 maxMembers > 1
            Competition teamCompetition = new Competition();
            teamCompetition.setId(1L);
            teamCompetition.setMaxMembers(5);
            teamCompetition.setIsDelete(0);

            when(httpRequest.getSession()).thenReturn(session);
            when(session.getAttribute(UserConstant.USER_LOGIN_STATE)).thenReturn(normalUser);
            doReturn(teamCompetition).when(competitionService).getById(1L);
            when(teamMapper.selectById(999L)).thenReturn(null);

            CompetitionRegisterRequest request = new CompetitionRegisterRequest();
            request.setCompetitionId(1L);
            request.setTeamId(999L);

            BusinessException exception = assertThrows(BusinessException.class,
                    () -> competitionService.registerCompetition(request, httpRequest));
            assertEquals(ErrorCode.PARAMS_ERROR.getCode(), exception.getCode());
        }

        @Test
        @DisplayName("非队长报名失败")
        void testRegisterCompetition_NotCaptain() {
            Competition teamCompetition = new Competition();
            teamCompetition.setId(1L);
            teamCompetition.setMaxMembers(5);
            teamCompetition.setIsDelete(0);

            Team team = new Team();
            team.setId(10L);
            team.setUserId(999L);  // 队长是其他人
            team.setIsDelete(0);

            when(httpRequest.getSession()).thenReturn(session);
            when(session.getAttribute(UserConstant.USER_LOGIN_STATE)).thenReturn(normalUser);
            doReturn(teamCompetition).when(competitionService).getById(1L);
            when(teamMapper.selectById(10L)).thenReturn(team);

            CompetitionRegisterRequest request = new CompetitionRegisterRequest();
            request.setCompetitionId(1L);
            request.setTeamId(10L);

            BusinessException exception = assertThrows(BusinessException.class,
                    () -> competitionService.registerCompetition(request, httpRequest));
            assertEquals(ErrorCode.NO_AUTH.getCode(), exception.getCode());
        }

        @Test
        @DisplayName("重复报名失败")
        void testRegisterCompetition_AlreadyRegistered() {
            Competition teamCompetition = new Competition();
            teamCompetition.setId(1L);
            teamCompetition.setMaxMembers(5);
            teamCompetition.setIsDelete(0);

            Team team = new Team();
            team.setId(10L);
            team.setUserId(2L);  // normalUser 是队长
            team.setIsDelete(0);

            when(httpRequest.getSession()).thenReturn(session);
            when(session.getAttribute(UserConstant.USER_LOGIN_STATE)).thenReturn(normalUser);
            doReturn(teamCompetition).when(competitionService).getById(1L);
            when(teamMapper.selectById(10L)).thenReturn(team);
            when(competitionRegistrationMapper.selectCount(any(QueryWrapper.class))).thenReturn(1L);

            CompetitionRegisterRequest request = new CompetitionRegisterRequest();
            request.setCompetitionId(1L);
            request.setTeamId(10L);

            BusinessException exception = assertThrows(BusinessException.class,
                    () -> competitionService.registerCompetition(request, httpRequest));
            assertEquals(ErrorCode.PARAMS_ERROR.getCode(), exception.getCode());
        }

        @Test
        @DisplayName("团队赛成功报名")
        void testRegisterCompetition_TeamSuccess() {
            Competition teamCompetition = new Competition();
            teamCompetition.setId(1L);
            teamCompetition.setMaxMembers(5);
            teamCompetition.setIsDelete(0);

            Team team = new Team();
            team.setId(10L);
            team.setUserId(2L);  // normalUser 是队长
            team.setIsDelete(0);

            when(httpRequest.getSession()).thenReturn(session);
            when(session.getAttribute(UserConstant.USER_LOGIN_STATE)).thenReturn(normalUser);
            doReturn(teamCompetition).when(competitionService).getById(1L);
            when(teamMapper.selectById(10L)).thenReturn(team);
            when(competitionRegistrationMapper.selectCount(any(QueryWrapper.class))).thenReturn(0L);
            when(competitionRegistrationMapper.insert(any(CompetitionRegistration.class))).thenReturn(1);

            CompetitionRegisterRequest request = new CompetitionRegisterRequest();
            request.setCompetitionId(1L);
            request.setTeamId(10L);

            boolean result = competitionService.registerCompetition(request, httpRequest);

            assertTrue(result);
            verify(competitionRegistrationMapper, times(1)).insert(any(CompetitionRegistration.class));
        }
    }

    @Nested
    @DisplayName("报名审核测试")
    class ReviewRegistrationTests {

        @Test
        @DisplayName("竞赛创建者成功审核报名")
        void testReviewRegistration_Success() {
            CompetitionRegistration registration = new CompetitionRegistration();
            registration.setId(1L);
            registration.setCompetitionId(1L);
            registration.setStatus(0);
            registration.setIsDelete(0);

            when(httpRequest.getSession()).thenReturn(session);
            when(session.getAttribute(UserConstant.USER_LOGIN_STATE)).thenReturn(adminUser);
            when(competitionRegistrationMapper.selectById(1L)).thenReturn(registration);
            doReturn(testCompetition).when(competitionService).getById(1L);
            when(competitionRegistrationMapper.updateById(any(CompetitionRegistration.class))).thenReturn(1);

            CompetitionReviewRequest request = new CompetitionReviewRequest();
            request.setRegistrationId(1L);
            request.setStatus(1);

            boolean result = competitionService.reviewRegistration(request, httpRequest);

            assertTrue(result);
            verify(competitionRegistrationMapper, times(1)).updateById(any(CompetitionRegistration.class));
        }

        @Test
        @DisplayName("非创建者审核报名失败")
        void testReviewRegistration_NoAuth() {
            CompetitionRegistration registration = new CompetitionRegistration();
            registration.setId(1L);
            registration.setCompetitionId(1L);
            registration.setStatus(0);
            registration.setIsDelete(0);

            when(httpRequest.getSession()).thenReturn(session);
            when(session.getAttribute(UserConstant.USER_LOGIN_STATE)).thenReturn(normalUser);
            when(competitionRegistrationMapper.selectById(1L)).thenReturn(registration);
            doReturn(testCompetition).when(competitionService).getById(1L);

            CompetitionReviewRequest request = new CompetitionReviewRequest();
            request.setRegistrationId(1L);
            request.setStatus(1);

            BusinessException exception = assertThrows(BusinessException.class,
                    () -> competitionService.reviewRegistration(request, httpRequest));
            assertEquals(ErrorCode.NO_AUTH.getCode(), exception.getCode());
        }

        @Test
        @DisplayName("无效状态参数审核失败")
        void testReviewRegistration_InvalidStatus() {
            when(httpRequest.getSession()).thenReturn(session);
            when(session.getAttribute(UserConstant.USER_LOGIN_STATE)).thenReturn(adminUser);

            CompetitionReviewRequest request = new CompetitionReviewRequest();
            request.setRegistrationId(1L);
            request.setStatus(5);

            BusinessException exception = assertThrows(BusinessException.class,
                    () -> competitionService.reviewRegistration(request, httpRequest));
            assertEquals(ErrorCode.PARAMS_ERROR.getCode(), exception.getCode());
        }

        @Test
        @DisplayName("重复审核失败")
        void testReviewRegistration_AlreadyReviewed() {
            CompetitionRegistration registration = new CompetitionRegistration();
            registration.setId(1L);
            registration.setCompetitionId(1L);
            registration.setStatus(1);
            registration.setIsDelete(0);

            when(httpRequest.getSession()).thenReturn(session);
            when(session.getAttribute(UserConstant.USER_LOGIN_STATE)).thenReturn(adminUser);
            when(competitionRegistrationMapper.selectById(1L)).thenReturn(registration);
            doReturn(testCompetition).when(competitionService).getById(1L);

            CompetitionReviewRequest request = new CompetitionReviewRequest();
            request.setRegistrationId(1L);
            request.setStatus(1);

            BusinessException exception = assertThrows(BusinessException.class,
                    () -> competitionService.reviewRegistration(request, httpRequest));
            assertEquals(ErrorCode.PARAMS_ERROR.getCode(), exception.getCode());
        }
    }

    @Nested
    @DisplayName("查询报名列表测试")
    class ListCompetitionRegistrationsTests {

        @Test
        @DisplayName("成功查询竞赛报名列表")
        void testListCompetitionRegistrations_Success() {
            CompetitionRegistration reg1 = new CompetitionRegistration();
            reg1.setId(1L);
            reg1.setCompetitionId(1L);

            List<CompetitionRegistration> registrations = Arrays.asList(reg1);

            when(httpRequest.getSession()).thenReturn(session);
            when(session.getAttribute(UserConstant.USER_LOGIN_STATE)).thenReturn(adminUser);
            doReturn(testCompetition).when(competitionService).getById(1L);
            when(competitionRegistrationMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(registrations);

            List<CompetitionRegistration> result = competitionService.listCompetitionRegistrations(1L, httpRequest);

            assertNotNull(result);
            assertEquals(1, result.size());
        }

        @Test
        @DisplayName("未登录用户查询报名列表失败")
        void testListCompetitionRegistrations_NotLogin() {
            when(httpRequest.getSession()).thenReturn(session);
            when(session.getAttribute(UserConstant.USER_LOGIN_STATE)).thenReturn(null);

            BusinessException exception = assertThrows(BusinessException.class,
                    () -> competitionService.listCompetitionRegistrations(1L, httpRequest));
            assertEquals(ErrorCode.NOT_LOGIN.getCode(), exception.getCode());
        }
    }

    @Nested
    @DisplayName("查询我的竞赛测试")
    class ListMyCompetitionsTests {

        @Test
        @DisplayName("未登录用户查询我的竞赛失败")
        void testListMyCompetitions_NotLogin() {
            when(httpRequest.getSession()).thenReturn(session);
            when(session.getAttribute(UserConstant.USER_LOGIN_STATE)).thenReturn(null);

            BusinessException exception = assertThrows(BusinessException.class,
                    () -> competitionService.listMyCompetitions(httpRequest));
            assertEquals(ErrorCode.NOT_LOGIN.getCode(), exception.getCode());
        }

        @Test
        @DisplayName("用户没有参加任何竞赛返回空列表")
        void testListMyCompetitions_Empty() {
            when(httpRequest.getSession()).thenReturn(session);
            when(session.getAttribute(UserConstant.USER_LOGIN_STATE)).thenReturn(normalUser);
            when(competitionRegistrationMapper.selectList(any(LambdaQueryWrapper.class)))
                    .thenReturn(Collections.emptyList());
            when(teamMemberMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(Collections.emptyList());

            List<Competition> result = competitionService.listMyCompetitions(httpRequest);

            assertNotNull(result);
            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("用户有参加竞赛返回竞赛列表")
        void testListMyCompetitions_WithCompetitions() {
            CompetitionRegistration reg = new CompetitionRegistration();
            reg.setId(1L);
            reg.setCompetitionId(1L);
            reg.setUserId(2L);
            reg.setStatus(1);

            when(httpRequest.getSession()).thenReturn(session);
            when(session.getAttribute(UserConstant.USER_LOGIN_STATE)).thenReturn(normalUser);
            when(competitionRegistrationMapper.selectList(any(LambdaQueryWrapper.class)))
                    .thenReturn(Arrays.asList(reg));
            when(teamMemberMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(Collections.emptyList());
            doReturn(Arrays.asList(testCompetition)).when(competitionService).list(any(LambdaQueryWrapper.class));

            List<Competition> result = competitionService.listMyCompetitions(httpRequest);

            assertNotNull(result);
            assertEquals(1, result.size());
        }
    }

    @Nested
    @DisplayName("查询用户报名记录测试")
    class GetRegistrationByUserAndCompetitionTests {

        @Test
        @DisplayName("成功查询用户报名记录")
        void testGetRegistrationByUserAndCompetition_Success() {
            CompetitionRegistration reg = new CompetitionRegistration();
            reg.setId(1L);
            reg.setCompetitionId(1L);
            reg.setUserId(2L);

            when(httpRequest.getSession()).thenReturn(session);
            when(session.getAttribute(UserConstant.USER_LOGIN_STATE)).thenReturn(normalUser);
            doReturn(testCompetition).when(competitionService).getById(1L);
            when(competitionRegistrationMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(reg);

            CompetitionRegistration result = competitionService.getRegistrationByUserAndCompetition(1L, 2L, httpRequest);

            assertNotNull(result);
            assertEquals(1L, result.getCompetitionId());
        }

        @Test
        @DisplayName("未登录用户查询报名记录失败")
        void testGetRegistrationByUserAndCompetition_NotLogin() {
            when(httpRequest.getSession()).thenReturn(session);
            when(session.getAttribute(UserConstant.USER_LOGIN_STATE)).thenReturn(null);

            BusinessException exception = assertThrows(BusinessException.class,
                    () -> competitionService.getRegistrationByUserAndCompetition(1L, 2L, httpRequest));
            assertEquals(ErrorCode.NOT_LOGIN.getCode(), exception.getCode());
        }

        @Test
        @DisplayName("竞赛不存在查询报名记录失败")
        void testGetRegistrationByUserAndCompetition_CompetitionNotFound() {
            when(httpRequest.getSession()).thenReturn(session);
            when(session.getAttribute(UserConstant.USER_LOGIN_STATE)).thenReturn(normalUser);
            doReturn(null).when(competitionService).getById(999L);

            BusinessException exception = assertThrows(BusinessException.class,
                    () -> competitionService.getRegistrationByUserAndCompetition(999L, 2L, httpRequest));
            assertEquals(ErrorCode.NULL_ERROR.getCode(), exception.getCode());
        }

        @Test
        @DisplayName("竞赛已删除查询报名记录失败")
        void testGetRegistrationByUserAndCompetition_CompetitionDeleted() {
            Competition deletedCompetition = new Competition();
            deletedCompetition.setId(1L);
            deletedCompetition.setIsDelete(1);

            when(httpRequest.getSession()).thenReturn(session);
            when(session.getAttribute(UserConstant.USER_LOGIN_STATE)).thenReturn(normalUser);
            doReturn(deletedCompetition).when(competitionService).getById(1L);

            BusinessException exception = assertThrows(BusinessException.class,
                    () -> competitionService.getRegistrationByUserAndCompetition(1L, 2L, httpRequest));
            assertEquals(ErrorCode.NULL_ERROR.getCode(), exception.getCode());
        }

        @Test
        @DisplayName("用户未报名返回null")
        void testGetRegistrationByUserAndCompetition_NotRegistered() {
            when(httpRequest.getSession()).thenReturn(session);
            when(session.getAttribute(UserConstant.USER_LOGIN_STATE)).thenReturn(normalUser);
            doReturn(testCompetition).when(competitionService).getById(1L);
            when(competitionRegistrationMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);

            CompetitionRegistration result = competitionService.getRegistrationByUserAndCompetition(1L, 2L, httpRequest);

            assertNull(result);
        }
    }

    @Nested
    @DisplayName("个人赛报名测试")
    class IndividualCompetitionRegisterTests {

        @Test
        @DisplayName("个人赛成功报名-自动创建队伍")
        void testRegisterCompetition_IndividualSuccess() {
            Competition individualCompetition = new Competition();
            individualCompetition.setId(1L);
            individualCompetition.setMaxMembers(1);
            individualCompetition.setIsDelete(0);

            when(httpRequest.getSession()).thenReturn(session);
            when(session.getAttribute(UserConstant.USER_LOGIN_STATE)).thenReturn(normalUser);
            doReturn(individualCompetition).when(competitionService).getById(1L);
            when(teamMapper.selectOne(any(QueryWrapper.class))).thenReturn(null);
            when(teamMapper.insert(any(Team.class))).thenAnswer(invocation -> {
                Team t = invocation.getArgument(0);
                t.setId(100L);
                return 1;
            });
            when(teamMemberMapper.insert(any(TeamMember.class))).thenReturn(1);
            when(teamMapper.selectById(100L)).thenReturn(createTeam(100L, 2L));
            when(competitionRegistrationMapper.selectCount(any(QueryWrapper.class))).thenReturn(0L);
            when(competitionRegistrationMapper.insert(any(CompetitionRegistration.class))).thenReturn(1);

            CompetitionRegisterRequest request = new CompetitionRegisterRequest();
            request.setCompetitionId(1L);

            boolean result = competitionService.registerCompetition(request, httpRequest);

            assertTrue(result);
        }

        @Test
        @DisplayName("个人赛已有队伍直接报名")
        void testRegisterCompetition_IndividualExistingTeam() {
            Competition individualCompetition = new Competition();
            individualCompetition.setId(1L);
            individualCompetition.setMaxMembers(1);
            individualCompetition.setIsDelete(0);

            Team existingTeam = createTeam(50L, 2L);

            when(httpRequest.getSession()).thenReturn(session);
            when(session.getAttribute(UserConstant.USER_LOGIN_STATE)).thenReturn(normalUser);
            doReturn(individualCompetition).when(competitionService).getById(1L);
            when(teamMapper.selectOne(any(QueryWrapper.class))).thenReturn(existingTeam);
            when(teamMapper.selectById(50L)).thenReturn(existingTeam);
            when(competitionRegistrationMapper.selectCount(any(QueryWrapper.class))).thenReturn(0L);
            when(competitionRegistrationMapper.insert(any(CompetitionRegistration.class))).thenReturn(1);

            CompetitionRegisterRequest request = new CompetitionRegisterRequest();
            request.setCompetitionId(1L);

            boolean result = competitionService.registerCompetition(request, httpRequest);

            assertTrue(result);
        }
    }

    @Nested
    @DisplayName("更新竞赛边界测试")
    class UpdateCompetitionEdgeCaseTests {

        @Test
        @DisplayName("更新已删除的竞赛失败")
        void testUpdateCompetition_DeletedCompetition() {
            Competition deletedCompetition = new Competition();
            deletedCompetition.setId(1L);
            deletedCompetition.setIsDelete(1);

            when(httpRequest.getSession()).thenReturn(session);
            when(session.getAttribute(UserConstant.USER_LOGIN_STATE)).thenReturn(adminUser);
            doReturn(deletedCompetition).when(competitionService).getById(1L);

            CompetitionUpdateRequest request = new CompetitionUpdateRequest();
            request.setId(1L);
            request.setName("更新后的名称");

            BusinessException exception = assertThrows(BusinessException.class,
                    () -> competitionService.updateCompetition(request, httpRequest));
            assertEquals(ErrorCode.NULL_ERROR.getCode(), exception.getCode());
        }

        @Test
        @DisplayName("未登录用户更新竞赛失败")
        void testUpdateCompetition_NotLogin() {
            when(httpRequest.getSession()).thenReturn(session);
            when(session.getAttribute(UserConstant.USER_LOGIN_STATE)).thenReturn(null);

            CompetitionUpdateRequest request = new CompetitionUpdateRequest();
            request.setId(1L);

            BusinessException exception = assertThrows(BusinessException.class,
                    () -> competitionService.updateCompetition(request, httpRequest));
            assertEquals(ErrorCode.NOT_LOGIN.getCode(), exception.getCode());
        }

        @Test
        @DisplayName("更新竞赛时间字段成功")
        void testUpdateCompetition_UpdateTimeFields() {
            when(httpRequest.getSession()).thenReturn(session);
            when(session.getAttribute(UserConstant.USER_LOGIN_STATE)).thenReturn(adminUser);
            doReturn(testCompetition).when(competitionService).getById(1L);
            doReturn(true).when(competitionService).updateById(any(Competition.class));

            CompetitionUpdateRequest request = new CompetitionUpdateRequest();
            request.setId(1L);
            request.setStartTime(new Date());
            request.setEndTime(new Date());

            boolean result = competitionService.updateCompetition(request, httpRequest);

            assertTrue(result);
        }
    }

    @Nested
    @DisplayName("删除竞赛边界测试")
    class DeleteCompetitionEdgeCaseTests {

        @Test
        @DisplayName("删除已删除的竞赛失败")
        void testDeleteCompetition_AlreadyDeleted() {
            Competition deletedCompetition = new Competition();
            deletedCompetition.setId(1L);
            deletedCompetition.setIsDelete(1);

            when(httpRequest.getSession()).thenReturn(session);
            when(session.getAttribute(UserConstant.USER_LOGIN_STATE)).thenReturn(adminUser);
            doReturn(deletedCompetition).when(competitionService).getById(1L);

            BusinessException exception = assertThrows(BusinessException.class,
                    () -> competitionService.deleteCompetition(1L, httpRequest));
            assertEquals(ErrorCode.PARAMS_ERROR.getCode(), exception.getCode());
        }

        @Test
        @DisplayName("未登录用户删除竞赛失败")
        void testDeleteCompetition_NotLogin() {
            when(httpRequest.getSession()).thenReturn(session);
            when(session.getAttribute(UserConstant.USER_LOGIN_STATE)).thenReturn(null);

            BusinessException exception = assertThrows(BusinessException.class,
                    () -> competitionService.deleteCompetition(1L, httpRequest));
            assertEquals(ErrorCode.NOT_LOGIN.getCode(), exception.getCode());
        }

        @Test
        @DisplayName("删除不存在的竞赛失败")
        void testDeleteCompetition_NotFound() {
            when(httpRequest.getSession()).thenReturn(session);
            when(session.getAttribute(UserConstant.USER_LOGIN_STATE)).thenReturn(adminUser);
            doReturn(null).when(competitionService).getById(999L);

            BusinessException exception = assertThrows(BusinessException.class,
                    () -> competitionService.deleteCompetition(999L, httpRequest));
            assertEquals(ErrorCode.PARAMS_ERROR.getCode(), exception.getCode());
        }
    }

    @Nested
    @DisplayName("审核报名边界测试")
    class ReviewRegistrationEdgeCaseTests {

        @Test
        @DisplayName("未登录用户审核报名失败")
        void testReviewRegistration_NotLogin() {
            when(httpRequest.getSession()).thenReturn(session);
            when(session.getAttribute(UserConstant.USER_LOGIN_STATE)).thenReturn(null);

            CompetitionReviewRequest request = new CompetitionReviewRequest();
            request.setRegistrationId(1L);
            request.setStatus(1);

            BusinessException exception = assertThrows(BusinessException.class,
                    () -> competitionService.reviewRegistration(request, httpRequest));
            assertEquals(ErrorCode.NOT_LOGIN.getCode(), exception.getCode());
        }

        @Test
        @DisplayName("报名记录不存在审核失败")
        void testReviewRegistration_RegistrationNotFound() {
            when(httpRequest.getSession()).thenReturn(session);
            when(session.getAttribute(UserConstant.USER_LOGIN_STATE)).thenReturn(adminUser);
            when(competitionRegistrationMapper.selectById(999L)).thenReturn(null);

            CompetitionReviewRequest request = new CompetitionReviewRequest();
            request.setRegistrationId(999L);
            request.setStatus(1);

            BusinessException exception = assertThrows(BusinessException.class,
                    () -> competitionService.reviewRegistration(request, httpRequest));
            assertEquals(ErrorCode.NULL_ERROR.getCode(), exception.getCode());
        }

        @Test
        @DisplayName("报名记录已删除审核失败")
        void testReviewRegistration_RegistrationDeleted() {
            CompetitionRegistration deletedReg = new CompetitionRegistration();
            deletedReg.setId(1L);
            deletedReg.setIsDelete(1);

            when(httpRequest.getSession()).thenReturn(session);
            when(session.getAttribute(UserConstant.USER_LOGIN_STATE)).thenReturn(adminUser);
            when(competitionRegistrationMapper.selectById(1L)).thenReturn(deletedReg);

            CompetitionReviewRequest request = new CompetitionReviewRequest();
            request.setRegistrationId(1L);
            request.setStatus(1);

            BusinessException exception = assertThrows(BusinessException.class,
                    () -> competitionService.reviewRegistration(request, httpRequest));
            assertEquals(ErrorCode.NULL_ERROR.getCode(), exception.getCode());
        }

        @Test
        @DisplayName("竞赛不存在审核失败")
        void testReviewRegistration_CompetitionNotFound() {
            CompetitionRegistration registration = new CompetitionRegistration();
            registration.setId(1L);
            registration.setCompetitionId(999L);
            registration.setStatus(0);
            registration.setIsDelete(0);

            when(httpRequest.getSession()).thenReturn(session);
            when(session.getAttribute(UserConstant.USER_LOGIN_STATE)).thenReturn(adminUser);
            when(competitionRegistrationMapper.selectById(1L)).thenReturn(registration);
            doReturn(null).when(competitionService).getById(999L);

            CompetitionReviewRequest request = new CompetitionReviewRequest();
            request.setRegistrationId(1L);
            request.setStatus(1);

            BusinessException exception = assertThrows(BusinessException.class,
                    () -> competitionService.reviewRegistration(request, httpRequest));
            assertEquals(ErrorCode.NULL_ERROR.getCode(), exception.getCode());
        }

        @Test
        @DisplayName("拒绝报名成功")
        void testReviewRegistration_Reject() {
            CompetitionRegistration registration = new CompetitionRegistration();
            registration.setId(1L);
            registration.setCompetitionId(1L);
            registration.setStatus(0);
            registration.setIsDelete(0);

            when(httpRequest.getSession()).thenReturn(session);
            when(session.getAttribute(UserConstant.USER_LOGIN_STATE)).thenReturn(adminUser);
            when(competitionRegistrationMapper.selectById(1L)).thenReturn(registration);
            doReturn(testCompetition).when(competitionService).getById(1L);
            when(competitionRegistrationMapper.updateById(any(CompetitionRegistration.class))).thenReturn(1);

            CompetitionReviewRequest request = new CompetitionReviewRequest();
            request.setRegistrationId(1L);
            request.setStatus(2);

            boolean result = competitionService.reviewRegistration(request, httpRequest);

            assertTrue(result);
        }
    }

    @Nested
    @DisplayName("查询报名列表边界测试")
    class ListRegistrationsEdgeCaseTests {

        @Test
        @DisplayName("竞赛不存在查询报名列表失败")
        void testListCompetitionRegistrations_CompetitionNotFound() {
            when(httpRequest.getSession()).thenReturn(session);
            when(session.getAttribute(UserConstant.USER_LOGIN_STATE)).thenReturn(adminUser);
            doReturn(null).when(competitionService).getById(999L);

            BusinessException exception = assertThrows(BusinessException.class,
                    () -> competitionService.listCompetitionRegistrations(999L, httpRequest));
            assertEquals(ErrorCode.NULL_ERROR.getCode(), exception.getCode());
        }

        @Test
        @DisplayName("竞赛已删除查询报名列表失败")
        void testListCompetitionRegistrations_CompetitionDeleted() {
            Competition deletedCompetition = new Competition();
            deletedCompetition.setId(1L);
            deletedCompetition.setIsDelete(1);

            when(httpRequest.getSession()).thenReturn(session);
            when(session.getAttribute(UserConstant.USER_LOGIN_STATE)).thenReturn(adminUser);
            doReturn(deletedCompetition).when(competitionService).getById(1L);

            BusinessException exception = assertThrows(BusinessException.class,
                    () -> competitionService.listCompetitionRegistrations(1L, httpRequest));
            assertEquals(ErrorCode.NULL_ERROR.getCode(), exception.getCode());
        }
    }

    private Team createTeam(Long id, Long userId) {
        Team team = new Team();
        team.setId(id);
        team.setUserId(userId);
        team.setIsDelete(0);
        return team;
    }
}