package com.miji.cms.model.request;

import lombok.Data;

import java.io.Serializable;

/**
 * 查询作品提交记录请求体
 */
@Data
public class SubmissionQueryRequest implements Serializable {

    /**
     * 竞赛ID（可选）
     */
    private Long competitionId;

    /**
     * 报名ID（可选）
     */
    private Long registrationId;

    /**
     * 用户ID（管理员可查任意用户，本人只能查自己）
     */
    private Long userId;

    /**
     * 排序字段（createTime / updateTime）
     */
    private String sortField;

    /**
     * 排序顺序 asc/desc
     */
    private String sortOrder;

    /**
     * 页码（可选）
     */
    private Integer pageNum = 1;

    /**
     * 每页数量（可选）
     */
    private Integer pageSize = 10;

    private static final long serialVersionUID = 1L;
}
