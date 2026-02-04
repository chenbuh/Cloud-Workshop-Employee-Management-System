package com.cloud.employee.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cloud.employee.entity.SysResource;
import com.cloud.employee.mapper.SysResourceMapper;
import com.cloud.employee.service.ISysResourceService;
import org.springframework.stereotype.Service;

@Service
public class SysResourceServiceImpl extends ServiceImpl<SysResourceMapper, SysResource> implements ISysResourceService {
}
