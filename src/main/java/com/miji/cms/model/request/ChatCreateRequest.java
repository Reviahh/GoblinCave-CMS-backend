package com.miji.cms.model.request;

import lombok.Data;
import java.io.Serializable;

@Data
public class ChatCreateRequest implements Serializable {
    private Long targetUserId;
    private Long recruitmentId; // 可选，发起会话时关联某条招募
}
