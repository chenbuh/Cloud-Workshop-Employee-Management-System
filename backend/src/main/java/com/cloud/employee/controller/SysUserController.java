package com.cloud.employee.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cloud.employee.annotation.Log;
import com.cloud.employee.common.result.Result;
import com.cloud.employee.entity.SysUser;
import com.cloud.employee.service.ISysUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/user")
public class SysUserController {

    @Autowired
    private ISysUserService sysUserService;

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @GetMapping("/list")
    @SaCheckPermission("system:user:list")
    public Result<List<SysUser>> list() {
        return Result.success(sysUserService.list());
    }

    @Log(title = "新增用户", businessType = 1)
    @PostMapping("/create")
    @SaCheckPermission("system:user:add")
    public Result<Boolean> create(@RequestBody SysUser user) {
        // Check if username exists
        long count = sysUserService.count(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUserName, user.getUserName()));
        if (count > 0) {
            return Result.error("用户名已存在");
        }

        // Hash password
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setStatus("0"); // Normal
        user.setDelFlag("0");
        return Result.success(sysUserService.createUser(user));
    }

    @Log(title = "删除用户", businessType = 3)
    @DeleteMapping("/{id}")
    @SaCheckPermission("system:user:remove")
    public Result<Boolean> remove(@PathVariable Long id) {
        if (id == 1L) {
            return Result.error("管理员账号不允许删除");
        }
        return Result.success(sysUserService.removeById(id));
    }

    @Log(title = "更新个人资料", businessType = 2)
    @PostMapping("/updateProfile")
    public Result<Boolean> updateProfile(@RequestBody SysUser user) {
        Long userId = StpUtil.getLoginIdAsLong();
        user.setId(userId);
        // 不允许通过此接口修改这些字段
        user.setUserName(null);
        user.setPassword(null);
        user.setDelFlag(null);
        user.setStatus(null);

        return Result.success(sysUserService.updateById(user));
    }

    @GetMapping("/menu-ids/{userId}")
    @SaCheckPermission("system:user:query")
    public Result<List<Long>> getUserMenuIds(@PathVariable Long userId) {
        return Result.success(sysUserService.getUserMenuIds(userId));
    }

    @Log(title = "分配权限", businessType = 2)
    @PostMapping("/assign-menus")
    @SaCheckPermission("system:user:edit")
    public Result<Boolean> assignMenus(@RequestBody UserMenuBody body) {
        return Result.success(sysUserService.updateUserMenus(body.getUserId(), body.getMenuIds()));
    }

    @lombok.Data
    public static class UserMenuBody {
        private Long userId;
        private List<Long> menuIds;
    }
}
