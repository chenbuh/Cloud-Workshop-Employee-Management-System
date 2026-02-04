package com.cloud.employee.service;

import com.cloud.employee.entity.SysPayroll;
import java.io.File;

public interface IPayrollPdfService {
    /**
     * 为指定的工资单记录生成 PDF 文件
     */
    File generateSalarySlipPdf(SysPayroll payroll, String employeeName);
}
