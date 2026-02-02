package com.cloud.employee.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.util.Date;

@Data
@TableName("sys_growth_record")
public class SysGrowthRecord {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String eventTitle;
    private String eventContent;
    private String eventType; // join, promotion, award, project, training
    private Date recordDate;
    private Date createTime;
}
