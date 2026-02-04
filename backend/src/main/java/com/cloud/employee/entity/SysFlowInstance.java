package com.cloud.employee.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.util.Date;

@Data
@TableName("sys_flow_instance")
public class SysFlowInstance {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long templateId;
    private Long employeeId;
    private Integer status; // 0进行中 1已完成 2已中止
    private Integer progress;
    private Date startTime;
    private Date endTime;
}
