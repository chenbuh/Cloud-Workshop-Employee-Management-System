package com.cloud.employee.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cloud.employee.entity.SysGrowthRecord;
import com.cloud.employee.mapper.SysGrowthMapper;
import com.cloud.employee.service.ISysGrowthService;
import org.springframework.stereotype.Service;

@Service
public class SysGrowthServiceImpl extends ServiceImpl<SysGrowthMapper, SysGrowthRecord> implements ISysGrowthService {
}
