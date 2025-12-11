package com.miji.cms.model.request;

import lombok.Data;

import java.io.Serializable;

/**
 * 提交作品请求体
 */
@Data
public class SubmissionSubmitRequest implements Serializable {

    /**
     * 竞赛ID（可选）
     */
    private Long competitionId;

    /**
     * 报名ID（每个报名只能提交一次，可覆盖旧稿）
     */
    private Long registrationId;

    /**
     * 作品描述（可为富文本）
     */
    private String description;


    private static final long serialVersionUID = -6832795906753266308L;
}
