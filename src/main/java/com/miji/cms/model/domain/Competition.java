package com.miji.cms.model.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;


/**
 * 竞赛信息表
 * @TableName competition
 */
@TableName(value ="competition")
@Data
public class Competition implements Serializable {
    private static final long serialVersionUID = -7848818871002747423L;

    /**
     * 竞赛ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 竞赛名称
     */
    private String name;

    /**
     * 竞赛简要介绍
     */
    private String summary;

    /**
     * 竞赛详情（富文本HTML内容）
     */
    private String content;

    /**
     * 封面图片URL
     */
    private String coverUrl;

    /**
     * 主办方
     */
    private String organizer;

    /**
     * 创建者ID
     */
    private Long creatorId;

    /**
     * 开始时间
     */
    private Date startTime;

    /**
     * 结束时间
     */
    private Date endTime;

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
    private Integer isDelete;

}