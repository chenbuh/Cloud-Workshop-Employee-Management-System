package com.cloud.employee.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cloud.employee.entity.SysResourceBooking;

import java.util.List;
import java.util.Map;

public interface ISysResourceBookingService extends IService<SysResourceBooking> {
    List<Map<String, Object>> getBookingList();

    boolean checkConflict(Long resourceId, java.util.Date start, java.util.Date end);

    java.util.Map<String, Object> getUsageAnalytics();
}
