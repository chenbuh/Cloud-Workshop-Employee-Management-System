package com.cloud.employee.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cloud.employee.entity.SysRole;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import java.util.List;

@Mapper
public interface SysRoleMapper extends BaseMapper<SysRole> {
    @Select("SELECT role_key FROM sys_role r " +
            "LEFT JOIN sys_user_role ur ON ur.role_id = r.role_id " +
            "WHERE ur.user_id = #{userId}")
    List<String> selectRoleKeysByUserId(Long userId);

    @Select("SELECT menu_id FROM sys_role_menu WHERE role_id = #{roleId}")
    List<Long> selectMenuIdsByRoleId(Long roleId);

    @org.apache.ibatis.annotations.Delete("DELETE FROM sys_role_menu WHERE role_id = #{roleId}")
    void deleteRoleMenus(Long roleId);

    @org.apache.ibatis.annotations.Insert("INSERT INTO sys_role_menu (role_id, menu_id) VALUES (#{roleId}, #{menuId})")
    void insertRoleMenu(@org.apache.ibatis.annotations.Param("roleId") Long roleId,
            @org.apache.ibatis.annotations.Param("menuId") Long menuId);
}
