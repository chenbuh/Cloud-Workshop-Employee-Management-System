package com.cloud.employee.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cloud.employee.entity.SysLeave;

public interface ISysLeaveService extends IService<SysLeave> {
    com.baomidou.mybatisplus.core.metadata.IPage<java.util.Map<String, Object>> getLeavePageWithEmployee(
            int pageNum, int pageSize, Long userId, Integer status);
}
