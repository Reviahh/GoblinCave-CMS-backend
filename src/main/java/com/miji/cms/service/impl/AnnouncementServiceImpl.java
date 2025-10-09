package com.miji.cms.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.miji.cms.model.domain.Announcement;
import com.miji.cms.service.AnnouncementService;
import com.miji.cms.mapper.AnnouncementMapper;
import org.springframework.stereotype.Service;

/**
* @author 16427
* @description 针对表【announcement(公告表)】的数据库操作Service实现
* @createDate 2025-10-09 15:08:50
*/
@Service
public class AnnouncementServiceImpl extends ServiceImpl<AnnouncementMapper, Announcement>
    implements AnnouncementService{

}




