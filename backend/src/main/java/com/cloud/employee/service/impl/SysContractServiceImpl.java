package com.cloud.employee.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cloud.employee.entity.SysContract;
import com.cloud.employee.mapper.SysContractMapper;
import com.cloud.employee.service.ISysContractService;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Map;

@Service
public class SysContractServiceImpl extends ServiceImpl<SysContractMapper, SysContract> implements ISysContractService {
    @Override
    public List<Map<String, Object>> getContractList() {
        return baseMapper.selectContractList();
    }
}
