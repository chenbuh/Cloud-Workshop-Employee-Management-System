package com.cloud.employee.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

@Data
@TableName("sys_leave")
public class SysLeave {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String leaveType;
    private Date startTime;
    private Date endTime;
    private String reason;
    private Integer status; // 0-待审批, 1-通过, 2-驳回
    private Long approveBy;
    private Date approveTime;
    private Date createTime;
    private Date updateTime;
}
