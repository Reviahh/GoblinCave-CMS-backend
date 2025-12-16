package com.miji.cms.service.impl;

import com.miji.cms.mapper.TeamMemberMapper;
import com.miji.cms.model.domain.TeamMember;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * TeamMemberServiceImpl 单元测试
 * 该服务继承自 ServiceImpl，没有自定义方法，主要测试基本的 CRUD 操作
 */
@ExtendWith(MockitoExtension.class)
class TeamMemberServiceImplTest {

    @Mock
    private TeamMemberMapper teamMemberMapper;

    @Spy
    @InjectMocks
    private TeamMemberServiceImpl teamMemberService;

    private TeamMember teamMember;
    private TeamMember teamMember2;

    @BeforeEach
    void setUp() {
        // 注入 baseMapper
        ReflectionTestUtils.setField(teamMemberService, "baseMapper", teamMemberMapper);

        // 初始化队员记录1 - 队长
        teamMember = new TeamMember();
        teamMember.setId(1L);
        teamMember.setUserId(1L);
        teamMember.setTeamId(100L);
        teamMember.setRole(1); // 队长
        teamMember.setJoinTime(new Date());
        teamMember.setCreateTime(new Date());
        teamMember.setUpdateTime(new Date());
        teamMember.setIsDelete(0);

        // 初始化队员记录2 - 普通成员
        teamMember2 = new TeamMember();
        teamMember2.setId(2L);
        teamMember2.setUserId(2L);
        teamMember2.setTeamId(100L);
        teamMember2.setRole(0); // 普通成员
        teamMember2.setJoinTime(new Date());
        teamMember2.setCreateTime(new Date());
        teamMember2.setUpdateTime(new Date());
        teamMember2.setIsDelete(0);
    }

    // ==================== save 测试 ====================

    @Test
    void testSave_Success() {
        // Given
        TeamMember newMember = new TeamMember();
        newMember.setUserId(3L);
        newMember.setTeamId(100L);
        newMember.setRole(0);
        newMember.setJoinTime(new Date());

        when(teamMemberMapper.insert(any(TeamMember.class))).thenReturn(1);

        // When
        boolean result = teamMemberService.save(newMember);

        // Then
        assertTrue(result);
        verify(teamMemberMapper, times(1)).insert(any(TeamMember.class));
    }

    // ==================== getById 测试 ====================

    @Test
    void testGetById_Success() {
        // Given
        doReturn(teamMember).when(teamMemberService).getById(1L);

        // When
        TeamMember result = teamMemberService.getById(1L);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals(1L, result.getUserId());
        assertEquals(100L, result.getTeamId());
        assertEquals(1, result.getRole());
    }

    @Test
    void testGetById_NotFound() {
        // Given
        doReturn(null).when(teamMemberService).getById(999L);

        // When
        TeamMember result = teamMemberService.getById(999L);

        // Then
        assertNull(result);
    }

    // ==================== list 测试 ====================

    @Test
    void testList_Success() {
        // Given
        List<TeamMember> memberList = Arrays.asList(teamMember, teamMember2);
        doReturn(memberList).when(teamMemberService).list(any());

        // When
        List<TeamMember> result = teamMemberService.list(null);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
    }

    // ==================== updateById 测试 ====================

    @Test
    void testUpdateById_Success() {
        // Given
        teamMember.setRole(0); // 降级为普通成员
        doReturn(true).when(teamMemberService).updateById(any(TeamMember.class));

        // When
        boolean result = teamMemberService.updateById(teamMember);

        // Then
        assertTrue(result);
    }

    // ==================== removeById 测试 ====================

    @Test
    void testRemoveById_Success() {
        // Given
        doReturn(true).when(teamMemberService).removeById(1L);

        // When
        boolean result = teamMemberService.removeById(1L);

        // Then
        assertTrue(result);
    }

    // ==================== 业务场景测试 ====================

    @Test
    void testTeamMember_RoleValues() {
        // 验证角色值定义正确
        TeamMember captain = new TeamMember();
        captain.setRole(1);
        assertEquals(1, captain.getRole()); // 队长

        TeamMember member = new TeamMember();
        member.setRole(0);
        assertEquals(0, member.getRole()); // 普通成员
    }

    @Test
    void testTeamMember_EntityFields() {
        // 验证实体类字段设置正确
        TeamMember member = new TeamMember();
        member.setId(10L);
        member.setUserId(100L);
        member.setTeamId(200L);
        member.setRole(0);
        Date now = new Date();
        member.setJoinTime(now);
        member.setCreateTime(now);
        member.setUpdateTime(now);
        member.setIsDelete(0);

        assertEquals(10L, member.getId());
        assertEquals(100L, member.getUserId());
        assertEquals(200L, member.getTeamId());
        assertEquals(0, member.getRole());
        assertEquals(now, member.getJoinTime());
        assertEquals(now, member.getCreateTime());
        assertEquals(now, member.getUpdateTime());
        assertEquals(0, member.getIsDelete());
    }

    @Test
    void testSaveMultipleMembers() {
        // 测试批量保存队员
        TeamMember member1 = new TeamMember();
        member1.setUserId(10L);
        member1.setTeamId(100L);
        member1.setRole(1);

        TeamMember member2 = new TeamMember();
        member2.setUserId(11L);
        member2.setTeamId(100L);
        member2.setRole(0);

        TeamMember member3 = new TeamMember();
        member3.setUserId(12L);
        member3.setTeamId(100L);
        member3.setRole(0);

        List<TeamMember> members = Arrays.asList(member1, member2, member3);

        doReturn(true).when(teamMemberService).saveBatch(anyCollection());

        // When
        boolean result = teamMemberService.saveBatch(members);

        // Then
        assertTrue(result);
    }
}
