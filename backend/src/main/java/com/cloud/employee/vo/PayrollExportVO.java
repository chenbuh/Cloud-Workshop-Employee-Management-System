package com.cloud.employee.vo;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class PayrollExportVO {
    @ExcelProperty("工号")
    private String empCode;

    @ExcelProperty("姓名")
    private String fullName;

    @ExcelProperty("部门")
    private String deptName;

    @ExcelProperty("月份")
    private String month;

    @ExcelProperty("基本工资")
    private BigDecimal baseSalary;

    @ExcelProperty("绩效奖金")
    private BigDecimal performanceBonus;

    @ExcelProperty("扣款金额")
    private BigDecimal deduction;

    @ExcelProperty("实发工资")
    private BigDecimal actualAmount;

    @ExcelProperty("发放状态")
    private String status; // 0-待发, 1-已发
}
