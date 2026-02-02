package com.cloud.employee.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cloud.employee.entity.EmpProfile;
import com.cloud.employee.mapper.EmpProfileMapper;
import com.cloud.employee.service.IEmpProfileService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cloud.employee.vo.EmpProfileVO;

@Service
public class EmpProfileServiceImpl extends ServiceImpl<EmpProfileMapper, EmpProfile> implements IEmpProfileService {

    @Override
    public List<Map<String, Object>> getDeptStats() {
        return this.baseMapper.countByDept();
    }

    @Override
    public List<Map<String, Object>> getTrendStats(int days) {
        // If days <= 30 or exactly 30, group by day. Else group by month.
        String dateFormat = (days <= 31) ? "%Y-%m-%d" : "%Y-%m";
        return this.baseMapper.countTrend(days, dateFormat);
    }

    @Override
    public Page<EmpProfileVO> getEmployeePage(Page<EmpProfileVO> page, String keyword, Long deptId) {
        return this.baseMapper.selectEmployeePage(page, keyword, deptId);
    }

    @Override
    public List<com.cloud.employee.vo.EmployeeExportVO> listAllForExport(String keyword, Long deptId) {
        return this.baseMapper.selectAllForExport(keyword, deptId);
    }
}
