package com.miji.cms.model.request;

import lombok.Data;
import java.io.Serializable;
import java.util.Date;

/**
 * 队伍更新请求
 */
@Data
public class TeamUpdateRequest implements Serializable {

    /**
     * 队伍ID
     */
    private Long id;

    /**
     * 队伍名称
     */
    private String name;

    /**
     * 描述
     */
    private String description;

    /**
     * 最大人数
     */
    private Integer maxNum;

    /**
     * 状态（0-正常 1-满员 2-结束）
     */
    private Integer status;

    /**
     * 过期时间
     */
    private Date expireTime;

    private static final long serialVersionUID = 1L;
}
