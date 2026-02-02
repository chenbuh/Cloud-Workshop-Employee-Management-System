package com.cloud.employee.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cloud.employee.entity.SysPerformanceAppraisal;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import java.util.List;
import java.util.Map;

@Mapper
public interface SysPerformanceAppraisalMapper extends BaseMapper<SysPerformanceAppraisal> {

    @Select("SELECT a.*, e.full_name as empName, c.cycle_name as cycleName " +
            "FROM sys_performance_appraisal a " +
            "LEFT JOIN emp_profile e ON a.emp_id = e.id " +
            "LEFT JOIN sys_performance_cycle c ON a.cycle_id = c.id " +
            "WHERE a.cycle_id = #{cycleId}")
    List<Map<String, Object>> selectWithDetails(Long cycleId);
}
