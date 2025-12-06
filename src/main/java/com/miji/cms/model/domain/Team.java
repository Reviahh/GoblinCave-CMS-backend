package com.miji.cms.model.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 队伍表
 * @TableName team
 */
@TableName(value ="team")
@Data
public class Team implements Serializable {
    private static final long serialVersionUID = -519129029463658215L;
    /**
     * 队伍ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 所属竞赛ID
     */
    private Long competitionId;

    /**
     * 队伍名称
     */
    private String name;

    /**
     * 队伍简介
     */
    private String description;

    /**
     * 队伍最大人数（默认等于竞赛人数上限）
     */
    private Integer maxNum;

    /**
     * 当前队伍人数（含队长）
     */
    private Integer currentNum;

    /**
     * 队伍状态：0-正常，1-已满员，2-已报名，3-解散
     */
    private Integer status;

    /**
     * 队伍过期时间（可用于自动解散）
     */
    private Date expireTime;

    /**
     * 队长ID（用户ID）
     */
    private Long userId;

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
    @TableLogic
    private Integer isDelete;
}