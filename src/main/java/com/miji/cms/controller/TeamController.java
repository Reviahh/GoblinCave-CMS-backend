package com.miji.cms.controller;

import com.miji.cms.common.BaseResponse;
import com.miji.cms.common.ErrorCode;
import com.miji.cms.common.ResultUtils;
import com.miji.cms.exception.BusinessException;
import com.miji.cms.model.request.TeamCreateRequest;
import com.miji.cms.model.request.TeamUpdateRequest; // 确保有这个类，或者用 Map
import com.miji.cms.service.TeamService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
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
     * 修改：改为 @RequestParam
     */
    @PostMapping("/join")
    public BaseResponse<Boolean> joinTeam(@RequestParam Long teamId, HttpServletRequest request) {
        if (teamId == null || teamId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "无效的队伍ID");
        }
        boolean result = teamService.joinTeam(teamId, request);
        return ResultUtils.success(result);
    }

    /**
     * 退出队伍
     * 修改：改为 @RequestParam
     */
    @PostMapping("/quit")
    public BaseResponse<Boolean> quitTeam(@RequestParam Long teamId, HttpServletRequest request) {
        if (teamId == null || teamId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "无效的队伍ID");
        }
        boolean result = teamService.quitTeam(teamId, request);
        return ResultUtils.success(result);
    }

    /**
     * 解散/删除队伍
     * 新增接口
     */
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteTeam(@RequestParam Long id, HttpServletRequest request) {
        if (id == null || id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "无效的队伍ID");
        }
        boolean result = teamService.deleteTeam(id, request);
        return ResultUtils.success(result);
    }

    /**
     * 更新队伍信息
     * 新增接口
     */
    @PostMapping("/update")
    public BaseResponse<Boolean> updateTeam(@RequestBody TeamUpdateRequest request, HttpServletRequest httpRequest) {
        if (request == null || request.getId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍ID不能为空");
        }
        boolean result = teamService.updateTeam(request, httpRequest);
        return ResultUtils.success(result);
    }

    /**
     * 查询队伍详情
     */
    @GetMapping("/detail")
    public BaseResponse<Map<String, Object>> getTeamDetail(@RequestParam Long teamId) {
        if (teamId == null || teamId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "无效的队伍ID");
        }
        return ResultUtils.success(teamService.getTeamDetail(teamId));
    }

    /**
     * 查询竞赛下的队伍列表
     * 新增接口
     */
    @GetMapping("/list")
    public BaseResponse<List<Map<String, Object>>> listTeams(@RequestParam Long competitionId) {
        if (competitionId == null || competitionId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "无效的竞赛ID");
        }
        return ResultUtils.success(teamService.listTeams(competitionId));
    }

    /**
     * 查询我的队伍列表
     * 新增接口
     */
    @GetMapping("/list/my")
    public BaseResponse<List<Map<String, Object>>> listMyTeams(HttpServletRequest request) {
        return ResultUtils.success(teamService.listMyTeams(request));
    }
}