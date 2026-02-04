package com.cloud.employee.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

@Data
@TableName("sys_resource")
public class SysResource {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private String type;
    private Integer capacity;
    private String location;
    private String facilities;
    private Integer status;
    private Date createTime;
}
