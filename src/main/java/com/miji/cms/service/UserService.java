package com.miji.cms.service;

import com.miji.cms.model.domain.User;
import com.baomidou.mybatisplus.extension.service.IService;

/**
* @author miji
* @description 针对表【user(用户表)】的数据库操作Service
* @createDate 2025-10-09 15:09:31
*/
public interface UserService extends IService<User> {
    /**
     * 用户注册
     *
     * @param userAccount 用户账户名
     * @param userPassword 用户密码
     * @param checkPassword 校验密码
     * @return 新用户 id
     */
    long userRegister(String userAccount,String userPassword,String checkPassword);

}
