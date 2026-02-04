package com.cloud.employee.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cloud.employee.entity.SysContract;
import java.util.List;
import java.util.Map;

public interface ISysContractService extends IService<SysContract> {
    List<Map<String, Object>> getContractList();
}
