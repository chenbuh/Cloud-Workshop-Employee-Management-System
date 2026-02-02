package com.cloud.employee.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cloud.employee.entity.SysLeave;
import com.cloud.employee.mapper.SysLeaveMapper;
import com.cloud.employee.service.ISysLeaveService;
import org.springframework.stereotype.Service;

@Service
public class SysLeaveServiceImpl extends ServiceImpl<SysLeaveMapper, SysLeave> implements ISysLeaveService {

    @org.springframework.beans.factory.annotation.Autowired
    private SysLeaveMapper sysLeaveMapper;

    @Override
    public com.baomidou.mybatisplus.core.metadata.IPage<java.util.Map<String, Object>> getLeavePageWithEmployee(
            int pageNum, int pageSize, Long userId, Integer status) {
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<java.util.Map<String, Object>> page = new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(
                pageNum, pageSize);
        return sysLeaveMapper.selectLeavePageWithEmployee(page, userId, status);
    }
}
