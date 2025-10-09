package com.miji.cms.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.miji.cms.model.domain.Team;
import com.miji.cms.service.TeamService;
import com.miji.cms.mapper.TeamMapper;
import org.springframework.stereotype.Service;

/**
* @author 16427
* @description 针对表【team(队伍表)】的数据库操作Service实现
* @createDate 2025-10-09 15:09:28
*/
@Service
public class TeamServiceImpl extends ServiceImpl<TeamMapper, Team>
    implements TeamService{

}




