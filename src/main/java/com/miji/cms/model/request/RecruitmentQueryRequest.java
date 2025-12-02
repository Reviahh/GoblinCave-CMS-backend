package com.miji.cms.model.request;

import lombok.Data;
import java.io.Serializable;

@Data
public class RecruitmentQueryRequest implements Serializable {
    private Long competitionId;
    private Integer isTeam; // 可选
    private Integer pageNum = 1;
    private Integer pageSize = 20;
}
