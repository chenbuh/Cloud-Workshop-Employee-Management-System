package com.cloud.employee.controller;

import com.cloud.employee.common.result.Result;
import com.cloud.employee.service.ISysConfigService;
import cn.dev33.satoken.annotation.SaCheckPermission;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/config")
public class SysConfigController {

    @Autowired
    private ISysConfigService sysConfigService;

    @GetMapping("/attendance")
    @SaCheckPermission("system:config:list")
    public Result<Map<String, Object>> getAttendanceConfig() {
        return Result.success(sysConfigService.getAttendanceConfig());
    }

    @PutMapping("/attendance")
    @SaCheckPermission("system:config:edit")
    public Result<Boolean> updateAttendanceConfig(@RequestBody Map<String, Object> config) {
        sysConfigService.updateAttendanceConfig(config);
        return Result.success(true);
    }
}
