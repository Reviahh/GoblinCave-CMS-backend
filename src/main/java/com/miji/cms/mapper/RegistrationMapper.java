package com.miji.cms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.miji.cms.model.domain.Registration;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface RegistrationMapper extends BaseMapper<Registration> {
}