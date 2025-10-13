package com.miji.cms.model.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Date;

/**
 * 创建竞赛请求体
 * @author miji
 */
@Data
public class CompetitionCreateRequest implements Serializable {
    private static final long serialVersionUID = -1777167802255010268L;

    private String name;        // 竞赛名称

    private String summary;     // 简要描述

    private String content;     // 富文本内容（支持 HTML / Markdown）

    private String coverUrl;    // 封面图片地址

    private String organizer;   // 主办方

    @JsonFormat(pattern="yyyy-MM-dd'T'HH:mm:ss")
    private Date startTime; // 开始时间

    @JsonFormat(pattern="yyyy-MM-dd'T'HH:mm:ss")
    private Date endTime;   // 结束时间
}
