package com.cloud.employee.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cloud.employee.common.result.Result;
import com.cloud.employee.entity.SysGrowthRecord;
import com.cloud.employee.entity.SysSkill;
import com.cloud.employee.service.ISysGrowthService;
import com.cloud.employee.service.ISysSkillService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/employee-profile")
public class EmployeeProfileController {

    @Autowired
    private ISysSkillService sysSkillService;

    @Autowired
    private ISysGrowthService sysGrowthService;

    @GetMapping("/full/{userId}")
    public Result<Map<String, Object>> getFullProfile(@PathVariable Long userId) {
        Map<String, Object> map = new HashMap<>();

        List<SysSkill> skills = sysSkillService.list(new LambdaQueryWrapper<SysSkill>()
                .eq(SysSkill::getUserId, userId));

        List<SysGrowthRecord> growth = sysGrowthService.list(new LambdaQueryWrapper<SysGrowthRecord>()
                .eq(SysGrowthRecord::getUserId, userId)
                .orderByDesc(SysGrowthRecord::getRecordDate));

        map.put("skills", skills);
        map.put("growth", growth);

        return Result.success(map);
    }

    @PostMapping("/skill")
    public Result<Boolean> addSkill(@RequestBody SysSkill sysSkill) {
        return Result.success(sysSkillService.save(sysSkill));
    }

    @PostMapping("/growth")
    public Result<Boolean> addGrowth(@RequestBody SysGrowthRecord record) {
        return Result.success(sysGrowthService.save(record));
    }
}
