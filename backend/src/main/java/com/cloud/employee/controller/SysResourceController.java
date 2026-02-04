package com.cloud.employee.controller;

import com.cloud.employee.common.result.Result;
import com.cloud.employee.entity.SysResource;
import com.cloud.employee.entity.SysResourceBooking;
import com.cloud.employee.service.ISysResourceBookingService;
import com.cloud.employee.service.ISysResourceService;
import cn.dev33.satoken.stp.StpUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/resource")
public class SysResourceController {

    @Autowired
    private ISysResourceService resourceService;

    @Autowired
    private ISysResourceBookingService bookingService;

    @Autowired
    private com.cloud.employee.service.ISysNotificationService notificationService;

    @GetMapping("/list")
    public Result<List<SysResource>> listResources() {
        return Result.success(resourceService.list());
    }

    @GetMapping("/bookings")
    public Result<List<Map<String, Object>>> listBookings() {
        return Result.success(bookingService.getBookingList());
    }

    @PostMapping("/booking")
    public Result<String> createBooking(@RequestBody SysResourceBooking booking) {
        // 1. Check for conflict
        if (bookingService.checkConflict(booking.getResourceId(), booking.getStartTime(), booking.getEndTime())) {
            return Result.error("该时间段已被预约，请选择其他时间");
        }

        // 2. Setting base fields
        booking.setUserId(StpUtil.getLoginIdAsLong());
        booking.setCreateTime(new Date());
        booking.setStatus(1);

        bookingService.save(booking);

        // 3. Send notification
        SysResource res = resourceService.getById(booking.getResourceId());
        com.cloud.employee.entity.SysNotification notify = new com.cloud.employee.entity.SysNotification();
        notify.setUserId(booking.getUserId());
        notify.setTitle("会议预约成功");
        notify.setContent("您已成功预约 " + (res != null ? res.getName() : "资源") + "，时间：" + booking.getStartTime());
        notify.setType("success");
        notify.setIsRead(0);
        notify.setCreateTime(new Date());
        notificationService.save(notify);

        return Result.success("预约成功");
    }

    @DeleteMapping("/booking/{id}")
    public Result<String> cancelBooking(@PathVariable Long id) {
        SysResourceBooking booking = bookingService.getById(id);
        if (booking != null) {
            booking.setStatus(0);
            bookingService.updateById(booking);
            return Result.success("取消成功");
        }
        return Result.error("记录不存在");
    }

    @GetMapping("/analytics")
    public Result<Map<String, Object>> getAnalytics() {
        return Result.success(bookingService.getUsageAnalytics());
    }
}
