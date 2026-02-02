package com.cloud.employee.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

@Data
@TableName("sys_attendance")
public class SysAttendance {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Date clockInTime;
    private Date clockOutTime;
    private Date workDate;
    private Integer status; // 1-正常, 2-迟到, 3-早退, 4-异常
    private String ipAddress;
    private Date createTime;
    private Date updateTime;
}
