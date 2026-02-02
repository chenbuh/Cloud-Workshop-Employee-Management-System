package com.cloud.employee.controller;

import com.cloud.employee.common.result.Result;
import com.cloud.employee.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.*;

@RestController
@RequestMapping("/api/v1/analytics")
public class AnalyticsController {

    @Autowired
    private IEmpProfileService empProfileService;

    @Autowired
    private ISysPayrollService sysPayrollService;

    @Autowired
    private ISysDeptService deptService;

    @GetMapping("/cost-efficiency")
    public Result<Map<String, Object>> getCostEfficiency() {
        Map<String, Object> data = new HashMap<>();

        // 1. Dept Salary Distribution
        List<Map<String, Object>> deptStats = deptService.list().stream().map(dept -> {
            String month = LocalDate.now().toString().substring(0, 7);
            double totalSalary = sysPayrollService.list().stream()
                    .filter(p -> p.getPayrollMonth().equals(month))
                    // Simple filter by emp -> dept mapping elsewhere, for mock we summarize
                    .mapToDouble(p -> p.getActualAmount().doubleValue())
                    .sum();

            Map<String, Object> m = new HashMap<>();
            m.put("deptName", dept.getDeptName());
            m.put("totalCost", totalSalary * (0.1 + Math.random() * 0.3)); // Simulation
            m.put("headcount", empProfileService.count());
            return m;
        }).toList();
        data.put("deptCost", deptStats);

        // 2. Performance vs Salary (Return high-level correlation)
        // [A: 20%, B: 50%, C: 20%, D: 10%] simulation
        data.put("perfDistribution", List.of(
                Map.of("name", "A (卓越)", "value", 15),
                Map.of("name", "B (优秀)", "value", 45),
                Map.of("name", "C (良好)", "value", 30),
                Map.of("name", "D (需改进)", "value", 10)));

        // 3. Real Stats
        long totalEmp = empProfileService.count();
        long resignedEmp = empProfileService.count(
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<com.cloud.employee.entity.EmpProfile>()
                        .eq("status", 3));

        double turnoverRate = totalEmp > 0 ? (double) resignedEmp / totalEmp : 0.05;

        // Mock ROI based on dept efficiency
        data.put("roi", 4.3 + Math.random());
        data.put("turnoverRate", turnoverRate * 100); // Percentage
        data.put("retentionRate", 95 + Math.random() * 4); // Simulated core retention

        return Result.success(data);
    }
}
