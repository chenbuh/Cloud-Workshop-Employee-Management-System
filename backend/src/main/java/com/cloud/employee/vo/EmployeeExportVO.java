package com.cloud.employee.vo;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

import java.util.Date;

@Data
public class EmployeeExportVO {
    @ExcelProperty("工号")
    private String empCode;

    @ExcelProperty("姓名")
    private String fullName;

    @ExcelProperty("部门")
    private String deptName;

    @ExcelProperty("职位")
    private String jobTitle;

    @ExcelProperty("状态")
    private String status; // 1-正式, 2-试用, 3-离职

    @ExcelProperty("联系电话")
    private String phone;

    @ExcelProperty("入职日期")
    private Date hireDate;
}
