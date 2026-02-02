package com.cloud.employee.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.cloud.employee.common.result.Result;
import com.cloud.employee.service.ISysUserService;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
@CrossOrigin
public class AuthController {

    @Autowired
    private ISysUserService sysUserService;

    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody LoginBody loginBody) {
        try {
            String token = sysUserService.login(loginBody.getUsername(), loginBody.getPassword());
            Map<String, Object> map = new HashMap<>();
            map.put("token", token);
            map.put("tokenInfo", StpUtil.getTokenInfo());
            return Result.success(map);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/logout")
    public Result<String> logout() {
        StpUtil.logout();
        return Result.success("Logout successful");
    }

    @GetMapping("/info")
    public Result<Map<String, Object>> getInfo() {
        if (!StpUtil.isLogin()) {
            return Result.error("Not logged in");
        }

        Long userId = StpUtil.getLoginIdAsLong();
        com.cloud.employee.entity.SysUser user = sysUserService.getById(userId);
        if (user == null) {
            return Result.error("User not found");
        }

        user.setPassword(null); // Safety first

        Map<String, Object> res = new HashMap<>();
        res.put("user", user);
        res.put("permissions", StpUtil.getPermissionList());
        res.put("roles", StpUtil.getRoleList());

        res.put("roles", StpUtil.getRoleList());

        return Result.success(res);
    }

    @PostMapping("/update-password")
    public Result<Boolean> updatePassword(@RequestBody PasswordUpdateBody body) {
        Long userId = StpUtil.getLoginIdAsLong();
        try {
            sysUserService.updatePassword(userId, body.getOldPassword(), body.getNewPassword());
            return Result.success(true);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @Data
    public static class PasswordUpdateBody {
        private String oldPassword;
        private String newPassword;
    }

    @Data
    public static class LoginBody {
        private String username;
        private String password;
    }
}
