package com.cloud.employee.controller;

import com.cloud.employee.common.result.Result;
import com.cloud.employee.entity.SysMenu;
import com.cloud.employee.service.ISysMenuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/menu")
public class SysMenuController {

    @Autowired
    private ISysMenuService sysMenuService;

    @GetMapping("/tree")
    public Result<List<SysMenu>> tree() {
        return Result.success(sysMenuService.selectMenuTree());
    }

    @GetMapping("/list")
    public Result<List<SysMenu>> list() {
        return Result.success(sysMenuService.list());
    }
}
