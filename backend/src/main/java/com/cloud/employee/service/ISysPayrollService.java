package com.cloud.employee.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cloud.employee.entity.SysPayroll;
import java.util.List;
import java.util.Map;

public interface ISysPayrollService extends IService<SysPayroll> {
    void generateMonthlyPayroll(String month);

    List<Map<String, Object>> getMonthlyPayroll(String month);

    void batchSendSalarySlips(List<Long> ids);
}
