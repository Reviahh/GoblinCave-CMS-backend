package com.miji.cms.service;

import com.miji.cms.model.domain.Submission;
import com.miji.cms.model.request.SubmissionRankVO;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * SubmissionService 集成测试
 * 使用真实数据库进行测试，测试完成后自动回滚
 */
@SpringBootTest
@Transactional // 测试完成后自动回滚，不污染数据库
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class SubmissionServiceIntegrationTest {

    @Resource
    private SubmissionService submissionService;

    private Submission testSubmission;

    @BeforeEach
    void setUp() {
        // 初始化测试数据
        testSubmission = new Submission();
        testSubmission.setCompetitionId(1L);
        testSubmission.setRegistrationId(1L);
        testSubmission.setUserId(1L);
        testSubmission.setTeamId(null);
        testSubmission.setFileUrl("/uploads/test/test.zip");
        testSubmission.setDescription("测试作品描述");
        testSubmission.setStatus(0);
        testSubmission.setScore(null);
        testSubmission.setCreateTime(new Date());
        testSubmission.setUpdateTime(new Date());
        testSubmission.setIsDelete(0);
    }

    // ==================== 基本 CRUD 测试 ====================

    @Test
    @Order(1)
    @DisplayName("保存提交记录")
    void testSave() {
        // When
        boolean result = submissionService.save(testSubmission);

        // Then
        assertTrue(result);
        assertNotNull(testSubmission.getId());
        System.out.println("保存成功，ID: " + testSubmission.getId());
    }

    @Test
    @Order(2)
    @DisplayName("根据ID查询提交记录")
    void testGetById() {
        // Given
        submissionService.save(testSubmission);
        Long id = testSubmission.getId();

        // When
        Submission found = submissionService.getById(id);

        // Then
        assertNotNull(found);
        assertEquals(id, found.getId());
        assertEquals("测试作品描述", found.getDescription());
    }

    @Test
    @Order(3)
    @DisplayName("更新提交记录")
    void testUpdate() {
        // Given
        submissionService.save(testSubmission);
        testSubmission.setDescription("更新后的描述");
        testSubmission.setScore(85);
        testSubmission.setStatus(1);

        // When
        boolean result = submissionService.updateById(testSubmission);

        // Then
        assertTrue(result);
        Submission updated = submissionService.getById(testSubmission.getId());
        assertEquals("更新后的描述", updated.getDescription());
        assertEquals(85, updated.getScore());
        assertEquals(1, updated.getStatus());
    }

    @Test
    @Order(4)
    @DisplayName("删除提交记录")
    void testRemove() {
        // Given
        submissionService.save(testSubmission);
        Long id = testSubmission.getId();

        // When
        boolean result = submissionService.removeById(id);

        // Then
        assertTrue(result);
        // 由于使用了逻辑删除，getById 可能仍返回记录
    }

    // ==================== 业务方法测试 ====================

    @Test
    @Order(5)
    @DisplayName("查询竞赛排名 - lambdaQuery 链式调用")
    void testGetCompetitionRank() {
        // Given - 插入测试数据
        Submission sub1 = new Submission();
        sub1.setCompetitionId(999L);
        sub1.setRegistrationId(1L);
        sub1.setUserId(1L);
        sub1.setFileUrl("/test1.zip");
        sub1.setScore(90);
        sub1.setStatus(1);
        sub1.setCreateTime(new Date());
        sub1.setUpdateTime(new Date());
        sub1.setIsDelete(0);
        submissionService.save(sub1);

        Submission sub2 = new Submission();
        sub2.setCompetitionId(999L);
        sub2.setRegistrationId(2L);
        sub2.setUserId(2L);
        sub2.setFileUrl("/test2.zip");
        sub2.setScore(85);
        sub2.setStatus(1);
        sub2.setCreateTime(new Date());
        sub2.setUpdateTime(new Date());
        sub2.setIsDelete(0);
        submissionService.save(sub2);

        // When
        List<SubmissionRankVO> rankList = submissionService.getCompetitionRank(999L);

        // Then
        assertNotNull(rankList);
        assertEquals(2, rankList.size());
        // 验证排序：分数高的在前
        assertTrue(rankList.get(0).getScore() >= rankList.get(1).getScore());
    }

    @Test
    @Order(6)
    @DisplayName("获取分数详情")
    void testGetScoreDetail() {
        // Given
        testSubmission.setScore(88);
        testSubmission.setStatus(1);
        submissionService.save(testSubmission);

        // When
        SubmissionRankVO detail = submissionService.getScoreDetail(testSubmission.getId());

        // Then
        assertNotNull(detail);
        assertEquals(88, detail.getScore());
    }

    // ==================== 查询测试 ====================

    @Test
    @Order(7)
    @DisplayName("根据竞赛ID查询提交列表")
    void testListByCompetitionId() {
        // Given
        testSubmission.setCompetitionId(888L);
        submissionService.save(testSubmission);

        Submission sub2 = new Submission();
        sub2.setCompetitionId(888L);
        sub2.setRegistrationId(2L);
        sub2.setUserId(2L);
        sub2.setFileUrl("/test2.zip");
        sub2.setStatus(0);
        sub2.setCreateTime(new Date());
        sub2.setUpdateTime(new Date());
        sub2.setIsDelete(0);
        submissionService.save(sub2);

        // When
        List<Submission> list = submissionService.lambdaQuery()
                .eq(Submission::getCompetitionId, 888L)
                .eq(Submission::getIsDelete, 0)
                .list();

        // Then
        assertNotNull(list);
        assertEquals(2, list.size());
    }

    @Test
    @Order(8)
    @DisplayName("根据用户ID查询提交列表")
    void testListByUserId() {
        // Given
        testSubmission.setUserId(777L);
        submissionService.save(testSubmission);

        // When
        List<Submission> list = submissionService.lambdaQuery()
                .eq(Submission::getUserId, 777L)
                .eq(Submission::getIsDelete, 0)
                .list();

        // Then
        assertNotNull(list);
        assertFalse(list.isEmpty());
        assertEquals(777L, list.get(0).getUserId());
    }
}
