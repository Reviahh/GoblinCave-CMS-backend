package com.miji.cms.model.domain;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.util.Date;

/**
 * 聊天消息实体（仅公开留言）
 */
@Data
@TableName("chat_message")
public class ChatMessage implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 招募帖ID（公开留言使用）
     */
    private Long recruitmentId;

    /**
     * 发送者ID
     */
    private Long senderId;

    /**
     * 发送者用户名（冗余字段，避免频繁JOIN）
     */
    private String senderName;

    /**
     * 消息内容
     */
    private String content;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 是否删除
     */
    @TableLogic
    private Integer isDelete;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
