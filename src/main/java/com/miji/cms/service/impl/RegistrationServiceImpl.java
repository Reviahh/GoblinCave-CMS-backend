package com.miji.cms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.miji.cms.mapper.RegistrationMapper;
import com.miji.cms.model.domain.Registration;
import com.miji.cms.service.RegistrationService;
import org.springframework.stereotype.Service;

@Service
public class RegistrationServiceImpl extends ServiceImpl<RegistrationMapper, Registration>
        implements RegistrationService {

    @Override
    public Registration getMyRegistration(Long userId, Long competitionId) {
        LambdaQueryWrapper<Registration> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Registration::getUserId, userId)
                .eq(Registration::getCompetitionId, competitionId)
                .eq(Registration::getIsDelete, 0);
        return this.getOne(wrapper);
    }
}