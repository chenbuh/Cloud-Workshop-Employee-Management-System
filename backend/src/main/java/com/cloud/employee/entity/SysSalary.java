package com.cloud.employee.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
@TableName("sys_salary")
public class SysSalary {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private BigDecimal baseSalary;
    private BigDecimal subsidy;
    private BigDecimal insurance;
    private BigDecimal bonus;
    private Date updateTime;
}
