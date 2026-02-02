package com.cloud.employee.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cloud.employee.entity.SysSalary;
import java.util.List;
import java.util.Map;

public interface ISysSalaryService extends IService<SysSalary> {
    List<Map<String, Object>> getSalaryList();

    List<Map<String, Object>> getUnconfiguredEmployees();
}
