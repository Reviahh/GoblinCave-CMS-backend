package com.miji.cms.model.request;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class CompetitionUpdateRequest implements Serializable {

    private Long id;             // 必填，竞赛ID
    private String name;         // 竞赛名称
    private String summary;      // 简要描述
    private String content;      // 富文本内容
    private String coverUrl;     // 封面图片地址
    private String organizer;    // 主办方
    private Integer maxMembers;
    private Date startTime;    // 格式 "yyyy-MM-dd'T'HH:mm:ss"
    private Date endTime;      // 格式 "yyyy-MM-dd'T'HH:mm:ss"
}
