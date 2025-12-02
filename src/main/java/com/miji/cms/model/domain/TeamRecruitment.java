package com.miji.cms.model.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.util.Date;
import lombok.Data;

/**
 * 队友招募表
 * @TableName team_recruitment
 */
@TableName(value ="team_recruitment")
@Data
public class TeamRecruitment {
    /**
     * 
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 发布者用户ID
     */
    private Long userId;

    /**
     * 所属竞赛ID
     */
    private Long competitionId;

    /**
     * 相关队伍ID（可为空，表示个人）
     */
    private Long teamId;

    /**
     * 是否代表队伍发布 0-个人 1-队伍
     */
    private Integer isTeam;

    /**
     * 
     */
    private String title;

    /**
     * 
     */
    private String description;

    /**
     * 
     */
    private String contact;

    /**
     * 
     */
    private Date createTime;

    /**
     * 
     */
    private Date updateTime;

    /**
     * 
     */
    private Integer isDelete;
}