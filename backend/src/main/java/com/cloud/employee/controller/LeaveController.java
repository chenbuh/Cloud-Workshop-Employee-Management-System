package com.cloud.employee.controller;

import cn.dev33.satoken.stp.StpUtil;
// import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
// import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cloud.employee.common.result.Result;
import com.cloud.employee.entity.SysLeave;
import com.cloud.employee.service.ISysLeaveService;
import com.cloud.employee.service.ISysNotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Date;

@RestController
@RequestMapping("/api/v1/leave")
public class LeaveController {

    @Autowired
    private ISysLeaveService sysLeaveService;

    @Autowired
    private ISysNotificationService sysNotificationService;

    @GetMapping("/list")
    public Result<com.baomidou.mybatisplus.core.metadata.IPage<java.util.Map<String, Object>>> list(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Long userId) {

        Long currentUserId = StpUtil.getLoginIdAsLong();
        // Check if user has admin permission (assuming system:leave:list for now, or
        // check generic admin role)
        // If we strictly follow RBAC, we should use a permission.
        // For safety, let's assume if they have 'system:user:list' they might be admin?
        // Or better, just check if they are looking for their own data.
        // But the requirement is for Approval View which implies managers.

        boolean hasPermission = StpUtil.hasPermission("system:leave:list");

        // If verify permission logic is complex, we might default to: if not specifying
        // userId, implies all (admin).
        // If specifying userId, must matches own unless admin.

        Long queryUserId = userId;
        if (!hasPermission) {
            queryUserId = currentUserId; // Force query own data
        }

        return Result.success(sysLeaveService.getLeavePageWithEmployee(pageNum, pageSize, queryUserId, status));
    }

    @PostMapping("/apply")
    public Result<Boolean> apply(@RequestBody SysLeave sysLeave) {
        sysLeave.setUserId(StpUtil.getLoginIdAsLong());
        sysLeave.setStatus(0); // 待审批
        return Result.success(sysLeaveService.save(sysLeave));
    }

    @PutMapping("/approve")
    public Result<Boolean> approve(@RequestBody SysLeave sysLeave) {
        SysLeave leave = sysLeaveService.getById(sysLeave.getId());
        if (leave == null)
            return Result.error("记录不存在");

        leave.setStatus(sysLeave.getStatus());
        leave.setApproveBy(StpUtil.getLoginIdAsLong());
        leave.setApproveTime(new Date());

        boolean success = sysLeaveService.updateById(leave);
        if (success) {
            String statusText = sysLeave.getStatus() == 1 ? "获得通过" : "被驳回";
            String type = sysLeave.getStatus() == 1 ? "success" : "error";
            sysNotificationService.send(
                    leave.getUserId(),
                    "申请审批通知",
                    "您的 " + leave.getLeaveType() + " 申请已" + statusText,
                    type);
        }
        return Result.success(success);
    }
}
