package com.cloud.employee.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

@Data
@TableName("sys_resource_booking")
public class SysResourceBooking {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long resourceId;
    private Long userId;
    private String title;
    private Date startTime;
    private Date endTime;
    private Integer status;
    private String remark;
    private Date createTime;
}
