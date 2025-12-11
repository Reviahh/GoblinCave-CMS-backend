package com.miji.cms.model.request;

import lombok.Data;
import java.io.Serializable;

@Data
public class RecruitmentCreateRequest implements Serializable{
    private static final long serialVersionUID = -3128779508961937944L;
    private Long competitionId;
    private Long teamId;
    private Integer isTeam;
    private String title;
    private String description;
    private String contact;
    private Integer maxMembers;
}
