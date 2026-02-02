package com.cloud.employee.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cloud.employee.entity.SysPerformanceAppraisal;
import java.util.List;
import java.util.Map;

public interface ISysPerformanceAppraisalService extends IService<SysPerformanceAppraisal> {
    List<Map<String, Object>> getDetailsByCycle(Long cycleId);
}
