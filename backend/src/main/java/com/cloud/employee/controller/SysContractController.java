package com.cloud.employee.controller;

import com.cloud.employee.common.result.Result;
import com.cloud.employee.entity.SysContract;
import com.cloud.employee.service.ISysContractService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/contract")
public class SysContractController {

    @Autowired
    private ISysContractService contractService;

    @GetMapping("/list")
    public Result<List<Map<String, Object>>> list() {
        return Result.success(contractService.getContractList());
    }

    @GetMapping("/{id}")
    public Result<SysContract> getById(@PathVariable Long id) {
        return Result.success(contractService.getById(id));
    }

    @PostMapping("/save")
    public Result<String> save(@RequestBody SysContract contract) {
        contractService.saveOrUpdate(contract);
        return Result.success("保存成功");
    }

    @DeleteMapping("/{id}")
    public Result<String> delete(@PathVariable Long id) {
        contractService.removeById(id);
        return Result.success("删除成功");
    }
}
