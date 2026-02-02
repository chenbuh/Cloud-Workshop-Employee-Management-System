package com.cloud.employee.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cloud.employee.common.result.Result;
import com.cloud.employee.entity.SysJobLevel;
import com.cloud.employee.service.ISysJobLevelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/system/job-level")
public class SysJobLevelController {

    @Autowired
    private ISysJobLevelService jobLevelService;

    @GetMapping("/list")
    @SaCheckPermission("system:jobLevel:list")
    public Result<Page<SysJobLevel>> list(Page<SysJobLevel> page, String levelName, String status) {
        LambdaQueryWrapper<SysJobLevel> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(levelName != null, SysJobLevel::getLevelName, levelName);
        queryWrapper.eq(status != null, SysJobLevel::getStatus, status);
        queryWrapper.orderByAsc(SysJobLevel::getLevelSort);
        return Result.success(jobLevelService.page(page, queryWrapper));
    }

    @GetMapping("/all")
    public Result<List<SysJobLevel>> all() {
        return Result.success(jobLevelService.list(new LambdaQueryWrapper<SysJobLevel>().eq(SysJobLevel::getStatus, "0")
                .orderByAsc(SysJobLevel::getLevelSort)));
    }

    @GetMapping("/{levelId}")
    @SaCheckPermission("system:jobLevel:query")
    public Result<SysJobLevel> getInfo(@PathVariable Long levelId) {
        return Result.success(jobLevelService.getById(levelId));
    }

    @PostMapping
    @SaCheckPermission("system:jobLevel:add")
    public Result<Boolean> add(@RequestBody SysJobLevel jobLevel) {
        return Result.success(jobLevelService.save(jobLevel));
    }

    @PutMapping
    @SaCheckPermission("system:jobLevel:edit")
    public Result<Boolean> edit(@RequestBody SysJobLevel jobLevel) {
        return Result.success(jobLevelService.updateById(jobLevel));
    }

    @DeleteMapping("/{levelIds}")
    @SaCheckPermission("system:jobLevel:remove")
    public Result<Boolean> remove(@PathVariable List<Long> levelIds) {
        return Result.success(jobLevelService.removeByIds(levelIds));
    }
}
