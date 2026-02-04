package com.cloud.employee.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cloud.employee.entity.RecJob;
import com.cloud.employee.mapper.RecJobMapper;
import com.cloud.employee.service.IRecJobService;
import org.springframework.stereotype.Service;

@Service
public class RecJobServiceImpl extends ServiceImpl<RecJobMapper, RecJob> implements IRecJobService {
}
