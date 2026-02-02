package com.cloud.employee.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cloud.employee.entity.SysPayroll;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface SysPayrollMapper extends BaseMapper<SysPayroll> {

    @Select("SELECT p.*, e.full_name, d.dept_name, e.emp_code " +
            "FROM sys_payroll p " +
            "JOIN emp_profile e ON p.user_id = e.id " +
            "LEFT JOIN sys_dept d ON e.dept_id = d.id " +
            "WHERE p.payroll_month = #{month}")
    List<Map<String, Object>> selectMonthlyPayroll(String month);
}
