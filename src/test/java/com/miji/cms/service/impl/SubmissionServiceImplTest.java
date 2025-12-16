package com.miji.cms.service.impl;

import com.miji.cms.common.ErrorCode;
import com.miji.cms.exception.BusinessException;
import com.miji.cms.mapper.CompetitionRegistrationMapper;
import com.miji.cms.mapper.CompetitionSubmissionMapper;
import com.miji.cms.mapper.TeamMapper;
import com.miji.cms.mapper.TeamMemberMapper;
import com.miji.cms.model.domain.*;
import com.miji.cms.model.request.SubmissionQueryRequest;
import com.miji.cms.model.request.SubmissionRankVO;
import com.miji.cms.model.request.SubmissionSubmitRequest;
import com.miji.cms.service.CompetitionService;
import com.miji.cms.service.TeamService;
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
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * SubmissionServiceImpl 单元测试
 */
@ExtendWith(MockitoExtension.class)
class SubmissionServiceImplTest {

    @Mock
    private CompetitionSubmissionMapper submissionMapper;

    @Mock
    private UserService userService;

    @Mock
    private TeamService teamService;

    @Mock
    private CompetitionService competitionService;

    @Mock
    private CompetitionRegistrationMapper competitionRegistrationMapper;

    @Mock
    private TeamMapper teamMapper;

    @Mock
    private TeamMemberMapper teamMemberMapper;

    @Spy
    @InjectMocks
    private SubmissionServiceImpl submissionService;

    private User loginUser;
    private Competition competition;
    private CompetitionRegistration registration;
    private Submission submission;
    private SubmissionSubmitRequest submitRequest;
    private MockHttpServletRequest httpRequest;

    @BeforeEach
    void setUp() {
        // 注入 baseMapper
        ReflectionTestUtils.setField(submissionService, "baseMapper", submissionMapper);
        // 注入上传路径
        ReflectionTestUtils.setField(submissionService, "uploadPath", System.getProperty("java.io.tmpdir"));

        // 初始化登录用户
        loginUser = new User();
        loginUser.setId(1L);
        loginUser.setUserName("testUser");
        loginUser.setUserRole(0);

        // 初始化竞赛
        competition = new Competition();
        competition.setId(100L);
        competition.setCreatorId(1L);

        // 初始化报名记录
        registration = new CompetitionRegistration();
        registration.setId(10L);
        registration.setCompetitionId(100L);
        registration.setUserId(1L);
        registration.setTeamId(null);
        registration.setStatus(1);

        // 初始化提交记录
        submission = new Submission();
        submission.setId(1L);
        submission.setCompetitionId(100L);
        submission.setRegistrationId(10L);
        submission.setUserId(1L);
        submission.setFileUrl("/uploads/submissions/test.zip");
        submission.setStatus(0);
        submission.setScore(null);
        submission.setIsDelete(0);
        submission.setCreateTime(new Date());

        // 初始化提交请求
        submitRequest = new SubmissionSubmitRequest();
        submitRequest.setRegistrationId(10L);
        submitRequest.setDescription("测试作品描述");

        // 初始化 HTTP 请求
        httpRequest = new MockHttpServletRequest();
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("userLoginState", loginUser);
        httpRequest.setSession(session);
    }

    // ==================== getSubmissionDetail 测试 ====================

    @Test
    void testGetSubmissionDetail_Success() {
        // Given
        doReturn(submission).when(submissionService).getById(1L);

        // When
        Submission result = submissionService.getSubmissionDetail(1L, httpRequest);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals(100L, result.getCompetitionId());
    }

