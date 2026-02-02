package com.cloud.employee.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cloud.employee.entity.SysPerformanceAppraisal;
import com.cloud.employee.mapper.SysPerformanceAppraisalMapper;
import com.cloud.employee.service.ISysPerformanceAppraisalService;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Map;

@Service
public class SysPerformanceAppraisalServiceImpl extends
        ServiceImpl<SysPerformanceAppraisalMapper, SysPerformanceAppraisal> implements ISysPerformanceAppraisalService {
    @Override
    public List<Map<String, Object>> getDetailsByCycle(Long cycleId) {
        return baseMapper.selectWithDetails(cycleId);
    }
}
