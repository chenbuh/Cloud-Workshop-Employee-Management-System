package com.cloud.employee.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cloud.employee.entity.SysLeave;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface SysLeaveMapper extends BaseMapper<SysLeave> {
    @org.apache.ibatis.annotations.Select({
            "<script>",
            "SELECT ",
            "  l.*, ",
            "  e.full_name AS full_name, ",
            "  e.emp_code AS emp_code, ",
            "  d.dept_name AS dept_name ",
            "FROM sys_leave l ",
            "LEFT JOIN emp_profile e ON l.user_id = e.user_id ",
            "LEFT JOIN sys_dept d ON e.dept_id = d.id ",
            "WHERE 1=1 ",
            "<if test='userId != null'>AND l.user_id = #{userId}</if> ",
            "<if test='status != null'>AND l.status = #{status}</if> ",
            "ORDER BY l.create_time DESC",
            "</script>"
    })
    com.baomidou.mybatisplus.core.metadata.IPage<java.util.Map<String, Object>> selectLeavePageWithEmployee(
            com.baomidou.mybatisplus.core.metadata.IPage<java.util.Map<String, Object>> page,
            @org.apache.ibatis.annotations.Param("userId") Long userId,
            @org.apache.ibatis.annotations.Param("status") Integer status);
}