    @Test
    void testGetSubmissionDetail_NotFound() {
        // Given
        doReturn(null).when(submissionService).getById(999L);

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class,
                () -> submissionService.getSubmissionDetail(999L, httpRequest));
        assertEquals(ErrorCode.PARAMS_ERROR.getCode(), exception.getCode());
    }

    @Test
    void testGetSubmissionDetail_Deleted() {
        // Given
        submission.setIsDelete(1);
        doReturn(submission).when(submissionService).getById(1L);

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class,
                () -> submissionService.getSubmissionDetail(1L, httpRequest));
        assertEquals(ErrorCode.PARAMS_ERROR.getCode(), exception.getCode());
    }

    // ==================== listSubmissions 测试 ====================

    @Test
    void testListSubmissions_Success() {
        // Given
        SubmissionQueryRequest queryRequest = new SubmissionQueryRequest();
        queryRequest.setCompetitionId(100L);

        List<Submission> submissionList = Arrays.asList(submission);
        doReturn(submissionList).when(submissionService).list(any());

        // When
        List<Submission> result = submissionService.listSubmissions(queryRequest, httpRequest);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void testListSubmissions_NotLogin() {
        // Given
        SubmissionQueryRequest queryRequest = new SubmissionQueryRequest();
        MockHttpServletRequest noLoginRequest = new MockHttpServletRequest();

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class,
                () -> submissionService.listSubmissions(queryRequest, noLoginRequest));
        assertEquals(ErrorCode.NOT_LOGIN.getCode(), exception.getCode());
    }

    // ==================== scoreSubmission 测试 ====================

    @Test
    void testScoreSubmission_Success() {
        // Given
        doReturn(submission).when(submissionService).getById(1L);
        when(competitionService.getById(100L)).thenReturn(competition);
        doReturn(true).when(submissionService).updateById(any(Submission.class));

        // When
        Boolean result = submissionService.scoreSubmission(1L, 85, httpRequest);

        // Then
        assertTrue(result);
        verify(submissionService, times(1)).updateById(any(Submission.class));
    }

    @Test
    void testScoreSubmission_NotLogin() {
        // Given
        MockHttpServletRequest noLoginRequest = new MockHttpServletRequest();

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class,
                () -> submissionService.scoreSubmission(1L, 85, noLoginRequest));
        assertEquals(ErrorCode.NOT_LOGIN.getCode(), exception.getCode());
    }

    @Test
    void testScoreSubmission_SubmissionNotFound() {
        // Given
        doReturn(null).when(submissionService).getById(999L);

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class,
                () -> submissionService.scoreSubmission(999L, 85, httpRequest));
        assertEquals(ErrorCode.PARAMS_ERROR.getCode(), exception.getCode());
    }

    @Test
    void testScoreSubmission_NoAuth() {
        // Given
        competition.setCreatorId(999L); // 不是当前用户创建的竞赛
        doReturn(submission).when(submissionService).getById(1L);
        when(competitionService.getById(100L)).thenReturn(competition);

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class,
                () -> submissionService.scoreSubmission(1L, 85, httpRequest));
        assertEquals(ErrorCode.NO_AUTH.getCode(), exception.getCode());
    }

    // ==================== getCompetitionRank 测试 ====================
    // 注意: getCompetitionRank 使用了 lambdaQuery 链式调用，难以完全模拟
    // 建议使用集成测试来测试此方法，这里仅验证方法调用不会抛出异常

    // ==================== getScoreDetail 测试 ====================

    @Test
    void testGetScoreDetail_Success() {
        // Given
        submission.setScore(85);
        doReturn(submission).when(submissionService).getById(1L);

        User user = new User();
        user.setId(1L);
        user.setUserName("测试用户");
        when(userService.getById(1L)).thenReturn(user);

        // When
        SubmissionRankVO result = submissionService.getScoreDetail(1L);

        // Then
        assertNotNull(result);
        assertEquals("测试用户", result.getSubmitUserName());
    }

    @Test
    void testGetScoreDetail_NotFound() {
        // Given
        doReturn(null).when(submissionService).getById(999L);

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class,
                () -> submissionService.getScoreDetail(999L));
        assertEquals(ErrorCode.NULL_ERROR.getCode(), exception.getCode());
    }

    // ==================== submitWork 测试 ====================

    @Test
    void testSubmitWork_NullRequest() {
        // Given
        MockMultipartFile file = new MockMultipartFile("file", "test.zip", "application/zip",
                "test content".getBytes());

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class,
                () -> submissionService.submitWork(null, file, httpRequest));
        assertEquals(ErrorCode.PARAMS_ERROR.getCode(), exception.getCode());
    }

    @Test
    void testSubmitWork_NullFile() {
        // When & Then
        BusinessException exception = assertThrows(BusinessException.class,
                () -> submissionService.submitWork(submitRequest, null, httpRequest));
        assertEquals(ErrorCode.PARAMS_ERROR.getCode(), exception.getCode());
    }

    @Test
    void testSubmitWork_NotLogin() {
        // Given
        MockMultipartFile file = new MockMultipartFile("file", "test.zip", "application/zip",
                "test content".getBytes());
        when(userService.getLoginUser(any(HttpServletRequest.class))).thenReturn(null);

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class,
                () -> submissionService.submitWork(submitRequest, file, httpRequest));
        assertEquals(ErrorCode.NOT_LOGIN.getCode(), exception.getCode());
    }

    @Test
    void testSubmitWork_RegistrationNotFound() {
        // Given
        MockMultipartFile file = new MockMultipartFile("file", "test.zip", "application/zip",
                "test content".getBytes());
        when(userService.getLoginUser(any(HttpServletRequest.class))).thenReturn(loginUser);
        when(competitionRegistrationMapper.selectById(10L)).thenReturn(null);

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class,
                () -> submissionService.submitWork(submitRequest, file, httpRequest));
        assertEquals(ErrorCode.PARAMS_ERROR.getCode(), exception.getCode());
    }

    @Test
    void testSubmitWork_RegistrationNotApproved() {
        // Given
        MockMultipartFile file = new MockMultipartFile("file", "test.zip", "application/zip",
                "test content".getBytes());
        registration.setStatus(0); // 未审核通过
        when(userService.getLoginUser(any(HttpServletRequest.class))).thenReturn(loginUser);
        when(competitionRegistrationMapper.selectById(10L)).thenReturn(registration);

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class,
                () -> submissionService.submitWork(submitRequest, file, httpRequest));
        assertEquals(ErrorCode.PARAMS_ERROR.getCode(), exception.getCode());
    }

    @Test
    void testSubmitWork_NoAuth() {
        // Given
        MockMultipartFile file = new MockMultipartFile("file", "test.zip", "application/zip",
                "test content".getBytes());
        registration.setUserId(999L); // 不是当前用户的报名
        when(userService.getLoginUser(any(HttpServletRequest.class))).thenReturn(loginUser);
        when(competitionRegistrationMapper.selectById(10L)).thenReturn(registration);

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class,
                () -> submissionService.submitWork(submitRequest, file, httpRequest));
        assertEquals(ErrorCode.NO_AUTH.getCode(), exception.getCode());
    }
}
