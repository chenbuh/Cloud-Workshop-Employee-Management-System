package com.cloud.employee.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.util.Date;

@Data
@TableName("sys_flow_task")
public class SysFlowTask {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long instanceId;
    private Long stepId;
    private String title;
    private String assigneeRole;
    private Integer status; // 0待办 1完成
    private Long operatorId;
    private Date completeTime;
    private String remark;
}
