package com.miji.cms.service;

import com.miji.cms.model.domain.TeamMember;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * TeamMemberService 集成测试
 * 使用真实数据库进行测试，测试完成后自动回滚
 */
@SpringBootTest
@Transactional // 测试完成后自动回滚，不污染数据库
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class TeamMemberServiceIntegrationTest {

    @Resource
    private TeamMemberService teamMemberService;

    private TeamMember captainMember;
    private TeamMember normalMember;

    @BeforeEach
    void setUp() {
        // 初始化队长成员
        captainMember = new TeamMember();
        captainMember.setUserId(1L);
        captainMember.setTeamId(100L);
        captainMember.setRole(1); // 队长
        captainMember.setJoinTime(new Date());
        captainMember.setCreateTime(new Date());
        captainMember.setUpdateTime(new Date());
        captainMember.setIsDelete(0);

        // 初始化普通成员
        normalMember = new TeamMember();
        normalMember.setUserId(2L);
        normalMember.setTeamId(100L);
        normalMember.setRole(0); // 普通成员
        normalMember.setJoinTime(new Date());
        normalMember.setCreateTime(new Date());
        normalMember.setUpdateTime(new Date());
        normalMember.setIsDelete(0);
    }

    // ==================== 基本 CRUD 测试 ====================

    @Test
    @Order(1)
    @DisplayName("保存队伍成员")
    void testSave() {
        // When
        boolean result = teamMemberService.save(captainMember);

        // Then
        assertTrue(result);
        assertNotNull(captainMember.getId());
        System.out.println("保存成功，ID: " + captainMember.getId());
    }

    @Test
    @Order(2)
    @DisplayName("根据ID查询成员")
    void testGetById() {
        // Given
        teamMemberService.save(captainMember);
        Long id = captainMember.getId();

        // When
        TeamMember found = teamMemberService.getById(id);

        // Then
        assertNotNull(found);
        assertEquals(id, found.getId());
        assertEquals(1L, found.getUserId());
        assertEquals(100L, found.getTeamId());
        assertEquals(1, found.getRole());
    }

    @Test
    @Order(3)
    @DisplayName("更新成员角色")
    void testUpdate() {
        // Given
        teamMemberService.save(normalMember);
        normalMember.setRole(1); // 升级为队长

        // When
        boolean result = teamMemberService.updateById(normalMember);

        // Then
        assertTrue(result);
        TeamMember updated = teamMemberService.getById(normalMember.getId());
        assertEquals(1, updated.getRole());
    }

    @Test
    @Order(4)
    @DisplayName("删除成员")
    void testRemove() {
        // Given
        teamMemberService.save(normalMember);
        Long id = normalMember.getId();

        // When
        boolean result = teamMemberService.removeById(id);

        // Then
        assertTrue(result);
    }

    // ==================== 批量操作测试 ====================

    @Test
    @Order(5)
    @DisplayName("批量保存成员")
    void testSaveBatch() {
        // Given
        TeamMember member1 = new TeamMember();
        member1.setUserId(10L);
        member1.setTeamId(200L);
        member1.setRole(1);
        member1.setJoinTime(new Date());
        member1.setCreateTime(new Date());
        member1.setUpdateTime(new Date());
        member1.setIsDelete(0);

        TeamMember member2 = new TeamMember();
        member2.setUserId(11L);
        member2.setTeamId(200L);
        member2.setRole(0);
        member2.setJoinTime(new Date());
        member2.setCreateTime(new Date());
        member2.setUpdateTime(new Date());
        member2.setIsDelete(0);

        TeamMember member3 = new TeamMember();
        member3.setUserId(12L);
        member3.setTeamId(200L);
        member3.setRole(0);
        member3.setJoinTime(new Date());
        member3.setCreateTime(new Date());
        member3.setUpdateTime(new Date());
        member3.setIsDelete(0);

        List<TeamMember> members = Arrays.asList(member1, member2, member3);

        // When
        boolean result = teamMemberService.saveBatch(members);

        // Then
        assertTrue(result);
        assertNotNull(member1.getId());
        assertNotNull(member2.getId());
        assertNotNull(member3.getId());
    }

    // ==================== 查询测试 ====================

    @Test
    @Order(6)
    @DisplayName("根据队伍ID查询所有成员")
    void testListByTeamId() {
        // Given
        captainMember.setTeamId(300L);
        normalMember.setTeamId(300L);
        teamMemberService.save(captainMember);
        teamMemberService.save(normalMember);

        // When
        List<TeamMember> members = teamMemberService.lambdaQuery()
                .eq(TeamMember::getTeamId, 300L)
                .eq(TeamMember::getIsDelete, 0)
                .list();

        // Then
        assertNotNull(members);
        assertEquals(2, members.size());
    }

    @Test
    @Order(7)
    @DisplayName("根据用户ID查询加入的队伍")
    void testListByUserId() {
        // Given
        TeamMember m1 = new TeamMember();
        m1.setUserId(999L);
        m1.setTeamId(101L);
        m1.setRole(0);
        m1.setJoinTime(new Date());
        m1.setCreateTime(new Date());
        m1.setUpdateTime(new Date());
        m1.setIsDelete(0);
        teamMemberService.save(m1);

        TeamMember m2 = new TeamMember();
        m2.setUserId(999L);
        m2.setTeamId(102L);
        m2.setRole(1);
        m2.setJoinTime(new Date());
        m2.setCreateTime(new Date());
        m2.setUpdateTime(new Date());
        m2.setIsDelete(0);
        teamMemberService.save(m2);

        // When
        List<TeamMember> teams = teamMemberService.lambdaQuery()
                .eq(TeamMember::getUserId, 999L)
                .eq(TeamMember::getIsDelete, 0)
                .list();

        // Then
        assertNotNull(teams);
        assertEquals(2, teams.size());
    }

    @Test
    @Order(8)
    @DisplayName("查询队伍中的队长")
    void testFindCaptain() {
        // Given
        captainMember.setTeamId(400L);
        normalMember.setTeamId(400L);
        teamMemberService.save(captainMember);
        teamMemberService.save(normalMember);

        // When
        TeamMember captain = teamMemberService.lambdaQuery()
                .eq(TeamMember::getTeamId, 400L)
                .eq(TeamMember::getRole, 1)
                .eq(TeamMember::getIsDelete, 0)
                .one();

        // Then
        assertNotNull(captain);
        assertEquals(1, captain.getRole());
        assertEquals(1L, captain.getUserId());
    }

    @Test
    @Order(9)
    @DisplayName("统计队伍成员数量")
    void testCountByTeamId() {
        // Given
        captainMember.setTeamId(500L);
        normalMember.setTeamId(500L);
        teamMemberService.save(captainMember);
        teamMemberService.save(normalMember);

        // When
        long count = teamMemberService.lambdaQuery()
                .eq(TeamMember::getTeamId, 500L)
                .eq(TeamMember::getIsDelete, 0)
                .count();

        // Then
        assertEquals(2, count);
    }

    @Test
    @Order(10)
    @DisplayName("检查用户是否已在队伍中")
    void testCheckUserInTeam() {
        // Given
        captainMember.setTeamId(600L);
        teamMemberService.save(captainMember);

        // When
        boolean exists = teamMemberService.lambdaQuery()
                .eq(TeamMember::getTeamId, 600L)
                .eq(TeamMember::getUserId, 1L)
                .eq(TeamMember::getIsDelete, 0)
                .exists();

        boolean notExists = teamMemberService.lambdaQuery()
                .eq(TeamMember::getTeamId, 600L)
                .eq(TeamMember::getUserId, 999L)
                .eq(TeamMember::getIsDelete, 0)
                .exists();

        // Then
        assertTrue(exists);
        assertFalse(notExists);
    }
}
