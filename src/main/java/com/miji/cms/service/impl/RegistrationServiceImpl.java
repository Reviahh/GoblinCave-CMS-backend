package com.miji.cms.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.miji.cms.model.domain.Registration;
import com.miji.cms.service.RegistrationService;
import com.miji.cms.mapper.RegistrationMapper;
import org.springframework.stereotype.Service;

/**
* @author 16427
* @description 针对表【registration(报名表)】的数据库操作Service实现
* @createDate 2025-10-09 15:09:22
*/
@Service
public class RegistrationServiceImpl extends ServiceImpl<RegistrationMapper, Registration>
    implements RegistrationService{

}




