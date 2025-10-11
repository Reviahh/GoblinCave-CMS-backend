package com.miji.cms.model.request;

import lombok.Data;

import java.io.Serializable;

/**
 * 用户注册请求体
 *
 * @author miji
 */
@Data
public class UserRegisterRequest implements Serializable {
    private static final long serialVersionUID = 3201085748138190325L;

    private String userAccount;

    private String userPassword;

    private String checkPassword;

    private Integer userRole;
}
