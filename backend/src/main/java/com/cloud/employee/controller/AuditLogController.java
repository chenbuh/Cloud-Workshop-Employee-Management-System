package com.cloud.employee.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cloud.employee.common.result.Result;
import com.cloud.employee.entity.SysAuditLog;
import com.cloud.employee.service.ISysAuditLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/audit")
public class AuditLogController {

    @Autowired
    private ISysAuditLogService auditLogService;

    @GetMapping("/list")
    public Result<Page<SysAuditLog>> list(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) Integer status) {
        Page<SysAuditLog> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<SysAuditLog> queryWrapper = new LambdaQueryWrapper<>();
        if (title != null && !title.isEmpty()) {
            queryWrapper.like(SysAuditLog::getTitle, title);
        }
        if (status != null) {
            queryWrapper.eq(SysAuditLog::getStatus, status);
        }
        queryWrapper.orderByDesc(SysAuditLog::getOperTime);
        return Result.success(auditLogService.page(page, queryWrapper));
    }

    @DeleteMapping("/{id}")
    public Result<Boolean> remove(@PathVariable Long id) {
        return Result.success(auditLogService.removeById(id));
    }
}
