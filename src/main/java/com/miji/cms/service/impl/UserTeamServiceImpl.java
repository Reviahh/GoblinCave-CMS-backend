package com.miji.cms.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.miji.cms.model.domain.UserTeam;
import com.miji.cms.service.UserTeamService;
import com.miji.cms.mapper.UserTeamMapper;
import org.springframework.stereotype.Service;

/**
* @author 16427
* @description 针对表【user_team(用户-队伍关系表)】的数据库操作Service实现
* @createDate 2025-10-09 15:09:34
*/
@Service
public class UserTeamServiceImpl extends ServiceImpl<UserTeamMapper, UserTeam>
    implements UserTeamService{

}




