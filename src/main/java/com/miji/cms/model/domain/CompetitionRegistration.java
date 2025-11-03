package com.miji.cms.model.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.util.Date;
import lombok.Data;

/**
 * 竞赛报名表
 * @TableName competition_registration
 */
@TableName(value ="competition_registration")
@Data
public class CompetitionRegistration {
    /**
     * 报名ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 竞赛ID
     */
    private Long competitionId;

    /**
     * 报名用户ID（个人赛）
     */
    private Long userId;

    /**
     * 报名队伍ID（团队赛）
     */
    private Long teamId;

    /**
     * 状态：0-待审核，1-已通过，2-拒绝
     */
    private Integer status;

    /**
     * 报名时间
     */
    private Date createTime;

    /**
     * 
     */
    private Date updateTime;

    /**
     * 是否删除
     */
    private Integer isDelete;
}