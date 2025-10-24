package com.miji.cms.model.request;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 创建队伍请求体
 */
@Data
public class TeamCreateRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long competitionId;     // 所属竞赛ID
    private String name;            // 队伍名称
    private String description;     // 队伍简介
    private Date expireTime;        // 队伍过期时间（可选）
}
