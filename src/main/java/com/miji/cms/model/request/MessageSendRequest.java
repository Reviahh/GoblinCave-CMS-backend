package com.miji.cms.model.request;

import lombok.Data;
import java.io.Serializable;

@Data
public class MessageSendRequest implements Serializable {
    private Long sessionId;
    private String content;
}
