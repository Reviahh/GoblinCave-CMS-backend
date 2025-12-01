package com.miji.cms.controller;

import com.miji.cms.common.BaseResponse;
import com.miji.cms.common.ResultUtils;
import com.miji.cms.exception.BusinessException;
import com.miji.cms.model.domain.TeamRecruitment;
import com.miji.cms.model.request.RecruitmentCreateRequest;
import com.miji.cms.model.request.RecruitmentQueryRequest;
import com.miji.cms.service.TeamRecruitmentService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 队友招募模块接口
 */
@RestController
@RequestMapping("/recruitment")
public class RecruitmentController {

    @Resource
    private TeamRecruitmentService recruitmentService;

    /**
     * 发布寻找队友请求
     * @param req
     * @param httpRequest
     * @return
     */
    @PostMapping("/create")
    public BaseResponse<Long> create(@RequestBody RecruitmentCreateRequest req, HttpServletRequest httpRequest) {
        Long id = recruitmentService.createRecruitment(req, httpRequest);
        return ResultUtils.success(id);
    }

    /**
     * 修改寻找队友请求
     * @param recruitment
     * @param httpRequest
     * @return
     */
    @PostMapping("/update")
    public BaseResponse<Boolean> update(@RequestBody TeamRecruitment recruitment, HttpServletRequest httpRequest) {
        boolean ok = recruitmentService.updateRecruitment(recruitment, httpRequest);
        return ResultUtils.success(ok);
    }

    /**
     * 删除寻找队友请求
     * @param id
     * @param httpRequest
     * @return
     */
    @PostMapping("/delete")
    public BaseResponse<Boolean> delete(@RequestParam Long id, HttpServletRequest httpRequest) {
        boolean ok = recruitmentService.deleteRecruitment(id, httpRequest);
        return ResultUtils.success(ok);
    }

    /**
     * 获取寻找队友请求列表
     * @param req
     * @param httpRequest
     * @return
     */
    @PostMapping("/list")
    public BaseResponse<List<TeamRecruitment>> list(@RequestBody RecruitmentQueryRequest req, HttpServletRequest httpRequest) {
        List<TeamRecruitment> list = recruitmentService.listRecruitments(req, httpRequest);
        return ResultUtils.success(list);
    }

    /**
     *  获取寻找队友请求详情
     * @param id
     * @param httpRequest
     * @return
     */
    @GetMapping("/detail")
    public BaseResponse<TeamRecruitment> detail(@RequestParam Long id, HttpServletRequest httpRequest) {
        TeamRecruitment r = recruitmentService.getRecruitmentDetail(id, httpRequest);
        return ResultUtils.success(r);
    }
}
