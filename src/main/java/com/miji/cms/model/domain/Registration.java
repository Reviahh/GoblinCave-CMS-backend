package com.miji.cms.model.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.util.Date;
import lombok.Data;

/**
 * 报名表
 * @TableName registration
 */
@TableName(value ="registration")
@Data
public class Registration {
    /**
     * id
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 竞赛id
     */
    private Long compId;

    /**
     * 报名用户id
     */
    private Long userId;

    /**
     * 报名队伍id（团队赛使用）
     */
    private Long teamId;

    /**
     * 报名状态 0-待审核 1-已通过 2-驳回
     */
    private Integer status;

    /**
     * 审核人id
     */
    private Long reviewUserId;

    /**
     * 提交时间
     */
    private Date submitTime;

    /**
     * 审核时间
     */
    private Date reviewTime;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 
     */
    private Date updateTime;

    /**
     * 是否删除
     */
    private Integer isDelete;
}