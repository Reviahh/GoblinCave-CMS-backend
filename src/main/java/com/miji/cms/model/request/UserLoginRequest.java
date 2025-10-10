package com.miji.cms.model.request;

import lombok.Data;

import java.io.Serializable;

/**
 * 用户登录请求体
 *
 * @author miji
 */
@Data
public class UserLoginRequest implements Serializable {
    private static final long serialVersionUID = 2602959441103033982L;

    private String userAccount;

    private String userPassword;


}