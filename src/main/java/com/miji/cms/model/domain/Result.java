package com.miji.cms.model.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;
import java.util.Date;
import lombok.Data;

/**
 * 成绩表
 * @TableName result
 */
@TableName(value ="result")
@Data
public class Result {
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
     * 个人赛用户id
     */
    private Long userId;

    /**
     * 团队赛队伍id
     */
    private Long teamId;

    /**
     * 成绩分数
     */
    private BigDecimal score;

    /**
     * 名次
     */
    private Integer rankNum;

    /**
     * 奖项（一等奖、二等奖等）
     */
    private String award;

    /**
     * 录入人id（教师）
     */
    private Long recordUserId;

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