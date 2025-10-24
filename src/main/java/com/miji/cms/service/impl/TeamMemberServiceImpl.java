package com.miji.cms.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.miji.cms.model.domain.TeamMember;
import com.miji.cms.service.TeamMemberService;
import com.miji.cms.mapper.TeamMemberMapper;
import org.springframework.stereotype.Service;

/**
* @author 16427
* @description 针对表【team_member(用户-队伍关系表)】的数据库操作Service实现
* @createDate 2025-10-24 14:12:22
*/
@Service
public class TeamMemberServiceImpl extends ServiceImpl<TeamMemberMapper, TeamMember>
    implements TeamMemberService{

}




