package com.miji.cms.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.miji.cms.model.domain.Competition;
import com.miji.cms.service.CompetitionService;
import com.miji.cms.mapper.CompetitionMapper;
import org.springframework.stereotype.Service;

/**
* @author 16427
* @description 针对表【competition(竞赛表)】的数据库操作Service实现
* @createDate 2025-10-09 15:09:15
*/
@Service
public class CompetitionServiceImpl extends ServiceImpl<CompetitionMapper, Competition>
    implements CompetitionService{

}




