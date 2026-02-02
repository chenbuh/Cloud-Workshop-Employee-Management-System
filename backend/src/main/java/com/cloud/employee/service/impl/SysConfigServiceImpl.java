package com.cloud.employee.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cloud.employee.entity.SysConfig;
import com.cloud.employee.mapper.SysConfigMapper;
import com.cloud.employee.service.ISysConfigService;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class SysConfigServiceImpl extends ServiceImpl<SysConfigMapper, SysConfig> implements ISysConfigService {

    @Override
    @Cacheable(value = "sys_config", key = "#key")
    public String getValue(String key) {
        SysConfig config = this.getOne(new LambdaQueryWrapper<SysConfig>().eq(SysConfig::getConfigKey, key));
        return config != null ? config.getConfigValue() : null;
    }

    @Override
    @CacheEvict(value = "sys_config", key = "#key")
    public void setValue(String key, String value) {
        SysConfig config = this.getOne(new LambdaQueryWrapper<SysConfig>().eq(SysConfig::getConfigKey, key));
        if (config != null) {
            config.setConfigValue(value);
            this.updateById(config);
        } else {
            config = new SysConfig();
            config.setConfigKey(key);
            config.setConfigValue(value);
            config.setConfigType("N"); // Default not system built-in if created via API
            this.save(config);
        }
    }

    @Override
    public Map<String, Object> getAttendanceConfig() {
        Map<String, Object> config = new HashMap<>();

        String workDaysStr = getValue("sys.attendance.workDays");
        // Default Mon-Fri
        if (workDaysStr == null)
            workDaysStr = "1,2,3,4,5";

        List<Integer> workDays = Arrays.stream(workDaysStr.split(","))
                .filter(s -> !s.isEmpty())
                .map(Integer::parseInt)
                .collect(Collectors.toList());

        String startTime = getValue("sys.attendance.startTime");
        if (startTime == null)
            startTime = "09:00";

        String endTime = getValue("sys.attendance.endTime");
        if (endTime == null)
            endTime = "18:00";

        config.put("workDays", workDays);
        config.put("startTime", startTime);
        config.put("endTime", endTime);

        return config;
    }

    @Override
    @Transactional
    public void updateAttendanceConfig(Map<String, Object> config) {
        if (config.containsKey("workDays")) {
            Object workDaysObj = config.get("workDays");
            if (workDaysObj instanceof List) {
                List<?> days = (List<?>) workDaysObj;
                String daysStr = days.stream().map(String::valueOf).collect(Collectors.joining(","));
                setValue("sys.attendance.workDays", daysStr);
            }
        }

        if (config.containsKey("startTime")) {
            setValue("sys.attendance.startTime", (String) config.get("startTime"));
        }

        if (config.containsKey("endTime")) {
            setValue("sys.attendance.endTime", (String) config.get("endTime"));
        }
    }
}
