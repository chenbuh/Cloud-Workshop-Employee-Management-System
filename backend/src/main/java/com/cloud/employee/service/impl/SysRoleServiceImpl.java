package com.cloud.employee.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cloud.employee.entity.SysRole;
import com.cloud.employee.mapper.SysRoleMapper;
import com.cloud.employee.service.ISysRoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SysRoleServiceImpl extends ServiceImpl<SysRoleMapper, SysRole> implements ISysRoleService {

    @Autowired
    private SysRoleMapper sysRoleMapper;

    @Override
    public List<Long> getRoleMenuIds(Long roleId) {
        return sysRoleMapper.selectMenuIdsByRoleId(roleId);
    }

    @Override
    @Transactional
    public boolean updateRoleMenus(Long roleId, List<Long> menuIds) {
        // 1. Delete existing
        sysRoleMapper.deleteRoleMenus(roleId);
        // 2. Insert new
        if (menuIds != null && !menuIds.isEmpty()) {
            for (Long menuId : menuIds) {
                sysRoleMapper.insertRoleMenu(roleId, menuId);
            }
        }
        return true;
    }
}
