package com.cloud.employee.controller;

import com.cloud.employee.common.result.Result;
import com.cloud.employee.entity.SysPayroll;
import com.cloud.employee.service.ISysPayrollService;
import com.cloud.employee.service.ISysNotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;
import java.util.Map;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.cloud.employee.vo.PayrollExportVO;
import com.alibaba.excel.EasyExcel;
import jakarta.servlet.http.HttpServletResponse;
import java.net.URLEncoder;
import java.io.IOException;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/payroll")
public class PayrollController {

    @Autowired
    private ISysPayrollService sysPayrollService;

    @Autowired
    private ISysNotificationService sysNotificationService;

    @GetMapping("/list")
    @SaCheckPermission("system:payroll:list")
    public Result<List<Map<String, Object>>> list(@RequestParam String month) {
        return Result.success(sysPayrollService.getMonthlyPayroll(month));
    }

    @PostMapping("/generate")
    @SaCheckPermission("system:payroll:add")
    public Result<Boolean> generate(@RequestParam String month) {
        sysPayrollService.generateMonthlyPayroll(month);
        return Result.success(true);
    }

    @PutMapping("/issue/{id}")
    @SaCheckPermission("system:payroll:edit")
    public Result<Boolean> issue(@PathVariable Long id) {
        SysPayroll payroll = sysPayrollService.getById(id);
        if (payroll == null)
            return Result.error("记录不存在");

        payroll.setStatus(1);
        payroll.setPublishTime(new Date());
        boolean success = sysPayrollService.updateById(payroll);

        if (success) {
            sysNotificationService.send(
                    payroll.getUserId(),
                    "工资发放通知",
                    "您 " + payroll.getPayrollMonth() + " 月份的工资已发放，请注意查收。",
                    "success");
        }
        return Result.success(success);
    }

    @GetMapping("/export")
    @SaCheckPermission("system:payroll:export")
    public void export(@RequestParam String month, HttpServletResponse response) throws IOException {
        List<Map<String, Object>> list = sysPayrollService.getMonthlyPayroll(month);
        List<PayrollExportVO> exportList = list.stream().map(m -> {
            PayrollExportVO vo = new PayrollExportVO();
            vo.setEmpCode((String) m.get("empCode"));
            vo.setFullName((String) m.get("fullName"));
            vo.setDeptName((String) m.get("deptName"));
            vo.setMonth((String) m.get("month"));
            vo.setBaseSalary((java.math.BigDecimal) m.get("baseSalary"));
            vo.setPerformanceBonus((java.math.BigDecimal) m.get("performanceBonus"));
            vo.setDeduction((java.math.BigDecimal) m.get("deduction"));
            vo.setActualAmount((java.math.BigDecimal) m.get("actualAmount"));
            vo.setStatus((Integer) m.get("status") == 1 ? "已发放" : "待发放");
            return vo;
        }).collect(Collectors.toList());

        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        String fileName = URLEncoder.encode("薪资明细-" + month, "UTF-8").replaceAll("\\+", "%20");
        response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");

        EasyExcel.write(response.getOutputStream(), PayrollExportVO.class)
                .sheet("明细")
                .doWrite(exportList);
    }

    @GetMapping("/history")
    public Result<List<SysPayroll>> getHistory(@RequestParam Long userId) {
        return Result.success(sysPayrollService
                .list(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SysPayroll>()
                        .eq(SysPayroll::getUserId, userId)
                        .eq(SysPayroll::getStatus, 1) // Only show issued payrolls
                        .orderByDesc(SysPayroll::getPayrollMonth)));
    }
}
