package com.cloud.employee.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cloud.employee.entity.SysInterview;
import com.cloud.employee.mapper.SysInterviewMapper;
import com.cloud.employee.service.ISysInterviewService;
import org.springframework.stereotype.Service;

@Service
public class SysInterviewServiceImpl extends ServiceImpl<SysInterviewMapper, SysInterview>
        implements ISysInterviewService {
}
