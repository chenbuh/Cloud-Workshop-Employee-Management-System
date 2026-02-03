package com.cloud.employee.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.cloud.employee.entity.EmpProfile;
import com.cloud.employee.vo.EmpProfileVO;

import java.util.List;
import java.util.Map;

public interface IEmpProfileService extends IService<EmpProfile> {
    List<Map<String, Object>> getDeptStats();

    List<Map<String, Object>> getTrendStats(int days);

    Page<EmpProfileVO> getEmployeePage(Page<EmpProfileVO> page, String keyword, Long deptId);

    java.util.List<com.cloud.employee.vo.EmployeeExportVO> listAllForExport(String keyword, Long deptId);

    List<EmpProfileVO> getEmployeeListForDropdown();
}
