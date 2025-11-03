package com.miji.cms.model.request;

import lombok.Data;

import java.io.Serializable;

@Data
public class CompetitionReviewRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 报名记录ID */
    private Long registrationId;

    /** 审核状态：1-通过，2-拒绝 */
    private Integer status;
}
