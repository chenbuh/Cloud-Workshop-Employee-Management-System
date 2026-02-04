package com.cloud.employee.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
@TableName("sys_asset")
public class SysAsset {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private String category;
    private String sku;
    private String spec;
    private String unit;
    private Integer stock;
    private BigDecimal price;
    private String location;
    private Integer status;
    private String remark;
    private Date createTime;
}
