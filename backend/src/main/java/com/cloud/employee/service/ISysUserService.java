package com.cloud.employee.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cloud.employee.entity.SysUser;

public interface ISysUserService extends IService<SysUser> {
    String login(String username, String password);

    boolean createUser(SysUser user);

    java.util.List<Long> getUserMenuIds(Long userId);

    boolean updateUserMenus(Long userId, java.util.List<Long> menuIds);

    void updatePassword(Long userId, String oldPassword, String newPassword);
}
