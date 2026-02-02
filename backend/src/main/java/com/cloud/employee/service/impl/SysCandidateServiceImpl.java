package com.cloud.employee.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cloud.employee.entity.SysCandidate;
import com.cloud.employee.mapper.SysCandidateMapper;
import com.cloud.employee.service.ISysCandidateService;
import org.springframework.stereotype.Service;

@Service
public class SysCandidateServiceImpl extends ServiceImpl<SysCandidateMapper, SysCandidate>
        implements ISysCandidateService {
}
