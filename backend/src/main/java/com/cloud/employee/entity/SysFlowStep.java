package com.cloud.employee.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("sys_flow_step")
public class SysFlowStep {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long templateId;
    private String title;
    private String assigneeRole; // HR, IT, ADMIN
    private Integer sortOrder;
}
