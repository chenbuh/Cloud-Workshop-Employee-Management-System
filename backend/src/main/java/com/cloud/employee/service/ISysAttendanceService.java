package com.cloud.employee.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cloud.employee.entity.SysAttendance;

public interface ISysAttendanceService extends IService<SysAttendance> {
    void clockIn(Long userId, String ip);

    void clockOut(Long userId, String ip);

    SysAttendance getTodayRecord(Long userId);

    java.util.List<java.util.Map<String, Object>> getAllAttendanceWithEmployee(String startDate, String endDate,
            Long userId);
}
