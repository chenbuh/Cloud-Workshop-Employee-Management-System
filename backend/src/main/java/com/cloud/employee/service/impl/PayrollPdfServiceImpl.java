package com.cloud.employee.service.impl;

import com.cloud.employee.entity.SysPayroll;
import com.cloud.employee.service.IPayrollPdfService;
import com.itextpdf.html2pdf.HtmlConverter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Paths;

@Slf4j
@Service
public class PayrollPdfServiceImpl implements IPayrollPdfService {

    @Override
    public File generateSalarySlipPdf(SysPayroll payroll, String employeeName) {
        String tempDir = System.getProperty("java.io.tmpdir");
        String[] parts = payroll.getPayrollMonth().split("-");
        int year = Integer.parseInt(parts[0]);
        int month = Integer.parseInt(parts[1]);
        String fileName = "SalarySlip_" + employeeName + "_" + year + "_" + month + ".pdf";
        File pdfFile = Paths.get(tempDir, fileName).toFile();

        String htmlContent = String.format(
                "<html><head><style>" +
                        "body { font-family: Arial, sans-serif; padding: 20px; }" +
                        ".header { text-align: center; border-bottom: 2px solid #333; padding-bottom: 10px; }" +
                        ".content { margin-top: 20px; }" +
                        "table { width: 100%%; border-collapse: collapse; margin-top: 10px; }" +
                        "th, td { border: 1px solid #ddd; padding: 8px; text-align: left; }" +
                        "th { background-color: #f2f2f2; }" +
                        ".total { font-weight: bold; color: #d32f2f; font-size: 1.2em; }" +
                        "</style></head><body>" +
                        "<div class='header'><h1>Cloud Workshop 薪资条</h1><p>%d 年 %d 月</p></div>" +
                        "<div class='content'>" +
                        "<p><b>员工姓名：</b>%s</p>" +
                        "<table>" +
                        "<tr><th>基本工资</th><td>￥%.2f</td></tr>" +
                        "<tr><th>绩效奖金</th><td>￥%.2f</td></tr>" +
                        "<tr><th>各项补贴</th><td>￥%.2f</td></tr>" +
                        "<tr><th>五险一金</th><td style='color: red'>-￥%.2f</td></tr>" +
                        "<tr><th>其他扣款</th><td style='color: red'>-￥%.2f</td></tr>" +
                        "<tr class='total'><th>实发金额</th><td>￥%.2f</td></tr>" +
                        "</table>" +
                        "<p style='margin-top: 30px; font-size: 12px; color: #666;'>备注：此邮件由系统自动发出，如有疑问请联系人力资源部。</p>" +
                        "</div></body></html>",
                year, month, employeeName,
                payroll.getBaseSalary().doubleValue(),
                payroll.getBonus().doubleValue(),
                payroll.getSubsidy().doubleValue(),
                payroll.getInsurance().doubleValue(),
                payroll.getDeduction().doubleValue(),
                payroll.getActualAmount().doubleValue());

        try (FileOutputStream out = new FileOutputStream(pdfFile)) {
            HtmlConverter.convertToPdf(htmlContent, out);
            return pdfFile;
        } catch (Exception e) {
            log.error("生成 PDF 失败: ", e);
            return null;
        }
    }
}
