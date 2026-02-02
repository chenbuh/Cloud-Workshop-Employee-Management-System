package com.cloud.employee.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cloud.employee.entity.SysAuditLog;
import com.cloud.employee.mapper.SysAuditLogMapper;
import com.cloud.employee.service.ISysAuditLogService;
import org.springframework.stereotype.Service;

@Service
public class SysAuditLogServiceImpl extends ServiceImpl<SysAuditLogMapper, SysAuditLog> implements ISysAuditLogService {
}
