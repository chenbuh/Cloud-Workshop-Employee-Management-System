package com.cloud.employee.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cloud.employee.common.result.Result;
import com.cloud.employee.entity.SysPerformanceCycle;
import com.cloud.employee.entity.SysPerformanceAppraisal;
import com.cloud.employee.service.ISysPerformanceCycleService;
import com.cloud.employee.service.ISysPerformanceAppraisalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/performance")
public class PerformanceController {

    @Autowired
    private ISysPerformanceCycleService cycleService;

    @Autowired
    private ISysPerformanceAppraisalService appraisalService;

    // Cycle Management
    @GetMapping("/cycles")
    public Result<List<SysPerformanceCycle>> listCycles() {
        return Result.success(cycleService.list(new LambdaQueryWrapper<SysPerformanceCycle>()
                .orderByDesc(SysPerformanceCycle::getCreateTime)));
    }

    @PostMapping("/cycles")
    public Result<Boolean> addCycle(@RequestBody SysPerformanceCycle cycle) {
        return Result.success(cycleService.save(cycle));
    }

    // Appraisal Management
    @GetMapping("/appraisals")
    public Result<List<Map<String, Object>>> listAppraisals(@RequestParam Long cycleId) {
        return Result.success(appraisalService.getDetailsByCycle(cycleId));
    }

    @PostMapping("/appraisals")
    public Result<Boolean> submitAppraisal(@RequestBody SysPerformanceAppraisal appraisal) {
        appraisal.setStatus(2); // Set to "evaluated"
        return Result.success(appraisalService.saveOrUpdate(appraisal));
    }

    @GetMapping("/emp/{empId}")
    public Result<List<SysPerformanceAppraisal>> getEmpPerformance(@PathVariable Long empId) {
        return Result.success(appraisalService.list(new LambdaQueryWrapper<SysPerformanceAppraisal>()
                .eq(SysPerformanceAppraisal::getEmpId, empId)
                .orderByDesc(SysPerformanceAppraisal::getCreateTime)));
    }
}
