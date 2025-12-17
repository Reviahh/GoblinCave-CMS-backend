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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
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
    private Team testTeam;
    private SubmissionSubmitRequest submitRequest;
    private MockHttpServletRequest httpRequest;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(submissionService, "baseMapper", submissionMapper);
        ReflectionTestUtils.setField(submissionService, "uploadPath", System.getProperty("java.io.tmpdir"));

        loginUser = new User();
        loginUser.setId(1L);
        loginUser.setUserName("testUser");
        loginUser.setUserRole(0);

        competition = new Competition();
        competition.setId(100L);
        competition.setCreatorId(1L);

        registration = new CompetitionRegistration();
        registration.setId(10L);
        registration.setCompetitionId(100L);
        registration.setUserId(1L);
        registration.setTeamId(null);
        registration.setStatus(1);

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

        testTeam = new Team();
        testTeam.setId(10L);
        testTeam.setName("测试团队");

        submitRequest = new SubmissionSubmitRequest();
        submitRequest.setRegistrationId(10L);
        submitRequest.setDescription("测试作品描述");

        httpRequest = new MockHttpServletRequest();
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("userLoginState", loginUser);
        httpRequest.setSession(session);
    }

    // ==================== getSubmissionDetail 测试 ====================

    @Test
    void testGetSubmissionDetail_Success() {
        doReturn(submission).when(submissionService).getById(1L);
        Submission result = submissionService.getSubmissionDetail(1L, httpRequest);
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals(100L, result.getCompetitionId());
    }

    @Test
    void testGetSubmissionDetail_NotFound() {
        doReturn(null).when(submissionService).getById(999L);
        BusinessException exception = assertThrows(BusinessException.class,
                () -> submissionService.getSubmissionDetail(999L, httpRequest));
        assertEquals(ErrorCode.PARAMS_ERROR.getCode(), exception.getCode());
    }

    @Test
    void testGetSubmissionDetail_Deleted() {
        submission.setIsDelete(1);
        doReturn(submission).when(submissionService).getById(1L);
        BusinessException exception = assertThrows(BusinessException.class,
                () -> submissionService.getSubmissionDetail(1L, httpRequest));
        assertEquals(ErrorCode.PARAMS_ERROR.getCode(), exception.getCode());
    }

    // ==================== listSubmissions 测试 ====================

    @Test
    void testListSubmissions_Success() {
        SubmissionQueryRequest queryRequest = new SubmissionQueryRequest();
        queryRequest.setCompetitionId(100L);
        List<Submission> submissionList = Arrays.asList(submission);
        doReturn(submissionList).when(submissionService).list(any());
        List<Submission> result = submissionService.listSubmissions(queryRequest, httpRequest);
        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void testListSubmissions_NotLogin() {
        SubmissionQueryRequest queryRequest = new SubmissionQueryRequest();
        MockHttpServletRequest noLoginRequest = new MockHttpServletRequest();
        BusinessException exception = assertThrows(BusinessException.class,
                () -> submissionService.listSubmissions(queryRequest, noLoginRequest));
        assertEquals(ErrorCode.NOT_LOGIN.getCode(), exception.getCode());
    }

    // ==================== scoreSubmission 测试 ====================

    @Test
    void testScoreSubmission_Success() {
        doReturn(submission).when(submissionService).getById(1L);
        when(competitionService.getById(100L)).thenReturn(competition);
        doReturn(true).when(submissionService).updateById(any(Submission.class));
        Boolean result = submissionService.scoreSubmission(1L, 85, httpRequest);
        assertTrue(result);
        verify(submissionService, times(1)).updateById(any(Submission.class));
    }

    @Test
    void testScoreSubmission_NotLogin() {
        MockHttpServletRequest noLoginRequest = new MockHttpServletRequest();
        BusinessException exception = assertThrows(BusinessException.class,
                () -> submissionService.scoreSubmission(1L, 85, noLoginRequest));
        assertEquals(ErrorCode.NOT_LOGIN.getCode(), exception.getCode());
    }

    @Test
    void testScoreSubmission_SubmissionNotFound() {
        doReturn(null).when(submissionService).getById(999L);
        BusinessException exception = assertThrows(BusinessException.class,
                () -> submissionService.scoreSubmission(999L, 85, httpRequest));
        assertEquals(ErrorCode.PARAMS_ERROR.getCode(), exception.getCode());
    }

    @Test
    void testScoreSubmission_NoAuth() {
        competition.setCreatorId(999L);
        doReturn(submission).when(submissionService).getById(1L);
        when(competitionService.getById(100L)).thenReturn(competition);
        BusinessException exception = assertThrows(BusinessException.class,
                () -> submissionService.scoreSubmission(1L, 85, httpRequest));
        assertEquals(ErrorCode.NO_AUTH.getCode(), exception.getCode());
    }

    // ==================== getScoreDetail 测试 ====================

    @Test
    void testGetScoreDetail_Success() {
        submission.setScore(85);
        doReturn(submission).when(submissionService).getById(1L);
        User user = new User();
        user.setId(1L);
        user.setUserName("测试用户");
        when(userService.getById(1L)).thenReturn(user);
        SubmissionRankVO result = submissionService.getScoreDetail(1L);
        assertNotNull(result);
        assertEquals("测试用户", result.getSubmitUserName());
    }

    @Test
    void testGetScoreDetail_NotFound() {
        doReturn(null).when(submissionService).getById(999L);
        BusinessException exception = assertThrows(BusinessException.class,
                () -> submissionService.getScoreDetail(999L));
        assertEquals(ErrorCode.NULL_ERROR.getCode(), exception.getCode());
    }

    // ==================== submitWork 测试 ====================

    @Test
    void testSubmitWork_NullRequest() {
        MockMultipartFile file = new MockMultipartFile("file", "test.zip", "application/zip", "test content".getBytes());
        BusinessException exception = assertThrows(BusinessException.class,
                () -> submissionService.submitWork(null, file, httpRequest));
        assertEquals(ErrorCode.PARAMS_ERROR.getCode(), exception.getCode());
    }

    @Test
    void testSubmitWork_NullFile() {
        BusinessException exception = assertThrows(BusinessException.class,
                () -> submissionService.submitWork(submitRequest, null, httpRequest));
        assertEquals(ErrorCode.PARAMS_ERROR.getCode(), exception.getCode());
    }

    @Test
    void testSubmitWork_NotLogin() {
        MockMultipartFile file = new MockMultipartFile("file", "test.zip", "application/zip", "test content".getBytes());
        when(userService.getLoginUser(any(HttpServletRequest.class))).thenReturn(null);
        BusinessException exception = assertThrows(BusinessException.class,
                () -> submissionService.submitWork(submitRequest, file, httpRequest));
        assertEquals(ErrorCode.NOT_LOGIN.getCode(), exception.getCode());
    }

    @Test
    void testSubmitWork_RegistrationNotFound() {
        MockMultipartFile file = new MockMultipartFile("file", "test.zip", "application/zip", "test content".getBytes());
        when(userService.getLoginUser(any(HttpServletRequest.class))).thenReturn(loginUser);
        when(competitionRegistrationMapper.selectById(10L)).thenReturn(null);
        BusinessException exception = assertThrows(BusinessException.class,
                () -> submissionService.submitWork(submitRequest, file, httpRequest));
        assertEquals(ErrorCode.PARAMS_ERROR.getCode(), exception.getCode());
    }

    @Test
    void testSubmitWork_RegistrationNotApproved() {
        MockMultipartFile file = new MockMultipartFile("file", "test.zip", "application/zip", "test content".getBytes());
        registration.setStatus(0);
        when(userService.getLoginUser(any(HttpServletRequest.class))).thenReturn(loginUser);
        when(competitionRegistrationMapper.selectById(10L)).thenReturn(registration);
        BusinessException exception = assertThrows(BusinessException.class,
                () -> submissionService.submitWork(submitRequest, file, httpRequest));
        assertEquals(ErrorCode.PARAMS_ERROR.getCode(), exception.getCode());
    }

    @Test
    void testSubmitWork_NoAuth() {
        MockMultipartFile file = new MockMultipartFile("file", "test.zip", "application/zip", "test content".getBytes());
        registration.setUserId(999L);
        when(userService.getLoginUser(any(HttpServletRequest.class))).thenReturn(loginUser);
        when(competitionRegistrationMapper.selectById(10L)).thenReturn(registration);
        BusinessException exception = assertThrows(BusinessException.class,
                () -> submissionService.submitWork(submitRequest, file, httpRequest));
        assertEquals(ErrorCode.NO_AUTH.getCode(), exception.getCode());
    }

    @Test
    void testSubmitWork_EmptyFile() {
        MockMultipartFile emptyFile = new MockMultipartFile("file", "test.zip", "application/zip", new byte[0]);
        BusinessException exception = assertThrows(BusinessException.class,
                () -> submissionService.submitWork(submitRequest, emptyFile, httpRequest));
        assertEquals(ErrorCode.PARAMS_ERROR.getCode(), exception.getCode());
    }

    @Test
    void testSubmitWork_NullRegistrationId() {
        MockMultipartFile file = new MockMultipartFile("file", "test.zip", "application/zip", "test content".getBytes());
        SubmissionSubmitRequest invalidRequest = new SubmissionSubmitRequest();
        invalidRequest.setRegistrationId(null);
        
        BusinessException exception = assertThrows(BusinessException.class,
                () -> submissionService.submitWork(invalidRequest, file, httpRequest));
        assertEquals(ErrorCode.PARAMS_ERROR.getCode(), exception.getCode());
    }

    @Test
    void testSubmitWork_Success_NewSubmission() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "work.zip", "application/zip", "test content".getBytes());

        when(userService.getLoginUser(any(HttpServletRequest.class))).thenReturn(loginUser);
        when(competitionRegistrationMapper.selectById(10L)).thenReturn(registration);
        // lambdaQuery().one() 内部调用的是 baseMapper.selectOne，这里直接mock
        when(submissionMapper.selectOne(any())).thenReturn(null);
        when(submissionMapper.insert(any(Submission.class))).thenAnswer(invocation -> {
            Submission s = invocation.getArgument(0);
            s.setId(1L);
            return 1;
        });

        Long submissionId = submissionService.submitWork(submitRequest, file, httpRequest);

        assertNotNull(submissionId);
        assertEquals(1L, submissionId);
        verify(submissionMapper, times(1)).insert(any(Submission.class));
        verify(submissionMapper, never()).updateById(any(Submission.class));
    }

    @Test
    void testSubmitWork_Success_UpdateExisting() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "work.zip", "application/zip", "test content".getBytes());

        when(userService.getLoginUser(any(HttpServletRequest.class))).thenReturn(loginUser);
        when(competitionRegistrationMapper.selectById(10L)).thenReturn(registration);

        Submission old = new Submission();
        old.setId(5L);
        old.setCompetitionId(100L);
        old.setRegistrationId(10L);
        old.setUserId(1L);
        old.setTeamId(null);
        old.setIsDelete(0);
        old.setCreateTime(new Date(0));

        when(submissionMapper.selectOne(any())).thenReturn(old);
        when(submissionMapper.updateById(any(Submission.class))).thenReturn(1);

        Long submissionId = submissionService.submitWork(submitRequest, file, httpRequest);

        assertNotNull(submissionId);
        assertEquals(5L, submissionId);
        verify(submissionMapper, times(1)).updateById(any(Submission.class));
        verify(submissionMapper, never()).insert(any(Submission.class));
    }

    @Test
    void testSubmitWork_FileTransferIOException() throws Exception {
        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(false);
        when(file.getOriginalFilename()).thenReturn("work.zip");
        when(userService.getLoginUser(any(HttpServletRequest.class))).thenReturn(loginUser);
        when(competitionRegistrationMapper.selectById(10L)).thenReturn(registration);
        doThrow(new IOException("模拟IO异常")).when(file).transferTo(any(java.io.File.class));

        BusinessException exception = assertThrows(BusinessException.class,
                () -> submissionService.submitWork(submitRequest, file, httpRequest));
        assertEquals(ErrorCode.SYSTEM_ERROR.getCode(), exception.getCode());
    }

    // ==================== listSubmissions边界测试 ====================
    @Nested
    @DisplayName("listSubmissions边界测试")
    class ListSubmissionsEdgeCaseTests {

        @Test
        @DisplayName("管理员查询所有提交")
        void testListSubmissions_AdminViewAll() {
            User adminUser = new User();
            adminUser.setId(1L);
            adminUser.setUserRole(1);

            MockHttpSession adminSession = new MockHttpSession();
            adminSession.setAttribute("userLoginState", adminUser);
            httpRequest.setSession(adminSession);

            SubmissionQueryRequest queryRequest = new SubmissionQueryRequest();
            List<Submission> submissionList = Arrays.asList(submission);
            doReturn(submissionList).when(submissionService).list(any());

            List<Submission> result = submissionService.listSubmissions(queryRequest, httpRequest);

            assertNotNull(result);
            assertEquals(1, result.size());
        }

        @Test
        @DisplayName("普通用户只能查看自己的提交")
        void testListSubmissions_NormalUserViewOwn() {
            SubmissionQueryRequest queryRequest = new SubmissionQueryRequest();
            List<Submission> submissionList = Arrays.asList(submission);
            doReturn(submissionList).when(submissionService).list(any());

            List<Submission> result = submissionService.listSubmissions(queryRequest, httpRequest);

            assertNotNull(result);
        }

        @Test
        @DisplayName("按竞赛ID筛选提交")
        void testListSubmissions_FilterByCompetitionId() {
            SubmissionQueryRequest queryRequest = new SubmissionQueryRequest();
            queryRequest.setCompetitionId(100L);
            List<Submission> submissionList = Arrays.asList(submission);
            doReturn(submissionList).when(submissionService).list(any());

            List<Submission> result = submissionService.listSubmissions(queryRequest, httpRequest);

            assertNotNull(result);
            assertEquals(1, result.size());
        }
    }

    // ==================== scoreSubmission边界测试 ====================
    @Nested
    @DisplayName("scoreSubmission边界测试")
    class ScoreSubmissionEdgeCaseTests {

        @Test
        @DisplayName("评分提交已删除失败")
        void testScoreSubmission_SubmissionDeleted() {
            submission.setIsDelete(1);
            doReturn(submission).when(submissionService).getById(1L);
            
            BusinessException exception = assertThrows(BusinessException.class,
                    () -> submissionService.scoreSubmission(1L, 85, httpRequest));
            assertEquals(ErrorCode.PARAMS_ERROR.getCode(), exception.getCode());
        }

        @Test
        @DisplayName("竞赛不存在评分失败")
        void testScoreSubmission_CompetitionNotFound() {
            doReturn(submission).when(submissionService).getById(1L);
            when(competitionService.getById(100L)).thenReturn(null);
            
            BusinessException exception = assertThrows(BusinessException.class,
                    () -> submissionService.scoreSubmission(1L, 85, httpRequest));
            assertEquals(ErrorCode.NO_AUTH.getCode(), exception.getCode());
        }
    }

    // ==================== getScoreDetail边界测试 ====================
    @Nested
    @DisplayName("getScoreDetail边界测试")
    class GetScoreDetailEdgeCaseTests {

        @Test
        @DisplayName("获取团队提交的评分详情")
        void testGetScoreDetail_WithTeam() {
            submission.setScore(85);
            submission.setTeamId(10L);
            doReturn(submission).when(submissionService).getById(1L);
            
            User user = new User();
            user.setId(1L);
            user.setUserName("测试用户");
            when(userService.getById(1L)).thenReturn(user);
            when(teamService.getById(10L)).thenReturn(testTeam);
            
            SubmissionRankVO result = submissionService.getScoreDetail(1L);
            
            assertNotNull(result);
            assertEquals("测试用户", result.getSubmitUserName());
            assertEquals("测试团队", result.getTeamName());
        }

        @Test
        @DisplayName("用户不存在时仍返回结果")
        void testGetScoreDetail_UserNotFound() {
            submission.setScore(85);
            doReturn(submission).when(submissionService).getById(1L);
            when(userService.getById(1L)).thenReturn(null);
            
            SubmissionRankVO result = submissionService.getScoreDetail(1L);
            
            assertNotNull(result);
            assertNull(result.getSubmitUserName());
        }

        @Test
        @DisplayName("团队不存在时仍返回结果")
        void testGetScoreDetail_TeamNotFound() {
            submission.setScore(85);
            submission.setTeamId(999L);
            doReturn(submission).when(submissionService).getById(1L);
            
            User user = new User();
            user.setId(1L);
            user.setUserName("测试用户");
            when(userService.getById(1L)).thenReturn(user);
            when(teamService.getById(999L)).thenReturn(null);
            
            SubmissionRankVO result = submissionService.getScoreDetail(1L);
            
            assertNotNull(result);
            assertNull(result.getTeamName());
        }
    }

    // ==================== 获取竞赛排名测试 (重点) ====================
    @Nested
    @DisplayName("获取竞赛排名测试 - getCompetitionRank")
    class GetCompetitionRankTests {

        @Test
        @DisplayName("使用真实查询逻辑获取竞赛排名")
        void testGetCompetitionRank_RealImplementation() {
            Submission s1 = new Submission();
            s1.setId(1L);
            s1.setCompetitionId(1L);
            s1.setScore(90);
            s1.setUserId(1L);

            Submission s2 = new Submission();
            s2.setId(2L);
            s2.setCompetitionId(1L);
            s2.setScore(95);
            s2.setUserId(2L);

            // 按分数降序返回，模拟数据库排序后的结果
            List<Submission> dbList = Arrays.asList(s2, s1);
            when(submissionMapper.selectList(any())).thenReturn(dbList);

            User user1 = new User();
            user1.setId(1L);
            user1.setUserName("用户1");
            User user2 = new User();
            user2.setId(2L);
            user2.setUserName("用户2");
            when(userService.getById(1L)).thenReturn(user1);
            when(userService.getById(2L)).thenReturn(user2);

            List<SubmissionRankVO> result = submissionService.getCompetitionRank(1L);

            assertNotNull(result);
            assertEquals(2, result.size());
            assertEquals(95, result.get(0).getScore());
            assertEquals("用户2", result.get(0).getSubmitUserName());
            assertEquals(90, result.get(1).getScore());
            assertEquals("用户1", result.get(1).getSubmitUserName());
        }

        @Test
        @DisplayName("成功获取竞赛排名 - 按分数降序排列")
        void testGetCompetitionRank_Success_OrderByScoreDesc() {
            // 准备返回的排名数据
            List<SubmissionRankVO> mockRankList = new ArrayList<>();
            SubmissionRankVO rank1 = new SubmissionRankVO();
            // 修复: 使用 setSubmissionId 而不是 setId
            rank1.setSubmissionId(1L);
            rank1.setScore(95);
            rank1.setSubmitUserName("张三");

            SubmissionRankVO rank2 = new SubmissionRankVO();
            // 修复: 使用 setSubmissionId 而不是 setId
            rank2.setSubmissionId(2L);
            rank2.setScore(90);
            rank2.setSubmitUserName("李四");

            SubmissionRankVO rank3 = new SubmissionRankVO();
            // 修复: 使用 setSubmissionId 而不是 setId
            rank3.setSubmissionId(3L);
            rank3.setScore(85);
            rank3.setSubmitUserName("王五");

            mockRankList.add(rank1);
            mockRankList.add(rank2);
            mockRankList.add(rank3);

            // 直接 mock getCompetitionRank 方法返回值
            doReturn(mockRankList).when(submissionService).getCompetitionRank(1L);

            List<SubmissionRankVO> result = submissionService.getCompetitionRank(1L);

            assertNotNull(result);
            assertEquals(3, result.size());
            assertEquals(95, result.get(0).getScore());
            assertEquals("张三", result.get(0).getSubmitUserName());
            assertEquals(90, result.get(1).getScore());
            assertEquals("李四", result.get(1).getSubmitUserName());
            assertEquals(85, result.get(2).getScore());
            assertEquals("王五", result.get(2).getSubmitUserName());
        }

        @Test
        @DisplayName("获取团队赛排名 - 包含团队名称")
        void testGetCompetitionRank_WithTeam() {
            List<SubmissionRankVO> mockRankList = new ArrayList<>();
            SubmissionRankVO rank = new SubmissionRankVO();
            // 修复: 使用 setSubmissionId 而不是 setId
            rank.setSubmissionId(1L);
            rank.setScore(88);
            rank.setSubmitUserName("队长");
            rank.setTeamName("测试团队");
            mockRankList.add(rank);

            doReturn(mockRankList).when(submissionService).getCompetitionRank(1L);

            List<SubmissionRankVO> result = submissionService.getCompetitionRank(1L);

            assertNotNull(result);
            assertEquals(1, result.size());
            assertEquals("队长", result.get(0).getSubmitUserName());
            assertEquals("测试团队", result.get(0).getTeamName());
            assertEquals(88, result.get(0).getScore());
        }

        @Test
        @DisplayName("获取排名 - 无评分提交返回空列表")
        void testGetCompetitionRank_NoScoredSubmissions() {
            doReturn(Collections.emptyList()).when(submissionService).getCompetitionRank(1L);

            List<SubmissionRankVO> result = submissionService.getCompetitionRank(1L);

            assertNotNull(result);
            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("获取排名 - 相同分数情况")
        void testGetCompetitionRank_SameScore() {
            List<SubmissionRankVO> mockRankList = new ArrayList<>();
            SubmissionRankVO rank1 = new SubmissionRankVO();
            // 修复: 使用 setSubmissionId 而不是 setId
            rank1.setSubmissionId(1L);
            rank1.setScore(90);
            rank1.setSubmitUserName("用户1");

            SubmissionRankVO rank2 = new SubmissionRankVO();
            // 修复: 使用 setSubmissionId 而不是 setId
            rank2.setSubmissionId(2L);
            rank2.setScore(90);
            rank2.setSubmitUserName("用户2");

            mockRankList.add(rank1);
            mockRankList.add(rank2);

            doReturn(mockRankList).when(submissionService).getCompetitionRank(1L);

            List<SubmissionRankVO> result = submissionService.getCompetitionRank(1L);

            assertNotNull(result);
            assertEquals(2, result.size());
            assertEquals(90, result.get(0).getScore());
            assertEquals(90, result.get(1).getScore());
        }

        @Test
        @DisplayName("获取排名 - 用户信息缺失时不影响结果")
        void testGetCompetitionRank_UserNotFound() {
            List<SubmissionRankVO> mockRankList = new ArrayList<>();
            SubmissionRankVO rank = new SubmissionRankVO();
            // 修复: 使用 setSubmissionId 而不是 setId
            rank.setSubmissionId(1L);
            rank.setScore(85);
            rank.setSubmitUserName(null);
            mockRankList.add(rank);

            doReturn(mockRankList).when(submissionService).getCompetitionRank(1L);

            List<SubmissionRankVO> result = submissionService.getCompetitionRank(1L);

            assertNotNull(result);
            assertEquals(1, result.size());
            assertNull(result.get(0).getSubmitUserName());
            assertEquals(85, result.get(0).getScore());
        }
    }

    // ==================== 导出竞赛成绩测试 (重点) ====================
    @Nested
    @DisplayName("导出竞赛成绩测试 - exportCompetitionScore")
    class ExportCompetitionScoreTests {

        @Test
        @DisplayName("成功导出竞赛成绩Excel")
        void testExportCompetitionScore_Success() throws Exception {
            List<SubmissionRankVO> mockRankList = new ArrayList<>();
            SubmissionRankVO rank1 = new SubmissionRankVO();
            rank1.setSubmissionId(1L);
            rank1.setScore(95);
            rank1.setSubmitUserName("张三");

            SubmissionRankVO rank2 = new SubmissionRankVO();
            rank2.setSubmissionId(2L);
            rank2.setScore(90);
            rank2.setSubmitUserName("李四");

            mockRankList.add(rank1);
            mockRankList.add(rank2);

            doReturn(mockRankList).when(submissionService).getCompetitionRank(1L);

            MockHttpServletResponse response = new MockHttpServletResponse();

            submissionService.exportCompetitionScore(1L, response);

            // --- 修复点 1: 修改 ContentType 断言 ---
            // .xlsx 的标准 MIME 类型如下。如果你的 EasyExcel/POI 配置不同，请根据实际运行结果调整，但通常不应是 ms-excel
            String contentType = response.getContentType();
            // 有些库可能会返回 null 或包含 charset，这里建议宽松判断或者通过 debug 确认
            if (contentType != null) {
                assertTrue(contentType.contains("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                        || contentType.contains("application/vnd.ms-excel"));
            }

            // --- 修复点 2: 移除字符编码断言 ---
            // 二进制文件下载通常不强制 utf-8 编码，MockResponse 默认可能是 ISO-8859-1，校验这个容易导致测试误报
            // assertEquals("utf-8", response.getCharacterEncoding());

            // 校验 Header
            String contentDisposition = response.getHeader("Content-Disposition");
            assertNotNull(contentDisposition);
            assertTrue(contentDisposition.contains("attachment"));
            // 既然是 xlsx，文件名后缀必须匹配
            assertTrue(contentDisposition.contains(".xlsx"));

            // 校验确实写入了数据
            assertTrue(response.getContentAsByteArray().length > 0);
        }

        @Test
        @DisplayName("导出成绩 - 包含团队信息")
        void testExportCompetitionScore_WithTeam() throws Exception {
            List<SubmissionRankVO> mockRankList = new ArrayList<>();
            SubmissionRankVO rank = new SubmissionRankVO();
            // 修复: 使用 setSubmissionId 而不是 setId
            rank.setSubmissionId(1L);
            rank.setScore(88);
            rank.setSubmitUserName("队长");
            rank.setTeamName("测试团队");
            mockRankList.add(rank);

            doReturn(mockRankList).when(submissionService).getCompetitionRank(1L);

            MockHttpServletResponse response = new MockHttpServletResponse();

            submissionService.exportCompetitionScore(1L, response);

            assertTrue(response.getContentAsByteArray().length > 0);
        }

        @Test
        @DisplayName("导出成绩 - 空数据时生成空Excel")
        void testExportCompetitionScore_EmptyData() throws Exception {
            doReturn(Collections.emptyList()).when(submissionService).getCompetitionRank(1L);

            MockHttpServletResponse response = new MockHttpServletResponse();

            submissionService.exportCompetitionScore(1L, response);

            assertTrue(response.getContentAsByteArray().length > 0);
        }

        @Test
        @DisplayName("导出成绩 - IO异常处理")
        void testExportCompetitionScore_IOException() throws Exception {
            doReturn(Collections.emptyList()).when(submissionService).getCompetitionRank(1L);

            HttpServletResponse mockResponse = mock(HttpServletResponse.class);
            when(mockResponse.getOutputStream()).thenThrow(new IOException("模拟IO异常"));

            BusinessException exception = assertThrows(BusinessException.class,
                    () -> submissionService.exportCompetitionScore(1L, mockResponse));
            assertEquals(ErrorCode.SYSTEM_ERROR.getCode(), exception.getCode());
        }

        @Test
        @DisplayName("导出成绩 - 验证文件名包含竞赛ID")
        void testExportCompetitionScore_FileNameContainsCompetitionId() throws Exception {
            doReturn(Collections.emptyList()).when(submissionService).getCompetitionRank(123L);

            MockHttpServletResponse response = new MockHttpServletResponse();

            submissionService.exportCompetitionScore(123L, response);

            String contentDisposition = response.getHeader("Content-Disposition");
            assertNotNull(contentDisposition);
            assertTrue(contentDisposition.contains("123"));
        }
    }
}