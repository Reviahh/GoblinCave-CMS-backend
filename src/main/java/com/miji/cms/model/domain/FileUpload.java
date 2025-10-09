package com.miji.cms.model.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.util.Date;
import lombok.Data;

/**
 * 文件上传表
 * @TableName file_upload
 */
@TableName(value ="file_upload")
@Data
public class FileUpload {
    /**
     * id
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 所属竞赛id
     */
    private Long compId;

    /**
     * 上传人id
     */
    private Long userId;

    /**
     * 队伍id（如为团队赛）
     */
    private Long teamId;

    /**
     * 文件路径
     */
    private String filePath;

    /**
     * 文件类型（报告、作品等）
     */
    private String fileType;

    /**
     * 创建时间
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