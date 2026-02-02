package com.cloud.employee.service.impl;

import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cloud.employee.entity.SysAttendance;
import com.cloud.employee.entity.SysPayroll;
import com.cloud.employee.entity.SysSalary;
import com.cloud.employee.mapper.SysPayrollMapper;
import com.cloud.employee.service.ISysAttendanceService;
import com.cloud.employee.service.ISysPayrollService;
import com.cloud.employee.service.ISysSalaryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Service
public class SysPayrollServiceImpl extends ServiceImpl<SysPayrollMapper, SysPayroll> implements ISysPayrollService {

    @Autowired
    private ISysSalaryService sysSalaryService;

    @Autowired
    private ISysAttendanceService sysAttendanceService;

    @Override
    @Transactional
    public void generateMonthlyPayroll(String month) {
        // 1. 获取所有已配置薪资的员工
        List<SysSalary> salaryConfigs = sysSalaryService.list();

        // 确定月份起止时间
        Date startDate = DateUtil.parse(month + "-01");
        Date endDate = DateUtil.endOfMonth(startDate);

        for (SysSalary config : salaryConfigs) {
            // 2. 检查该月份是否已存在
            long count = this.count(new LambdaQueryWrapper<SysPayroll>()
                    .eq(SysPayroll::getUserId, config.getUserId())
                    .eq(SysPayroll::getPayrollMonth, month));

            if (count > 0)
                continue;

            // 3. 计算考勤扣款
            // 迟到 (status=2)
            long lateCount = sysAttendanceService.count(new LambdaQueryWrapper<SysAttendance>()
                    .eq(SysAttendance::getUserId, config.getUserId())
                    .between(SysAttendance::getWorkDate, startDate, endDate)
                    .eq(SysAttendance::getStatus, 2));

            // 早退 (status=3)
            long earlyCount = sysAttendanceService.count(new LambdaQueryWrapper<SysAttendance>()
                    .eq(SysAttendance::getUserId, config.getUserId())
                    .between(SysAttendance::getWorkDate, startDate, endDate)
                    .eq(SysAttendance::getStatus, 3));

            // 缺勤 (status=0)
            long absentCount = sysAttendanceService.count(new LambdaQueryWrapper<SysAttendance>()
                    .eq(SysAttendance::getUserId, config.getUserId())
                    .between(SysAttendance::getWorkDate, startDate, endDate)
                    .eq(SysAttendance::getStatus, 0));

            // 扣款规则: 迟到/早退每次50，缺勤每次200
            BigDecimal deduction = BigDecimal.valueOf((lateCount + earlyCount) * 50 + absentCount * 200);

            // 4. 创建工资条
            SysPayroll payroll = new SysPayroll();
            payroll.setUserId(config.getUserId());
            payroll.setPayrollMonth(month);
            payroll.setBaseSalary(config.getBaseSalary());
            payroll.setSubsidy(config.getSubsidy());
            payroll.setBonus(config.getBonus());
            payroll.setInsurance(config.getInsurance());
            payroll.setDeduction(deduction);

            // 计算实发
            BigDecimal actual = config.getBaseSalary()
                    .add(config.getSubsidy())
                    .add(config.getBonus())
                    .subtract(config.getInsurance())
                    .subtract(deduction);

            // 确保实发不为负数
            if (actual.compareTo(BigDecimal.ZERO) < 0) {
                actual = BigDecimal.ZERO;
            }

            payroll.setActualAmount(actual);
            payroll.setStatus(0); // 待发放
            payroll.setCreateTime(new Date());

            this.save(payroll);
        }
    }

    @Override
    public List<Map<String, Object>> getMonthlyPayroll(String month) {
        return baseMapper.selectMonthlyPayroll(month);
    }
}
