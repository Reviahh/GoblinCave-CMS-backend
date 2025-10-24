package com.miji.cms.controller;

import com.miji.cms.common.BaseResponse;
import com.miji.cms.common.ErrorCode;
import com.miji.cms.common.ResultUtils;
import com.miji.cms.exception.BusinessException;
import com.miji.cms.model.request.TeamCreateRequest;
import com.miji.cms.service.TeamService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * 队伍接口
 *
 * @author miji
 */
@RestController
@RequestMapping("/team")
@CrossOrigin(origins = {"http://localhost:5173/","http://localhost:3000/","https://miji-frontend.vercel.app/"},allowCredentials = "true")
public class TeamController {

    @Resource
    private TeamService teamService;

    /**
     * 创建队伍
     */
    @PostMapping("/add")
    public BaseResponse<Long> createTeam(@RequestBody TeamCreateRequest request, HttpServletRequest httpRequest) {
        if (request == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数为空");
        }
        long result = teamService.createTeam(request, httpRequest);
        return ResultUtils.success(result);
    }

    /**
     * 加入队伍
     *
     * @param teamId
     * @param request
     * @return
     */
    @PostMapping("/join")
    public BaseResponse<Boolean> joinTeam(@RequestBody Long teamId, HttpServletRequest request) {
        if (teamId == null || teamId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "无效的队伍ID");
        }
        boolean result = teamService.joinTeam(teamId, request);
        return ResultUtils.success(result);
    }


    /**
     * 退出队伍
     *
     * @param teamId
     * @param request
     * @return
     */
    @PostMapping("/quit")
    public BaseResponse<Boolean> quitTeam(@RequestBody Long teamId, HttpServletRequest request) {
        if (teamId == null || teamId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "无效的队伍ID");
        }
        boolean result = teamService.quitTeam(teamId, request);
        return ResultUtils.success(result);
    }

    /**
     * 查询队伍详情
     *
     * @param teamId
     * @return
     */
    @GetMapping("/detail")
    public BaseResponse<Map<String, Object>> getTeamDetail(@RequestParam Long teamId) {
        if (teamId == null || teamId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "无效的队伍ID");
        }
        return ResultUtils.success(teamService.getTeamDetail(teamId));
    }

}
