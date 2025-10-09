package com.miji.cms.model.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.util.Date;
import lombok.Data;

/**
 * 竞赛表
 * @TableName competition
 */
@TableName(value ="competition")
@Data
public class Competition {
    /**
     * id
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 竞赛名称
     */
    private String compName;

    /**
     * 学科类别
     */
    private String category;

    /**
     * 主办方
     */
    private String organizer;

    /**
     * 竞赛简介
     */
    private String description;

    /**
     * 开始时间
     */
    private Date startTime;

    /**
     * 结束时间
     */
    private Date endTime;

    /**
     * 是否团队赛 0-否 1-是
     */
    private Integer isTeam;

    /**
     * 最大队伍人数
     */
    private Integer maxTeamSize;

    /**
     * 创建者id（教师或管理员）
     */
    private Long createUserId;

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