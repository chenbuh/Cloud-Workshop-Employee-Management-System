package com.cloud.employee.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cloud.employee.entity.SysAttendance;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface SysAttendanceMapper extends BaseMapper<SysAttendance> {
        @org.apache.ibatis.annotations.Select({
                        "<script>",
                        "SELECT ",
                        "  a.id, a.user_id, a.work_date, ",
                        "  a.clock_in_time, a.clock_out_time, ",
                        "  a.status, a.ip_address, ",
                        "  e.full_name AS full_name, ",
                        "  e.emp_code AS emp_code, ",
                        "  d.dept_name AS dept_name ",
                        "FROM sys_attendance a ",
                        "LEFT JOIN emp_profile e ON a.user_id = e.user_id ",
                        "LEFT JOIN sys_dept d ON e.dept_id = d.id ",
                        "WHERE 1=1 ",
                        "<if test='startDate != null'>AND a.work_date &gt;= #{startDate}</if> ",
                        "<if test='endDate != null'>AND a.work_date &lt;= #{endDate}</if> ",
                        "<if test='userId != null'>AND a.user_id = #{userId}</if> ",
                        "ORDER BY a.work_date DESC, a.clock_in_time DESC",
                        "</script>"
        })
        java.util.List<java.util.Map<String, Object>> selectAllWithEmployee(
                        @org.apache.ibatis.annotations.Param("startDate") String startDate,
                        @org.apache.ibatis.annotations.Param("endDate") String endDate,
                        @org.apache.ibatis.annotations.Param("userId") Long userId);
}
