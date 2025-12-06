package com.miji.cms.model.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import java.util.Date;
import lombok.Data;

/**
 * 一对一会话表
 * @TableName chat_session
 */
@TableName(value ="chat_session")
@Data
public class ChatSession {
    /**
     * 
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 
     */
    private Long user1Id;

    /**
     * 
     */
    private Long user2Id;

    /**
     * 可选：来源于某条招募信息
     */
    private Long recruitmentId;

    /**
     * 
     */
    private Date createTime;

    /**
     * 
     */
    @TableLogic
    private Integer isDelete;
}