package com.cloud.employee.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.cloud.employee.common.result.Result;
import com.cloud.employee.entity.SysNotification;
import com.cloud.employee.service.ISysNotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/notification")
public class NotificationController {

    @Autowired
    private ISysNotificationService sysNotificationService;

    @GetMapping("/list")
    public Result<List<SysNotification>> list() {
        Long userId = StpUtil.getLoginIdAsLong();
        return Result.success(sysNotificationService.list(new LambdaQueryWrapper<SysNotification>()
                .eq(SysNotification::getUserId, userId)
                .orderByDesc(SysNotification::getCreateTime)));
    }

    @GetMapping("/unread-count")
    public Result<Long> getUnreadCount() {
        Long userId = StpUtil.getLoginIdAsLong();
        return Result.success(sysNotificationService.count(new LambdaQueryWrapper<SysNotification>()
                .eq(SysNotification::getUserId, userId)
                .eq(SysNotification::getIsRead, 0)));
    }

    @PutMapping("/read-all")
    public Result<Boolean> readAll() {
        Long userId = StpUtil.getLoginIdAsLong();
        return Result.success(sysNotificationService.update(new LambdaUpdateWrapper<SysNotification>()
                .eq(SysNotification::getUserId, userId)
                .set(SysNotification::getIsRead, 1)));
    }

    @PutMapping("/read/{id}")
    public Result<Boolean> read(@PathVariable Long id) {
        SysNotification notification = new SysNotification();
        notification.setId(id);
        notification.setIsRead(1);
        return Result.success(sysNotificationService.updateById(notification));
    }
}
