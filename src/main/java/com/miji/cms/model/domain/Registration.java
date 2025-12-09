package com.miji.cms.model.domain;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.util.Date;

@Data
@TableName("registration")
public class Registration {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private Long competitionId;

    private Long teamId;

    /**
     * 报名状态：0-待审核，1-已通过，2-已拒绝
     */
    private Integer status;

    private Date createTime;

    private Date updateTime;

    @TableLogic
    private Integer isDelete;
}