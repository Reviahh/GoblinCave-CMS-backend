package com.miji.cms.model.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 竞赛提交作品表
 * @TableName competition_submission
 */
@TableName(value ="competition_submission")
@Data
public class Submission implements Serializable {
    private static final long serialVersionUID = 7048728379832793743L;
    /**
     * 提交ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 竞赛ID
     */
    private Long competitionId;

    /**
     * 报名记录ID
     */
    private Long registrationId;

    /**
     * 提交用户ID（个人提交）
     */
    private Long userId;

    /**
     * 提交队伍ID（团队提交）
     */
    private Long teamId;

    /**
     * 作品文件访问URL（支持多媒体、压缩包等）
     */
    private String fileUrl;

    /**
     * 作品描述（富文本）
     */
    private String description;

    /**
     * 评分（管理员评审）
     */
    private Integer score;

    /**
     * 评分管理员ID
     */
    private Long reviewerId;

    /**
     * 状态：0-已提交待评审，1-已评分
     */
    private Integer status;

    /**
     * 提交时间
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
}