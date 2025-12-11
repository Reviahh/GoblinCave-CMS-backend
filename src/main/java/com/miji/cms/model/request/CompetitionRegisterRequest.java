package com.miji.cms.model.request;

import lombok.Data;

import java.io.Serializable;

@Data
public class CompetitionRegisterRequest implements Serializable {
    private static final long serialVersionUID = 3922573745646694659L;

    /** 竞赛ID（必填） */
    private Long competitionId;

    /** 队伍ID（可选） */
    private Long teamId;
}
