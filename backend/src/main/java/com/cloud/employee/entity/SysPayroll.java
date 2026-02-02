package com.cloud.employee.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
@TableName("sys_payroll")
public class SysPayroll {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String payrollMonth;
    private BigDecimal baseSalary;
    private BigDecimal subsidy;
    private BigDecimal bonus;
    private BigDecimal insurance;
    private BigDecimal deduction;
    private BigDecimal actualAmount;
    private Integer status; // 0-未发放, 1-已发放
    private Date publishTime;
    private Date createTime;
}
