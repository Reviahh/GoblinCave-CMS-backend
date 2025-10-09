package com.miji.cms.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.miji.cms.model.domain.User;
import com.miji.cms.service.UserService;
import com.miji.cms.mapper.UserMapper;
import org.springframework.stereotype.Service;

/**
* @author 16427
* @description 针对表【user(用户表)】的数据库操作Service实现
* @createDate 2025-10-09 15:09:31
*/
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
    implements UserService{

}




