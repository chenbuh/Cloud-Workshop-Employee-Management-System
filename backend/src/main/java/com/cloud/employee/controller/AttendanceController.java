package com.cloud.employee.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cloud.employee.common.result.Result;
import com.cloud.employee.entity.SysAttendance;
import com.cloud.employee.service.ISysAttendanceService;
import com.cloud.employee.service.ISysNotificationService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/attendance")
public class AttendanceController {

    @Autowired
    private ISysAttendanceService sysAttendanceService;

    @Autowired
    private ISysNotificationService sysNotificationService;

    @PostMapping("/clock-in")
    public Result<Boolean> clockIn(HttpServletRequest request) {
        Long userId = StpUtil.getLoginIdAsLong();
        String ip = request.getRemoteAddr();
        sysAttendanceService.clockIn(userId, ip);
        sysNotificationService.send(userId, "签到提醒", "上班打卡成功，祝您工作愉快！", "success");
        return Result.success(true);
    }

    @PostMapping("/clock-out")
    public Result<Boolean> clockOut(HttpServletRequest request) {
        Long userId = StpUtil.getLoginIdAsLong();
        String ip = request.getRemoteAddr();
        sysAttendanceService.clockOut(userId, ip);
        sysNotificationService.send(userId, "签退提醒", "下班打卡成功，辛苦了，陈总！", "info");
        return Result.success(true);
    }

    @GetMapping("/today")
    public Result<SysAttendance> getToday() {
        return Result.success(sysAttendanceService.getTodayRecord(StpUtil.getLoginIdAsLong()));
    }

    @GetMapping("/list")
    public Result<Page<SysAttendance>> list(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Long userId) {
        Page<SysAttendance> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<SysAttendance> wrapper = new LambdaQueryWrapper<>();
        if (userId != null) {
            wrapper.eq(SysAttendance::getUserId, userId);
        }
        wrapper.orderByDesc(SysAttendance::getWorkDate);
        return Result.success(sysAttendanceService.page(page, wrapper));
    }

    @GetMapping("/all")
    @cn.dev33.satoken.annotation.SaCheckPermission("system:attendance:list")
    public Result<java.util.List<java.util.Map<String, Object>>> getAllAttendance(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) Long userId) {
        return Result.success(sysAttendanceService.getAllAttendanceWithEmployee(startDate, endDate, userId));
    }

    @PostMapping("/update")
    @cn.dev33.satoken.annotation.SaCheckPermission("system:attendance:update")
    public Result<Boolean> update(@RequestBody SysAttendance attendance) {
        attendance.setUpdateTime(new java.util.Date());
        return Result.success(sysAttendanceService.updateById(attendance));
    }

    @DeleteMapping("/delete")
    @cn.dev33.satoken.annotation.SaCheckPermission("system:attendance:delete")
    public Result<Boolean> delete(@RequestParam Long id) {
        return Result.success(sysAttendanceService.removeById(id));
    }
}
