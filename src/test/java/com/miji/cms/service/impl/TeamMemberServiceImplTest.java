package com.miji.cms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.miji.cms.mapper.TeamMemberMapper;
import com.miji.cms.model.domain.TeamMember;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * TeamMemberServiceImpl 单元测试
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("队伍成员服务测试")
class TeamMemberServiceImplTest {

    @Mock
    private TeamMemberMapper teamMemberMapper;

    @Spy
    @InjectMocks
    private TeamMemberServiceImpl teamMemberService;

    private TeamMember testMember;

    @BeforeEach
    void setUp() {
        // 注入 baseMapper，保证 ServiceImpl 继承的方法正常工作
        ReflectionTestUtils.setField(teamMemberService, "baseMapper", teamMemberMapper);

        testMember = new TeamMember();
        testMember.setId(1L);
        testMember.setUserId(10L);
        testMember.setTeamId(20L);
        testMember.setRole(1);
        Date now = new Date();
        testMember.setJoinTime(now);
        testMember.setCreateTime(now);
        testMember.setUpdateTime(now);
        testMember.setIsDelete(0);
    }

    @Nested
    @DisplayName("保存成员测试")
    class SaveMemberTests {

        @Test
        @DisplayName("成功保存成员")
        void testSaveMember_Success() {
            when(teamMemberMapper.insert(any(TeamMember.class))).thenReturn(1);

            boolean result = teamMemberService.save(testMember);

            assertTrue(result);
            verify(teamMemberMapper, times(1)).insert(any(TeamMember.class));
        }

        @Test
        @DisplayName("保存成员失败")
        void testSaveMember_Failure() {
            when(teamMemberMapper.insert(any(TeamMember.class))).thenReturn(0);

            boolean result = teamMemberService.save(testMember);

            assertFalse(result);
        }
    }

    @Nested
    @DisplayName("查询成员测试")
    class GetMemberTests {

        @Test
        @DisplayName("根据ID查询成员成功")
        void testGetById_Success() {
            when(teamMemberMapper.selectById(1L)).thenReturn(testMember);

            TeamMember result = teamMemberService.getById(1L);

            assertNotNull(result);
            assertEquals(1L, result.getId());
            assertEquals(10L, result.getUserId());
        }

        @Test
        @DisplayName("查询不存在的成员返回null")
        void testGetById_NotFound() {
            when(teamMemberMapper.selectById(999L)).thenReturn(null);

            TeamMember result = teamMemberService.getById(999L);

            assertNull(result);
        }
    }

    @Nested
    @DisplayName("列表查询测试")
    class ListMemberTests {

        @Test
        @DisplayName("查询成员列表成功")
        void testListMembers_Success() {
            TeamMember m1 = new TeamMember();
            m1.setId(1L);
            TeamMember m2 = new TeamMember();
            m2.setId(2L);
            List<TeamMember> list = Arrays.asList(m1, m2);

            when(teamMemberMapper.selectList(any(QueryWrapper.class))).thenReturn(list);

            List<TeamMember> result = teamMemberService.list(new QueryWrapper<>());

            assertNotNull(result);
            assertEquals(2, result.size());
        }

        @Test
        @DisplayName("查询空列表")
        void testListMembers_Empty() {
            when(teamMemberMapper.selectList(any(QueryWrapper.class))).thenReturn(Collections.emptyList());

            List<TeamMember> result = teamMemberService.list(new QueryWrapper<>());

            assertNotNull(result);
            assertTrue(result.isEmpty());
        }
    }

    @Nested
    @DisplayName("统计成员测试")
    class CountMemberTests {

        @Test
        @DisplayName("统计成员数量")
        void testCountMembers_Success() {
            when(teamMemberMapper.selectCount(any(QueryWrapper.class))).thenReturn(5L);

            long count = teamMemberService.count(new QueryWrapper<>());

            assertEquals(5L, count);
        }

        @Test
        @DisplayName("统计空表成员数量")
        void testCountMembers_Empty() {
            when(teamMemberMapper.selectCount(any(QueryWrapper.class))).thenReturn(0L);

            long count = teamMemberService.count(new QueryWrapper<>());

            assertEquals(0L, count);
        }
    }

    @Nested
    @DisplayName("成员实体测试")
    class MemberEntityTests {

        @Test
        @DisplayName("成员实体属性赋值")
        void testTeamMemberEntity_AllFields() {
            TeamMember m = new TeamMember();
            Date now = new Date();

            m.setId(1L);
            m.setUserId(10L);
            m.setTeamId(20L);
            m.setRole(1);
            m.setJoinTime(now);
            m.setCreateTime(now);
            m.setUpdateTime(now);
            m.setIsDelete(0);

            assertEquals(1L, m.getId());
            assertEquals(10L, m.getUserId());
            assertEquals(20L, m.getTeamId());
            assertEquals(1, m.getRole());
            assertEquals(now, m.getJoinTime());
            assertEquals(now, m.getCreateTime());
            assertEquals(now, m.getUpdateTime());
            assertEquals(0, m.getIsDelete());
        }

        @Test
        @DisplayName("成员实体默认值为null")
        void testTeamMemberEntity_DefaultNull() {
            TeamMember m = new TeamMember();

            assertNull(m.getId());
            assertNull(m.getUserId());
            assertNull(m.getTeamId());
            assertNull(m.getRole());
            assertNull(m.getJoinTime());
            assertNull(m.getCreateTime());
            assertNull(m.getUpdateTime());
            assertNull(m.getIsDelete());
        }
    }
}

