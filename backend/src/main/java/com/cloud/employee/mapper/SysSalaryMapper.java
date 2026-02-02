package com.cloud.employee.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cloud.employee.entity.SysSalary;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface SysSalaryMapper extends BaseMapper<SysSalary> {

    @Select("SELECT s.*, e.full_name, d.dept_name " +
            "FROM sys_salary s " +
            "JOIN emp_profile e ON s.user_id = e.id " +
            "LEFT JOIN sys_dept d ON e.dept_id = d.id")
    List<Map<String, Object>> selectSalaryList();

    @Select("SELECT e.id as userId, e.full_name, d.dept_name " +
            "FROM emp_profile e " +
            "LEFT JOIN sys_dept d ON e.dept_id = d.id " +
            "WHERE e.id NOT IN (SELECT user_id FROM sys_salary)")
    List<Map<String, Object>> selectUnconfiguredEmployees();
}
