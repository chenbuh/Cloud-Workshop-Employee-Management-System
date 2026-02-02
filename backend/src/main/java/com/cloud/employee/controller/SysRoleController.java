package com.cloud.employee.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.cloud.employee.common.result.Result;
import com.cloud.employee.entity.SysRole;
import com.cloud.employee.service.ISysRoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/role")
public class SysRoleController {

    @Autowired
    private ISysRoleService sysRoleService;

    @GetMapping("/list")
    @SaCheckPermission("system:role:list")
    public Result<List<SysRole>> list() {
        return Result.success(sysRoleService.list());
    }

    @PostMapping
    @SaCheckPermission("system:role:add")
    public Result<Boolean> add(@RequestBody SysRole role) {
        return Result.success(sysRoleService.save(role));
    }

    @DeleteMapping("/{id}")
    @SaCheckPermission("system:role:remove")
    public Result<Boolean> remove(@PathVariable Long id) {
        return Result.success(sysRoleService.removeById(id));
    }

    @GetMapping("/menu-ids/{roleId}")
    @SaCheckPermission("system:role:query")
    public Result<List<Long>> getMenuIds(@PathVariable Long roleId) {
        return Result.success(sysRoleService.getRoleMenuIds(roleId));
    }

    @PostMapping("/assign-menus")
    @SaCheckPermission("system:role:edit")
    public Result<Boolean> updateMenus(@RequestBody RoleMenuBody body) {
        return Result.success(sysRoleService.updateRoleMenus(body.getRoleId(), body.getMenuIds()));
    }

    @lombok.Data
    public static class RoleMenuBody {
        private Long roleId;
        private List<Long> menuIds;
    }
}
