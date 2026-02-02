package com.cloud.employee.controller;

import com.cloud.employee.common.result.Result;
import com.cloud.employee.entity.SysSalary;
import com.cloud.employee.service.ISysSalaryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/salary")
public class SalaryController {

    @Autowired
    private ISysSalaryService sysSalaryService;

    @GetMapping("/list")
    public Result<List<Map<String, Object>>> list() {
        return Result.success(sysSalaryService.getSalaryList());
    }

    @GetMapping("/unconfigured")
    public Result<List<Map<String, Object>>> unconfigured() {
        return Result.success(sysSalaryService.getUnconfiguredEmployees());
    }

    @PostMapping("/save")
    public Result<Boolean> save(@RequestBody SysSalary sysSalary) {
        return Result.success(sysSalaryService.saveOrUpdate(sysSalary));
    }

    @DeleteMapping("/{id}")
    public Result<Boolean> delete(@PathVariable Long id) {
        return Result.success(sysSalaryService.removeById(id));
    }
}
