package com.cloud.employee.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cloud.employee.entity.SysContract;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import java.util.List;
import java.util.Map;

@Mapper
public interface SysContractMapper extends BaseMapper<SysContract> {
    @Select("SELECT c.*, p.full_name as employeeName, p.emp_code as employeeCode " +
            "FROM sys_contract c " +
            "LEFT JOIN emp_profile p ON c.employee_id = p.id " +
            "ORDER BY c.create_time DESC")
    List<Map<String, Object>> selectContractList();
}
