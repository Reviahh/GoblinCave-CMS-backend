package com.miji.cms.model.request;

import lombok.Data;
import java.io.Serializable;

@Data
public class MessageQueryRequest implements Serializable {
    private Long sessionId;
    private Integer pageNum = 1;
    private Integer pageSize = 50;
}
