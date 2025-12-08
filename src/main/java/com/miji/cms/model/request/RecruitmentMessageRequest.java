package com.miji.cms.model.request;

import lombok.Data;
import java.io.Serializable;

/**
 * 招募帖留言请求
 */
@Data
public class RecruitmentMessageRequest implements Serializable {

    /**
     * 招募帖ID
     */
    private Long recruitmentId;

    /**
     * 留言内容
     */
    private String content;

    private static final long serialVersionUID = 1L;
}
