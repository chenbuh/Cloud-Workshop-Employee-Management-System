package com.cloud.employee.controller;

import com.cloud.employee.common.result.Result;
import com.cloud.employee.entity.SysDept;
import com.cloud.employee.service.ISysDeptService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/dept")
public class DeptController {

    @Autowired
    private ISysDeptService sysDeptService;

    // 获取部门树
    @GetMapping("/tree")
    public Result<List<SysDept>> tree() {
        return Result.success(sysDeptService.selectDeptTree());
    }

    // 获取列表
    @GetMapping("/list")
    public Result<List<SysDept>> list() {
        return Result.success(sysDeptService.list());
    }

    // 获取详情
    @GetMapping("/{id:\\d+}")
    public Result<SysDept> getInfo(@PathVariable Long id) {
        return Result.success(sysDeptService.getById(id));
    }

    // 新增部门
    @PostMapping
    public Result<Boolean> add(@RequestBody SysDept dept) {
        if (dept.getParentId() == null) {
            dept.setParentId(0L);
        }
        return Result.success(sysDeptService.save(dept));
    }

    // 修改部门
    @PutMapping
    public Result<Boolean> edit(@RequestBody SysDept dept) {
        return Result.success(sysDeptService.updateById(dept));
    }

    // 删除部门
    @DeleteMapping("/{id}")
    public Result<Boolean> remove(@PathVariable Long id) {
        if (sysDeptService.hasChildren(id)) {
            return Result.error("存在下级部门，不允许删除");
        }
        // TODO: Check if users exist in dept
        return Result.success(sysDeptService.removeById(id));
    }
}
