package com.cloud.employee.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.util.Date;

@Data
@TableName("sys_flow_template")
public class SysFlowTemplate {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private String type; // ONBOARDING, OFFBOARDING
    private String description;
    private Integer status;
    private Date createTime;
}
