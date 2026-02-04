package com.cloud.employee.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cloud.employee.entity.EmpProfile;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cloud.employee.vo.EmpProfileVO;
import org.apache.ibatis.annotations.Param;

public interface EmpProfileMapper extends BaseMapper<EmpProfile> {

        @Select("SELECT d.dept_name, COUNT(e.id) as count FROM sys_dept d LEFT JOIN emp_profile e ON d.id = e.dept_id AND e.status != 3 GROUP BY d.dept_name HAVING count > 0")
        List<Map<String, Object>> countByDept();

        @Select("SELECT dept_id as deptId, COUNT(*) as count FROM emp_profile WHERE status != 3 GROUP BY dept_id")
        List<Map<String, Object>> countGroupedByDeptId();

        @Select("<script>" +
                        "SELECT * FROM (" +
                        "  SELECT date, SUM(new_count) OVER (ORDER BY date) as count " +
                        "  FROM (" +
                        "    SELECT DATE_FORMAT(COALESCE(entry_date, create_time), #{dateFormat}) as date, COUNT(*) as new_count "
                        +
                        "    FROM emp_profile " +
                        "    GROUP BY date " +
                        "  ) t " +
                        ") cumulative_t " +
                        "WHERE date &gt;= DATE_FORMAT(DATE_SUB(CURDATE(), INTERVAL #{days} DAY), #{dateFormat}) " +
                        "ORDER BY date ASC" +
                        "</script>")
        List<Map<String, Object>> countTrend(@Param("days") int days, @Param("dateFormat") String dateFormat);

        @Select("<script>" +
                        "SELECT e.*, d.dept_name, p.post_name, l.level_name " +
                        "FROM emp_profile e " +
                        "LEFT JOIN sys_dept d ON e.dept_id = d.id " +
                        "LEFT JOIN sys_post p ON e.post_id = p.post_id " +
                        "LEFT JOIN sys_job_level l ON e.level_id = l.level_id " +
                        "<where>" +
                        "  <if test='keyword != null and keyword != \"\"'>" +
                        "    AND (e.full_name LIKE CONCAT('%', #{keyword}, '%') OR e.emp_code LIKE CONCAT('%', #{keyword}, '%'))"
                        +
                        "  </if>" +
                        "  <if test='deptId != null'>" +
                        "    AND (e.dept_id = #{deptId} OR e.dept_id IN (SELECT id FROM sys_dept WHERE find_in_set(#{deptId}, ancestors)))"
                        +
                        "  </if>" +
                        "</where>" +
                        "ORDER BY e.create_time DESC" +
                        "</script>")
        Page<EmpProfileVO> selectEmployeePage(Page<EmpProfileVO> page,
                        @Param("keyword") String keyword,
                        @Param("deptId") Long deptId);

        @Select("<script>" +
                        "SELECT e.emp_code, e.full_name, d.dept_name, p.post_name, l.level_name, e.job_title, e.entry_date, "
                        +
                        "CASE e.status WHEN 1 THEN '正式' WHEN 2 THEN '试用' WHEN 3 THEN '离职' ELSE '其他' END as status_desc "
                        +
                        "FROM emp_profile e " +
                        "LEFT JOIN sys_dept d ON e.dept_id = d.id " +
                        "LEFT JOIN sys_post p ON e.post_id = p.post_id " +
                        "LEFT JOIN sys_job_level l ON e.level_id = l.level_id " +
                        "<where>" +
                        "  <if test='keyword != null and keyword != \"\"'>" +
                        "    AND (e.full_name LIKE CONCAT('%', #{keyword}, '%') OR e.emp_code LIKE CONCAT('%', #{keyword}, '%'))"
                        +
                        "  </if>" +
                        "  <if test='deptId != null'>" +
                        "    AND (e.dept_id = #{deptId} OR e.dept_id IN (SELECT id FROM sys_dept WHERE find_in_set(#{deptId}, ancestors)))"
                        +
                        "  </if>" +
                        "</where>" +
                        "ORDER BY e.create_time DESC" +
                        "</script>")
        java.util.List<com.cloud.employee.vo.EmployeeExportVO> selectAllForExport(@Param("keyword") String keyword,
                        @Param("deptId") Long deptId);

        @Select("SELECT e.id, e.user_id, e.emp_code, e.full_name, e.dept_id, d.dept_name, e.job_title " +
                        "FROM emp_profile e " +
                        "LEFT JOIN sys_dept d ON e.dept_id = d.id " +
                        "WHERE e.status != 3 " +
                        "ORDER BY e.create_time DESC")
        List<EmpProfileVO> selectAllForDropdown();
}
