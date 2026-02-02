package com.cloud.employee.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cloud.employee.common.result.Result;
import com.cloud.employee.entity.EmpProfile;
import com.cloud.employee.entity.SysLeave;
import com.cloud.employee.service.IEmpProfileService;
import com.cloud.employee.service.ISysLeaveService;
import com.cloud.employee.entity.SysAnnouncement;
import com.cloud.employee.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/dashboard")
public class DashboardController {

        @Autowired
        private IEmpProfileService empProfileService;

        @Autowired
        private ISysDeptService sysDeptService;

        @Autowired
        private ISysLeaveService sysLeaveService;

        @Autowired
        private ISysAnnouncementService sysAnnouncementService;

        @Autowired
        private ISysPayrollService sysPayrollService;

        @GetMapping("/stats")
        public Result<Map<String, Object>> getDashboardStats() {
                Map<String, Object> stats = new HashMap<>();

                // 1. 总员工数
                long totalEmployees = empProfileService.count(new LambdaQueryWrapper<EmpProfile>()
                                .in(EmpProfile::getStatus, 1, 2));
                stats.put("totalEmployees", totalEmployees);

                // 2. 本月入职
                Date firstDayOfMonth = Date.from(LocalDate.now().with(TemporalAdjusters.firstDayOfMonth())
                                .atStartOfDay(ZoneId.systemDefault()).toInstant());
                long newHires = empProfileService.count(new LambdaQueryWrapper<EmpProfile>()
                                .ge(EmpProfile::getEntryDate, firstDayOfMonth));
                stats.put("newHires", newHires);

                // 3. 待办审批
                long pendingApprovals = sysLeaveService
                                .count(new LambdaQueryWrapper<SysLeave>().eq(SysLeave::getStatus, 0));
                stats.put("pendingApprovals", pendingApprovals);

                // 4. 本月预估支出
                String currentMonth = LocalDate.now().toString().substring(0, 7);
                List<Map<String, Object>> payrolls = sysPayrollService.getMonthlyPayroll(currentMonth);
                double totalPayroll = payrolls.stream()
                                .mapToDouble(m -> ((java.math.BigDecimal) m.get("actualAmount")).doubleValue())
                                .sum();
                stats.put("totalPayroll", totalPayroll);

                return Result.success(stats);
        }

        @GetMapping("/news")
        public Result<List<SysAnnouncement>> getNews() {
                return Result.success(sysAnnouncementService.list(new LambdaQueryWrapper<SysAnnouncement>()
                                .eq(SysAnnouncement::getIsPublished, 1)
                                .orderByDesc(SysAnnouncement::getPublishTime)
                                .last("LIMIT 5")));
        }

        @GetMapping("/upcoming-birthdays")
        public Result<List<Map<String, Object>>> getUpcomingBirthdays() {
                // Fetch all active employees with entry date
                List<EmpProfile> employees = empProfileService.list(new LambdaQueryWrapper<EmpProfile>()
                                .in(EmpProfile::getStatus, 1, 2)
                                .isNotNull(EmpProfile::getEntryDate));

                LocalDate today = LocalDate.now();
                int currentMonth = today.getMonthValue();
                int currentDay = today.getDayOfMonth();
                int nextMonth = today.plusMonths(1).getMonthValue();

                // Filter for work anniversaries in current month (today onwards) or next month
                List<EmpProfile> upcoming = employees.stream()
                                .filter(e -> {
                                        LocalDate entryDay = e.getEntryDate().toInstant().atZone(ZoneId.systemDefault())
                                                        .toLocalDate();
                                        int m = entryDay.getMonthValue();
                                        int d = entryDay.getDayOfMonth();

                                        // Current month remaining days
                                        if (m == currentMonth && d >= currentDay)
                                                return true;
                                        // Next month all days
                                        if (m == nextMonth)
                                                return true;
                                        return false;
                                })
                                .sorted(Comparator.comparingInt(e -> {
                                        LocalDate entryDay = e.getEntryDate().toInstant().atZone(ZoneId.systemDefault())
                                                        .toLocalDate();
                                        int m = entryDay.getMonthValue();
                                        int d = entryDay.getDayOfMonth();
                                        int score = m * 100 + d;
                                        if (score < currentMonth * 100 + currentDay) {
                                                score += 1200; // Push to next year
                                        }
                                        return score;
                                }))
                                .limit(5)
                                .collect(Collectors.toList());

                List<Map<String, Object>> result = new ArrayList<>();
                for (EmpProfile emp : upcoming) {
                        Map<String, Object> map = new HashMap<>();
                        map.put("id", emp.getId());
                        map.put("fullName", emp.getFullName());
                        map.put("entryDate", emp.getEntryDate());
                        map.put("deptName", sysDeptService.getDeptNameById(emp.getDeptId()));
                        result.add(map);
                }

                return Result.success(result);
        }

        @GetMapping("/growth-trend")
        public Result<Map<String, Object>> getGrowthTrend(@RequestParam(defaultValue = "180") int days) {
                List<Map<String, Object>> trend = empProfileService.getTrendStats(days);
                // Transform
                List<String> dates = trend.stream()
                                .map(m -> (String) m.get("date"))
                                .collect(Collectors.toList());
                List<Long> counts = trend.stream()
                                .map(m -> {
                                        Object count = m.get("count");
                                        if (count instanceof Number) {
                                                return ((Number) count).longValue();
                                        }
                                        return 0L;
                                })
                                .collect(Collectors.toList());

                Map<String, Object> res = new HashMap<>();
                res.put("dates", dates);
                res.put("counts", counts);
                return Result.success(res);
        }

        @GetMapping("/dept-distribution")
        public Result<List<Map<String, Object>>> getDeptDistribution() {
                return Result.success(empProfileService.getDeptStats());
        }
}
