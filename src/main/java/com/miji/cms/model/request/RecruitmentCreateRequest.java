package com.miji.cms.model.request;

import lombok.Data;
import java.io.Serializable;

@Data
public class RecruitmentCreateRequest implements Serializable {
    private Long competitionId;
    private Long teamId; // 可选
    private Integer isTeam; // 0 or 1
    private String title;
    private String description;
    private String contact;
}
