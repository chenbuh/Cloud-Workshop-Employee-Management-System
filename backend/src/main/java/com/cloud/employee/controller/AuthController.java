package com.cloud.employee.controller;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.codec.Base32;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.crypto.digest.otp.TOTP;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cloud.employee.common.result.Result;
import com.cloud.employee.entity.SysAuditLog;
import com.cloud.employee.entity.SysUser;
import com.cloud.employee.service.ISysAuditLogService;
import com.cloud.employee.service.ISysUserService;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.time.Instant;

@RestController
@RequestMapping("/api/v1/auth")
@CrossOrigin
public class AuthController {

    @Autowired
    private ISysUserService sysUserService;

    @Autowired
    private ISysAuditLogService auditLogService;

    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody LoginBody loginBody) {
        try {
            Map<String, Object> loginResult = sysUserService.login(loginBody.getUsername(), loginBody.getPassword());
            return Result.success(loginResult);
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
        SysUser user = sysUserService.getById(userId);
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

    @GetMapping("/security-logs")
    public Result<List<SysAuditLog>> getSecurityLogs() {
        Long userId = StpUtil.getLoginIdAsLong();
        return Result.success(auditLogService.list(new LambdaQueryWrapper<SysAuditLog>()
                .eq(SysAuditLog::getOperatorId, userId)
                .orderByDesc(SysAuditLog::getOperTime)
                .last("limit 10")));
    }

    @GetMapping("/sessions")
    public Result<List<Map<String, Object>>> getSessions() {
        Long userId = StpUtil.getLoginIdAsLong();
        List<String> tokenList = StpUtil.getTokenValueListByLoginId(userId);
        List<Map<String, Object>> sessions = new ArrayList<>();
        for (String token : tokenList) {
            Map<String, Object> session = new HashMap<>();
            session.put("token", token);
            session.put("isCurrent", token.equals(StpUtil.getTokenValue()));
            // In a real scenario, you might store IP/UA in custom session data
            sessions.add(session);
        }
        return Result.success(sessions);
    }

    @PostMapping("/kickout")
    public Result<Boolean> kickout(@RequestParam String token) {
        StpUtil.kickoutByTokenValue(token);
        return Result.success(true);
    }

    // --- MFA (Two-Step Verification) ---

    @GetMapping("/mfa/setup")
    public Result<Map<String, String>> setupMfa() {
        Long userId = StpUtil.getLoginIdAsLong();
        SysUser user = sysUserService.getById(userId);

        // Generate a 16-character base32 secret (standard for Google Authenticator)
        String secret = Base32.encode(RandomUtil.randomBytes(10));
        String label = user.getUserName();
        String issuer = "CloudWorkshop";
        String otpAuthUrl = String.format("otpauth://totp/%s:%s?secret=%s&issuer=%s", issuer, label, secret, issuer);

        Map<String, String> map = new HashMap<>();
        map.put("secret", secret);
        map.put("qrcodeUrl", otpAuthUrl); // 前端使用此 URL 生成二维码
        return Result.success(map);
    }

    @PostMapping("/mfa/enable")
    public Result<Boolean> enableMfa(@RequestBody Map<String, String> body) {
        Long userId = StpUtil.getLoginIdAsLong();
        String secret = body.get("secret");
        String codeStr = body.get("code");

        TOTP totp = new TOTP(Base32.decode(secret));
        if (totp.validate(Instant.now(), 1, Integer.parseInt(codeStr))) {
            SysUser user = sysUserService.getById(userId);
            user.setMfaSecret(secret);
            user.setMfaEnabled(1);
            sysUserService.updateById(user);
            return Result.success(true);
        } else {
            return Result.error("验证码错误");
        }
    }

    @PostMapping("/mfa/login")
    public Result<Map<String, Object>> mfaLogin(@RequestBody Map<String, Object> body) {
        Long userId = Long.parseLong(body.get("userId").toString());
        String codeStr = body.get("code").toString();

        SysUser user = sysUserService.getById(userId);
        if (user == null || user.getMfaSecret() == null) {
            return Result.error("非法请求");
        }

        TOTP totp = new TOTP(Base32.decode(user.getMfaSecret()));
        if (totp.validate(Instant.now(), 1, Integer.parseInt(codeStr))) {
            StpUtil.login(userId);
            Map<String, Object> result = new HashMap<>();
            result.put("token", StpUtil.getTokenValue());
            return Result.success(result);
        } else {
            return Result.error("验证码错误");
        }
    }

    @PostMapping("/mfa/disable")
    public Result<Boolean> disableMfa(@RequestBody Map<String, String> body) {
        Long userId = StpUtil.getLoginIdAsLong();
        String code = body.get("code");

        SysUser user = sysUserService.getById(userId);
        if (user == null || user.getMfaEnabled() != 1 || user.getMfaSecret() == null) {
            return Result.error("MFA 未开启");
        }

        // 验证当前 TOTP 验证码，确保是本人操作
        TOTP totp = new TOTP(Base32.decode(user.getMfaSecret()));
        if (!totp.validate(Instant.now(), 1, Integer.parseInt(code))) {
            return Result.error("验证码错误");
        }

        // 清除 MFA 设置
        user.setMfaSecret(null);
        user.setMfaEnabled(0);
        sysUserService.updateById(user);

        return Result.success(true);
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
