package com.miji.cms.service;

import com.miji.cms.model.domain.TeamRecruitment;
import com.miji.cms.model.request.RecruitmentQueryRequest;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * TeamRecruitmentService 集成测试
 * 使用真实数据库进行测试，测试完成后自动回滚
 */
@SpringBootTest
@Transactional // 测试完成后自动回滚，不污染数据库
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class TeamRecruitmentServiceIntegrationTest {

    @Resource
    private TeamRecruitmentService teamRecruitmentService;

    private TeamRecruitment personalRecruitment;
    private TeamRecruitment teamRecruitment;

    @BeforeEach
    void setUp() {
        // 初始化个人招募令
        personalRecruitment = new TeamRecruitment();
        personalRecruitment.setUserId(1L);
        personalRecruitment.setCompetitionId(100L);
        personalRecruitment.setTeamId(null);
        personalRecruitment.setIsTeam(0); // 个人发布
        personalRecruitment.setTitle("寻找队友一起参赛");
        personalRecruitment.setDescription("本人擅长后端开发，寻找前端队友");
        personalRecruitment.setContact("QQ:123456789");
        personalRecruitment.setMaxMembers(2);
        personalRecruitment.setStatus(0);
        personalRecruitment.setCreateTime(new Date());
        personalRecruitment.setUpdateTime(new Date());
        personalRecruitment.setIsDelete(0);

        // 初始化队伍招募令
        teamRecruitment = new TeamRecruitment();
        teamRecruitment.setUserId(2L);
        teamRecruitment.setCompetitionId(100L);
        teamRecruitment.setTeamId(10L);
        teamRecruitment.setIsTeam(1); // 队伍发布
        teamRecruitment.setTitle("XX队招募成员");
        teamRecruitment.setDescription("我们队伍目前缺少一名UI设计师");
        teamRecruitment.setContact("微信:team_leader");
        teamRecruitment.setMaxMembers(1);
        teamRecruitment.setStatus(0);
        teamRecruitment.setCreateTime(new Date());
        teamRecruitment.setUpdateTime(new Date());
        teamRecruitment.setIsDelete(0);
    }

    // ==================== 基本 CRUD 测试 ====================

    @Test
    @Order(1)
    @DisplayName("保存个人招募令")
    void testSavePersonalRecruitment() {
        // When
        boolean result = teamRecruitmentService.save(personalRecruitment);

        // Then
        assertTrue(result);
        assertNotNull(personalRecruitment.getId());
        System.out.println("保存成功，ID: " + personalRecruitment.getId());
    }

    @Test
    @Order(2)
    @DisplayName("保存队伍招募令")
    void testSaveTeamRecruitment() {
        // When
        boolean result = teamRecruitmentService.save(teamRecruitment);

        // Then
        assertTrue(result);
        assertNotNull(teamRecruitment.getId());
        assertEquals(1, teamRecruitment.getIsTeam());
        assertEquals(10L, teamRecruitment.getTeamId());
    }

    @Test
    @Order(3)
    @DisplayName("根据ID查询招募令")
    void testGetById() {
        // Given
        teamRecruitmentService.save(personalRecruitment);
        Long id = personalRecruitment.getId();

        // When
        TeamRecruitment found = teamRecruitmentService.getById(id);

        // Then
        assertNotNull(found);
        assertEquals(id, found.getId());
        assertEquals("寻找队友一起参赛", found.getTitle());
        assertEquals("本人擅长后端开发，寻找前端队友", found.getDescription());
    }

    @Test
    @Order(4)
    @DisplayName("更新招募令")
    void testUpdate() {
        // Given
        teamRecruitmentService.save(personalRecruitment);
        personalRecruitment.setTitle("更新后的标题");
        personalRecruitment.setDescription("更新后的描述");
        personalRecruitment.setMaxMembers(3);

        // When
        boolean result = teamRecruitmentService.updateById(personalRecruitment);

        // Then
        assertTrue(result);
        TeamRecruitment updated = teamRecruitmentService.getById(personalRecruitment.getId());
        assertEquals("更新后的标题", updated.getTitle());
        assertEquals("更新后的描述", updated.getDescription());
        assertEquals(3, updated.getMaxMembers());
    }

    @Test
    @Order(5)
    @DisplayName("删除招募令")
    void testRemove() {
        // Given
        teamRecruitmentService.save(personalRecruitment);
        Long id = personalRecruitment.getId();

        // When
        boolean result = teamRecruitmentService.removeById(id);

        // Then
        assertTrue(result);
    }

    // ==================== 查询测试 ====================

    @Test
    @Order(6)
    @DisplayName("根据竞赛ID查询招募令列表")
    void testListByCompetitionId() {
        // Given
        personalRecruitment.setCompetitionId(999L);
        teamRecruitment.setCompetitionId(999L);
        teamRecruitmentService.save(personalRecruitment);
        teamRecruitmentService.save(teamRecruitment);

        // When
        List<TeamRecruitment> list = teamRecruitmentService.lambdaQuery()
                .eq(TeamRecruitment::getCompetitionId, 999L)
                .eq(TeamRecruitment::getIsDelete, 0)
                .orderByDesc(TeamRecruitment::getCreateTime)
                .list();

        // Then
        assertNotNull(list);
        assertEquals(2, list.size());
    }

    @Test
    @Order(7)
    @DisplayName("根据类型筛选 - 仅个人招募")
    void testListByIsTeam_Personal() {
        // Given
        personalRecruitment.setCompetitionId(888L);
        teamRecruitment.setCompetitionId(888L);
        teamRecruitmentService.save(personalRecruitment);
        teamRecruitmentService.save(teamRecruitment);

        // When
        List<TeamRecruitment> personalList = teamRecruitmentService.lambdaQuery()
                .eq(TeamRecruitment::getCompetitionId, 888L)
                .eq(TeamRecruitment::getIsTeam, 0)
                .eq(TeamRecruitment::getIsDelete, 0)
                .list();

        // Then
        assertNotNull(personalList);
        assertEquals(1, personalList.size());
        assertEquals(0, personalList.get(0).getIsTeam());
    }

    @Test
    @Order(8)
    @DisplayName("根据类型筛选 - 仅队伍招募")
    void testListByIsTeam_Team() {
        // Given
        personalRecruitment.setCompetitionId(777L);
        teamRecruitment.setCompetitionId(777L);
        teamRecruitmentService.save(personalRecruitment);
        teamRecruitmentService.save(teamRecruitment);

        // When
        List<TeamRecruitment> teamList = teamRecruitmentService.lambdaQuery()
                .eq(TeamRecruitment::getCompetitionId, 777L)
                .eq(TeamRecruitment::getIsTeam, 1)
                .eq(TeamRecruitment::getIsDelete, 0)
                .list();

        // Then
        assertNotNull(teamList);
        assertEquals(1, teamList.size());
        assertEquals(1, teamList.get(0).getIsTeam());
    }

    @Test
    @Order(9)
    @DisplayName("根据用户ID查询发布的招募令")
    void testListByUserId() {
        // Given
        personalRecruitment.setUserId(666L);
        teamRecruitmentService.save(personalRecruitment);

        TeamRecruitment another = new TeamRecruitment();
        another.setUserId(666L);
        another.setCompetitionId(200L);
        another.setIsTeam(0);
        another.setTitle("另一个招募");
        another.setDescription("描述");
        another.setContact("联系方式");
        another.setCreateTime(new Date());
        another.setUpdateTime(new Date());
        another.setIsDelete(0);
        teamRecruitmentService.save(another);

        // When
        List<TeamRecruitment> list = teamRecruitmentService.lambdaQuery()
                .eq(TeamRecruitment::getUserId, 666L)
                .eq(TeamRecruitment::getIsDelete, 0)
                .list();

        // Then
        assertNotNull(list);
        assertEquals(2, list.size());
    }

    @Test
    @Order(10)
    @DisplayName("根据队伍ID查询招募令")
    void testListByTeamId() {
        // Given
        teamRecruitment.setTeamId(555L);
        teamRecruitmentService.save(teamRecruitment);

        // When
        List<TeamRecruitment> list = teamRecruitmentService.lambdaQuery()
                .eq(TeamRecruitment::getTeamId, 555L)
                .eq(TeamRecruitment::getIsDelete, 0)
                .list();

        // Then
        assertNotNull(list);
        assertEquals(1, list.size());
        assertEquals(555L, list.get(0).getTeamId());
    }

    // ==================== 状态测试 ====================

    @Test
    @Order(11)
    @DisplayName("更新招募状态 - 已满员")
    void testUpdateStatus_Full() {
        // Given
        teamRecruitmentService.save(personalRecruitment);
        personalRecruitment.setStatus(1); // 已满员

        // When
        boolean result = teamRecruitmentService.updateById(personalRecruitment);

        // Then
        assertTrue(result);
        TeamRecruitment updated = teamRecruitmentService.getById(personalRecruitment.getId());
        assertEquals(1, updated.getStatus());
    }

    @Test
    @Order(12)
    @DisplayName("更新招募状态 - 已关闭")
    void testUpdateStatus_Closed() {
        // Given
        teamRecruitmentService.save(personalRecruitment);
        personalRecruitment.setStatus(2); // 已关闭

        // When
        boolean result = teamRecruitmentService.updateById(personalRecruitment);

        // Then
        assertTrue(result);
        TeamRecruitment updated = teamRecruitmentService.getById(personalRecruitment.getId());
        assertEquals(2, updated.getStatus());
    }

    // ==================== content/description 映射测试 ====================

    @Test
    @Order(13)
    @DisplayName("测试 content 和 description 映射")
    void testContentDescriptionMapping() {
        // Given
        TeamRecruitment r = new TeamRecruitment();
        r.setContent("通过 content 设置的内容");

        // Then
        assertEquals("通过 content 设置的内容", r.getDescription());
        assertEquals("通过 content 设置的内容", r.getContent());

        // When
        r.setDescription("通过 description 设置的内容");

        // Then
        assertEquals("通过 description 设置的内容", r.getContent());
    }

    @Test
    @Order(14)
    @DisplayName("统计竞赛的招募令数量")
    void testCountByCompetitionId() {
        // Given
        personalRecruitment.setCompetitionId(444L);
        teamRecruitment.setCompetitionId(444L);
        teamRecruitmentService.save(personalRecruitment);
        teamRecruitmentService.save(teamRecruitment);

        // When
        long count = teamRecruitmentService.lambdaQuery()
                .eq(TeamRecruitment::getCompetitionId, 444L)
                .eq(TeamRecruitment::getIsDelete, 0)
                .count();

        // Then
        assertEquals(2, count);
    }
}
