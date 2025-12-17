package com.miji.cms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.miji.cms.mapper.ChatMessageMapper;
import com.miji.cms.model.domain.ChatMessage;
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
 * ChatMessageServiceImpl 单元测试
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("聊天消息服务测试")
class ChatMessageServiceImplTest {

    @Mock
    private ChatMessageMapper chatMessageMapper;

    @Spy
    @InjectMocks
    private ChatMessageServiceImpl chatMessageService;

    private ChatMessage testMessage;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(chatMessageService, "baseMapper", chatMessageMapper);
        
        testMessage = new ChatMessage();
        testMessage.setId(1L);
        testMessage.setContent("测试消息内容");
        testMessage.setSenderId(1L);
        testMessage.setSenderName("测试用户");
        testMessage.setRecruitmentId(100L);
        testMessage.setCreateTime(new Date());
        testMessage.setIsDelete(0);
    }

    @Nested
    @DisplayName("保存消息测试")
    class SaveMessageTests {

        @Test
        @DisplayName("成功保存聊天消息")
        void testSaveChatMessage_Success() {
            when(chatMessageMapper.insert(any(ChatMessage.class))).thenReturn(1);

            ChatMessage newMessage = new ChatMessage();
            newMessage.setContent("新消息");
            newMessage.setSenderId(1L);
            newMessage.setSenderName("发送者");
            newMessage.setRecruitmentId(100L);

            boolean result = chatMessageService.save(newMessage);

            assertTrue(result);
            verify(chatMessageMapper, times(1)).insert(any(ChatMessage.class));
        }

        @Test
        @DisplayName("保存空内容消息")
        void testSaveChatMessage_EmptyContent() {
            when(chatMessageMapper.insert(any(ChatMessage.class))).thenReturn(1);

            ChatMessage newMessage = new ChatMessage();
            newMessage.setContent("");
            newMessage.setSenderId(1L);
            newMessage.setSenderName("发送者");
            newMessage.setRecruitmentId(100L);

            boolean result = chatMessageService.save(newMessage);

            assertTrue(result);
        }

        @Test
        @DisplayName("保存消息失败")
        void testSaveChatMessage_Failure() {
            when(chatMessageMapper.insert(any(ChatMessage.class))).thenReturn(0);

            ChatMessage newMessage = new ChatMessage();
            boolean result = chatMessageService.save(newMessage);

            assertFalse(result);
        }
    }

    @Nested
    @DisplayName("查询消息测试")
    class GetMessageTests {

        @Test
        @DisplayName("根据ID查询聊天消息成功")
        void testGetById_Success() {
            when(chatMessageMapper.selectById(1L)).thenReturn(testMessage);

            ChatMessage result = chatMessageService.getById(1L);

            assertNotNull(result);
            assertEquals(1L, result.getId());
            assertEquals("测试消息内容", result.getContent());
        }

        @Test
        @DisplayName("查询不存在的消息返回null")
        void testGetById_NotFound() {
            when(chatMessageMapper.selectById(999L)).thenReturn(null);

            ChatMessage result = chatMessageService.getById(999L);

            assertNull(result);
        }

        @Test
        @DisplayName("根据null ID查询")
        void testGetById_NullId() {
            ChatMessage result = chatMessageService.getById(null);

            assertNull(result);
        }
    }

    @Nested
    @DisplayName("查询消息列表测试")
    class ListMessagesTests {

        @Test
        @DisplayName("查询聊天记录列表成功")
        void testListMessages_Success() {
            ChatMessage message1 = new ChatMessage();
            message1.setId(1L);
            message1.setContent("消息1");

            ChatMessage message2 = new ChatMessage();
            message2.setId(2L);
            message2.setContent("消息2");

            List<ChatMessage> messages = Arrays.asList(message1, message2);
            when(chatMessageMapper.selectList(any(QueryWrapper.class))).thenReturn(messages);

            List<ChatMessage> result = chatMessageService.list(new QueryWrapper<>());

            assertNotNull(result);
            assertEquals(2, result.size());
        }

        @Test
        @DisplayName("查询空列表")
        void testListMessages_Empty() {
            when(chatMessageMapper.selectList(any(QueryWrapper.class))).thenReturn(Collections.emptyList());

            List<ChatMessage> result = chatMessageService.list(new QueryWrapper<>());

            assertNotNull(result);
            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("根据发送者查询消息")
        void testListMessages_BySenderId() {
            ChatMessage message = new ChatMessage();
            message.setId(1L);
            message.setSenderId(1L);

            when(chatMessageMapper.selectList(any(QueryWrapper.class))).thenReturn(Arrays.asList(message));

            QueryWrapper<ChatMessage> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("senderId", 1L);
            List<ChatMessage> result = chatMessageService.list(queryWrapper);

            assertNotNull(result);
            assertEquals(1, result.size());
            assertEquals(1L, result.get(0).getSenderId());
        }

        @Test
        @DisplayName("根据招募帖ID查询消息")
        void testListMessages_ByRecruitmentId() {
            ChatMessage message = new ChatMessage();
            message.setId(1L);
            message.setRecruitmentId(100L);

            when(chatMessageMapper.selectList(any(QueryWrapper.class))).thenReturn(Arrays.asList(message));

            QueryWrapper<ChatMessage> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("recruitmentId", 100L);
            List<ChatMessage> result = chatMessageService.list(queryWrapper);

            assertNotNull(result);
            assertEquals(1, result.size());
        }
    }

    @Nested
    @DisplayName("批量查询测试")
    class BatchQueryTests {

        @Test
        @DisplayName("批量查询聊天消息成功")
        void testBatchSelectMessages_Success() {
            List<Long> ids = Arrays.asList(1L, 2L, 3L);
            List<ChatMessage> messages = Arrays.asList(testMessage);
            when(chatMessageMapper.selectBatchIds(ids)).thenReturn(messages);

            List<ChatMessage> result = chatMessageService.listByIds(ids);

            assertNotNull(result);
            assertFalse(result.isEmpty());
        }

        @Test
        @DisplayName("批量查询空ID列表")
        void testBatchSelectMessages_EmptyIds() {
            List<ChatMessage> result = chatMessageService.listByIds(Collections.emptyList());

            assertNotNull(result);
            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("批量查询部分存在的ID")
        void testBatchSelectMessages_PartialExists() {
            List<Long> ids = Arrays.asList(1L, 999L);
            when(chatMessageMapper.selectBatchIds(ids)).thenReturn(Arrays.asList(testMessage));

            List<ChatMessage> result = chatMessageService.listByIds(ids);

            assertNotNull(result);
            assertEquals(1, result.size());
        }
    }

    @Nested
    @DisplayName("统计消息测试")
    class CountMessagesTests {

        @Test
        @DisplayName("查询消息数量")
        void testCountMessages_Success() {
            when(chatMessageMapper.selectCount(any(QueryWrapper.class))).thenReturn(10L);

            long count = chatMessageService.count(new QueryWrapper<>());

            assertEquals(10L, count);
        }

        @Test
        @DisplayName("查询空表消息数量")
        void testCountMessages_Empty() {
            when(chatMessageMapper.selectCount(any(QueryWrapper.class))).thenReturn(0L);

            long count = chatMessageService.count(new QueryWrapper<>());

            assertEquals(0L, count);
        }

        @Test
        @DisplayName("按条件统计消息数量")
        void testCountMessages_WithCondition() {
            when(chatMessageMapper.selectCount(any(QueryWrapper.class))).thenReturn(5L);

            QueryWrapper<ChatMessage> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("senderId", 1L);
            long count = chatMessageService.count(queryWrapper);

            assertEquals(5L, count);
        }
    }

    @Nested
    @DisplayName("消息实体测试")
    class MessageEntityTests {

        @Test
        @DisplayName("测试消息实体属性")
        void testMessageEntity() {
            ChatMessage message = new ChatMessage();
            message.setId(1L);
            message.setContent("测试内容");
            message.setSenderId(10L);
            message.setSenderName("发送者");
            message.setRecruitmentId(100L);
            Date now = new Date();
            message.setCreateTime(now);
            message.setIsDelete(0);

            assertEquals(1L, message.getId());
            assertEquals("测试内容", message.getContent());
            assertEquals(10L, message.getSenderId());
            assertEquals("发送者", message.getSenderName());
            assertEquals(100L, message.getRecruitmentId());
            assertEquals(now, message.getCreateTime());
            assertEquals(0, message.getIsDelete());
        }

        @Test
        @DisplayName("测试消息实体null属性")
        void testMessageEntity_NullProperties() {
            ChatMessage message = new ChatMessage();

            assertNull(message.getId());
            assertNull(message.getContent());
            assertNull(message.getSenderId());
            assertNull(message.getSenderName());
            assertNull(message.getRecruitmentId());
        }
    }
}