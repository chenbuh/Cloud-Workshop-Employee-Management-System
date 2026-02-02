package com.cloud.employee.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cloud.employee.entity.SysSalary;
import com.cloud.employee.mapper.SysSalaryMapper;
import com.cloud.employee.service.ISysSalaryService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class SysSalaryServiceImpl extends ServiceImpl<SysSalaryMapper, SysSalary> implements ISysSalaryService {

    @Override
    public List<Map<String, Object>> getSalaryList() {
        return baseMapper.selectSalaryList();
    }

    @Override
    public List<Map<String, Object>> getUnconfiguredEmployees() {
        return baseMapper.selectUnconfiguredEmployees();
    }
}
