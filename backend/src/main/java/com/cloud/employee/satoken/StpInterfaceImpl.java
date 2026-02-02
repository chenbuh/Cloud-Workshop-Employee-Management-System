package com.cloud.employee.satoken;

import cn.dev33.satoken.stp.StpInterface;
import com.cloud.employee.mapper.SysMenuMapper;
import com.cloud.employee.mapper.SysRoleMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 自定义权限加载接口实现类
 */
@Component
public class StpInterfaceImpl implements StpInterface {

    @Autowired
    private SysMenuMapper sysMenuMapper;

    @Autowired
    private SysRoleMapper sysRoleMapper;

    @Autowired
    private com.cloud.employee.mapper.SysUserMapper sysUserMapper;

    /**
     * 返回一个账号所拥有的权限码集合
     */
    @Override
    public List<String> getPermissionList(Object loginId, String loginType) {
        Long userId = Long.valueOf(loginId.toString());
        com.cloud.employee.entity.SysUser user = sysUserMapper.selectById(userId);
        if (user != null && "00".equals(user.getUserType())) {
            return java.util.Collections.singletonList("*");
        }
        return sysMenuMapper.selectPermsByUserId(userId);
    }

    /**
     * 返回一个账号所拥有的角色标识集合 (权限与角色可分开校验)
     */
    @Override
    public List<String> getRoleList(Object loginId, String loginType) {
        Long userId = Long.valueOf(loginId.toString());
        com.cloud.employee.entity.SysUser user = sysUserMapper.selectById(userId);
        if (user != null && "00".equals(user.getUserType())) {
            List<String> list = new java.util.ArrayList<>();
            list.add("admin");
            list.addAll(sysRoleMapper.selectRoleKeysByUserId(userId));
            return list;
        }
        return sysRoleMapper.selectRoleKeysByUserId(userId);
    }

}
