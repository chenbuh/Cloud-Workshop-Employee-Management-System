package com.cloud.employee.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cloud.employee.entity.SysRole;

import java.util.List;

public interface ISysRoleService extends IService<SysRole> {
    List<Long> getRoleMenuIds(Long roleId);

    boolean updateRoleMenus(Long roleId, List<Long> menuIds);
}
