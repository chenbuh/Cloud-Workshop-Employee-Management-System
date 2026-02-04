package com.cloud.employee.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cloud.employee.entity.SysFlowInstance;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import java.util.List;
import java.util.Map;

@Mapper
public interface SysFlowInstanceMapper extends BaseMapper<SysFlowInstance> {
    @Select("SELECT i.*, p.full_name as employeeName, t.name as templateName " +
            "FROM sys_flow_instance i " +
            "LEFT JOIN emp_profile p ON i.employee_id = p.id " +
            "LEFT JOIN sys_flow_template t ON i.template_id = t.id " +
            "ORDER BY i.start_time DESC")
    List<Map<String, Object>> selectInstanceList();
}
