package com.miji.cms.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.miji.cms.common.ErrorCode;
import com.miji.cms.constant.UserConstant;
import com.miji.cms.exception.BusinessException;
import com.miji.cms.model.domain.Competition;
import com.miji.cms.model.domain.User;
import com.miji.cms.model.request.CompetitionCreateRequest;
import com.miji.cms.service.CompetitionService;
import com.miji.cms.mapper.CompetitionMapper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;

/**
* @author 16427
* @description 针对表【competition(竞赛信息表)】的数据库操作Service实现
* @createDate 2025-10-13 10:43:52
*/
@Service
public class CompetitionServiceImpl extends ServiceImpl<CompetitionMapper, Competition>
    implements CompetitionService{

    @Override
    public long addCompetition(CompetitionCreateRequest request, HttpServletRequest httpRequest) {
        // 1. 权限校验（只有管理员或教师可发布）
        User loginUser = (User) httpRequest.getSession().getAttribute(UserConstant.USER_LOGIN_STATE);
        if (loginUser == null || loginUser.getUserRole() == 0) {
            throw new BusinessException(ErrorCode.NO_AUTH, "无权限发布竞赛");
        }

        // 2. 参数校验
        if (StringUtils.isAnyBlank(request.getName(), request.getSummary(), request.getOrganizer())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数不完整");
        }

        // 3. 构造实体
        Competition competition = new Competition();
        BeanUtils.copyProperties(request, competition);


        // 4. 保存数据库
        boolean saveResult = this.save(competition);
        if (!saveResult) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "竞赛创建失败");
        }

        return competition.getId();
    }

}




