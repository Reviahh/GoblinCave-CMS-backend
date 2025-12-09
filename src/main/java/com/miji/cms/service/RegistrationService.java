package com.miji.cms.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.miji.cms.model.domain.Registration;

public interface RegistrationService extends IService<Registration> {

    Registration getMyRegistration(Long userId, Long competitionId);
}