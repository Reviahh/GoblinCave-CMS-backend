package com.miji.cms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.miji.cms.mapper.ChatMessageMapper;
import com.miji.cms.model.domain.ChatMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
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

    @InjectMocks
    private ChatMessageServiceImpl chatMessageService;

    private ChatMessage testMessage;

    @BeforeEach
    void setUp() {
        testMessage = new ChatMessage();
        testMessage.setId(1L);
    }

    @Test
    @DisplayName("测试保存聊天消息")
    void testSaveChatMessage() {
        // Arrange
        when(chatMessageMapper.insert(any(ChatMessage.class))).thenReturn(1);

        // Act
        ChatMessage newMessage = new ChatMessage();
        int result = chatMessageMapper.insert(newMessage);

        // Assert
        assertEquals(1, result);
        verify(chatMessageMapper, times(1)).insert(newMessage);
    }

    @Test
    @DisplayName("测试根据ID查询聊天消息")
    void testGetById() {
        // Arrange
        when(chatMessageMapper.selectById(1L)).thenReturn(testMessage);

        // Act
        ChatMessage result = chatMessageMapper.selectById(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
    }

    @Test
    @DisplayName("测试查询不存在的消息返回null")
    void testGetByIdNotFound() {
        // Arrange
        when(chatMessageMapper.selectById(999L)).thenReturn(null);

        // Act
        ChatMessage result = chatMessageMapper.selectById(999L);

        // Assert
        assertNull(result);
    }

    @Test
    @DisplayName("测试查询聊天记录列表")
    void testListMessages() {
        // Arrange
        ChatMessage message1 = new ChatMessage();
        message1.setId(1L);

        ChatMessage message2 = new ChatMessage();
        message2.setId(2L);

        List<ChatMessage> messages = Arrays.asList(message1, message2);
        when(chatMessageMapper.selectList(any(QueryWrapper.class))).thenReturn(messages);

        // Act
        List<ChatMessage> result = chatMessageMapper.selectList(new QueryWrapper<>());

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
    }

    @Test
    @DisplayName("测试删除聊天消息")
    void testDeleteMessage() {
        // Arrange
        when(chatMessageMapper.deleteById(1L)).thenReturn(1);

        // Act
        int result = chatMessageMapper.deleteById(1L);

        // Assert
        assertEquals(1, result);
        verify(chatMessageMapper, times(1)).deleteById(1L);
    }

    @Test
    @DisplayName("测试更新聊天消息")
    void testUpdateMessage() {
        // Arrange
        when(chatMessageMapper.updateById(any(ChatMessage.class))).thenReturn(1);

        // Act
        int result = chatMessageMapper.updateById(testMessage);

        // Assert
        assertEquals(1, result);
    }

    @Test
    @DisplayName("测试批量查询聊天消息")
    void testBatchSelectMessages() {
        // Arrange
        List<Long> ids = Arrays.asList(1L, 2L, 3L);
        List<ChatMessage> messages = Arrays.asList(testMessage);
        when(chatMessageMapper.selectBatchIds(ids)).thenReturn(messages);

        // Act
        List<ChatMessage> result = chatMessageMapper.selectBatchIds(ids);

        // Assert
        assertNotNull(result);
        assertFalse(result.isEmpty());
    }

    @Test
    @DisplayName("测试查询消息数量")
    void testCountMessages() {
        // Arrange
        when(chatMessageMapper.selectCount(any(QueryWrapper.class))).thenReturn(10L);

        // Act
        Long count = chatMessageMapper.selectCount(new QueryWrapper<>());

        // Assert
        assertEquals(10L, count);
    }
}