package com.cloud.employee.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cloud.employee.entity.SysPerformanceCycle;
import com.cloud.employee.mapper.SysPerformanceCycleMapper;
import com.cloud.employee.service.ISysPerformanceCycleService;
import org.springframework.stereotype.Service;

@Service
public class SysPerformanceCycleServiceImpl extends ServiceImpl<SysPerformanceCycleMapper, SysPerformanceCycle>
        implements ISysPerformanceCycleService {
}
