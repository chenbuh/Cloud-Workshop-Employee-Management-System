package com.cloud.employee.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

@Data
@TableName("sys_config")
public class SysConfig {
    @TableId(type = IdType.AUTO)
    private Integer configId;

    private String configName;

    private String configKey;

    private String configValue;

    private String configType; // 'Y' or 'N'

    private String remark;

    private String createBy;

    private Date createTime;

    private String updateBy;

    private Date updateTime;
}
