package com.miji.cms.model.domain;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.util.Date;

/**
 * 队友招募实体
 */
@Data
@TableName("team_recruitment")
public class TeamRecruitment implements Serializable {

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
     * 招募标题
     */
    private String title;

    /**
     * 详细说明（注意：前端可能使用 content）
     */
    private String description;

    /**
     * 招募人数
     */
    private Integer maxMembers;

    /**
     * 联系方式
     */
    private String contact;

    /**
     * 状态：0-招募中，1-已满员，2-已关闭
     */
    private Integer status;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 是否删除
     */
    @TableLogic
    private Integer isDelete;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

    // ========== 为前端提供字段映射 ==========

    /**
     * 前端使用 content，后端使用 description
     */
    public String getContent() {
        return this.description;
    }

    public void setContent(String content) {
        this.description = content;
    }
}
