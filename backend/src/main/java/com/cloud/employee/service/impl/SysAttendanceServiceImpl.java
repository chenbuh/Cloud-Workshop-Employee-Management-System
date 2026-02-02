package com.cloud.employee.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cloud.employee.entity.SysAttendance;
import com.cloud.employee.mapper.SysAttendanceMapper;
import com.cloud.employee.service.ISysAttendanceService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Date;
import org.springframework.beans.factory.annotation.Autowired;

@Service
public class SysAttendanceServiceImpl extends ServiceImpl<SysAttendanceMapper, SysAttendance>
        implements ISysAttendanceService {

    @Autowired
    private com.cloud.employee.service.ISysConfigService sysConfigService;

    @Override
    public void clockIn(Long userId, String ip) {
        SysAttendance record = getTodayRecord(userId);
        if (record != null) {
            throw new RuntimeException("今日已上班打卡");
        }
        record = new SysAttendance();
        record.setUserId(userId);
        record.setClockInTime(new Date());
        record.setWorkDate(java.sql.Date.valueOf(LocalDate.now())); // Uses current date
        record.setIpAddress(ip);

        // Determine status based on config
        String startTimeStr = sysConfigService.getValue("sys.attendance.startTime");
        if (startTimeStr == null)
            startTimeStr = "09:00";

        LocalTime startTime = LocalTime.parse(startTimeStr);
        LocalTime now = LocalTime.now();

        if (now.isAfter(startTime)) {
            record.setStatus(2); // Late
        } else {
            record.setStatus(1); // Normal
        }

        this.save(record);
    }

    @Override
    public void clockOut(Long userId, String ip) {
        SysAttendance record = getTodayRecord(userId);
        if (record == null) {
            throw new RuntimeException("请先进行上班打卡");
        }
        if (record.getClockOutTime() != null) {
            throw new RuntimeException("今日已下班打卡");
        }

        record.setClockOutTime(new Date());

        // Determine Early Leave
        String endTimeStr = sysConfigService.getValue("sys.attendance.endTime");
        if (endTimeStr == null)
            endTimeStr = "18:00";

        LocalTime endTime = LocalTime.parse(endTimeStr);
        LocalTime now = LocalTime.now();

        if (now.isBefore(endTime)) {
            // Early Leave
            if (record.getStatus() == 2) {
                record.setStatus(4); // Late + Early = Abnormal
            } else {
                record.setStatus(3); // Early Leave
            }
        }

        this.updateById(record);
    }

    @Override
    public SysAttendance getTodayRecord(Long userId) {
        return this.getOne(new LambdaQueryWrapper<SysAttendance>()
                .eq(SysAttendance::getUserId, userId)
                .eq(SysAttendance::getWorkDate, java.sql.Date.valueOf(LocalDate.now())));
    }

    @Override
    public java.util.List<java.util.Map<String, Object>> getAllAttendanceWithEmployee(String startDate, String endDate,
            Long userId) {
        return this.baseMapper.selectAllWithEmployee(startDate, endDate, userId);
    }
}
