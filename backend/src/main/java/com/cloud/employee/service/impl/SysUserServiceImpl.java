package com.cloud.employee.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cloud.employee.entity.SysUser;
import com.cloud.employee.mapper.SysUserMapper;
import com.cloud.employee.service.ISysUserService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements ISysUserService {

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Override
    public Map<String, Object> login(String username, String password) {
        // 1. Check user exists
        SysUser user = this.getOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUserName, username));

        if (user == null) {
            throw new RuntimeException("User not found: " + username);
        }

        // 2. Check password (BCrypt)
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("Password incorrect");
        }

        Map<String, Object> result = new HashMap<>();

        // 3. Check MFA
        if (user.getMfaEnabled() != null && user.getMfaEnabled() == 1) {
            result.put("mfaRequired", true);
            result.put("userId", user.getId());
            return result;
        }

        // 4. Login via Sa-Token
        StpUtil.login(user.getId());

        // 5. Return token info
        result.put("token", StpUtil.getTokenValue());
        result.put("mfaRequired", false);
        return result;
    }

    @Override
    @org.springframework.transaction.annotation.Transactional
    public boolean createUser(SysUser user) {
        boolean success = this.save(user);
        if (success && user.getRoleIds() != null) {
            for (Long roleId : user.getRoleIds()) {
                this.baseMapper.insertUserRole(user.getId(), roleId);
            }
        }
        return success;
    }

    @Override
    public java.util.List<Long> getUserMenuIds(Long userId) {
        return this.baseMapper.selectMenuIdsByUserId(userId);
    }

    @Override
    public boolean updateUserMenus(Long userId, java.util.List<Long> menuIds) {
        this.baseMapper.deleteUserMenus(userId);
        if (menuIds != null && !menuIds.isEmpty()) {
            for (Long menuId : menuIds) {
                this.baseMapper.insertUserMenu(userId, menuId);
            }
        }
        return true;
    }

    @Override
    public void updatePassword(Long userId, String oldPassword, String newPassword) {
        SysUser user = this.getById(userId);
        if (user == null) {
            throw new RuntimeException("User not found");
        }
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new RuntimeException("Old password incorrect");
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        this.updateById(user);
    }

    @Override
    public java.util.List<java.util.Map<String, Object>> getChatUsers() {
        return this.baseMapper.selectChatUsers();
    }
}
