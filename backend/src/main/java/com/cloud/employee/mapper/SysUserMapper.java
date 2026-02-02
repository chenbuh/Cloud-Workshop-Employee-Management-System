package com.cloud.employee.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cloud.employee.entity.SysUser;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface SysUserMapper extends BaseMapper<SysUser> {
    @org.apache.ibatis.annotations.Insert("INSERT INTO sys_user_role (user_id, role_id) VALUES (#{userId}, #{roleId})")
    void insertUserRole(@org.apache.ibatis.annotations.Param("userId") Long userId,
            @org.apache.ibatis.annotations.Param("roleId") Long roleId);

    @org.apache.ibatis.annotations.Select("SELECT menu_id FROM sys_user_menu WHERE user_id = #{userId}")
    java.util.List<Long> selectMenuIdsByUserId(Long userId);

    @org.apache.ibatis.annotations.Delete("DELETE FROM sys_user_menu WHERE user_id = #{userId}")
    void deleteUserMenus(Long userId);

    @org.apache.ibatis.annotations.Insert("INSERT INTO sys_user_menu (user_id, menu_id) VALUES (#{userId}, #{menuId})")
    void insertUserMenu(@org.apache.ibatis.annotations.Param("userId") Long userId,
            @org.apache.ibatis.annotations.Param("menuId") Long menuId);
}
