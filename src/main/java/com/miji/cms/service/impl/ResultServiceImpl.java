package com.miji.cms.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.miji.cms.model.domain.Result;
import com.miji.cms.service.ResultService;
import com.miji.cms.mapper.ResultMapper;
import org.springframework.stereotype.Service;

/**
* @author 16427
* @description 针对表【result(成绩表)】的数据库操作Service实现
* @createDate 2025-10-09 15:09:25
*/
@Service
public class ResultServiceImpl extends ServiceImpl<ResultMapper, Result>
    implements ResultService{

}




