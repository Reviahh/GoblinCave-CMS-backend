package com.miji.cms.model.request;

import lombok.Data;

import java.util.Date;

@Data
public class SubmissionRankVO {
    private Long submissionId;

    private Long competitionId;
    private Long userId;
    private Long teamId;

    private Integer score;

    private String submitUserName;   // 根据 userId 查询
    private String teamName;         // 根据 teamId 查询

    private String description;
    private String fileUrl;

    private Date createTime;
}
