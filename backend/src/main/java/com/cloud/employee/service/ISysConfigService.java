package com.cloud.employee.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cloud.employee.entity.SysConfig;
import java.util.Map;

public interface ISysConfigService extends IService<SysConfig> {

    String getValue(String key);

    void setValue(String key, String value);

    Map<String, Object> getAttendanceConfig();

    void updateAttendanceConfig(Map<String, Object> config);
}
