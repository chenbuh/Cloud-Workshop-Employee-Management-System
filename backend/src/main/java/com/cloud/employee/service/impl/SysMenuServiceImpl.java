package com.cloud.employee.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cloud.employee.entity.SysMenu;
import com.cloud.employee.mapper.SysMenuMapper;
import com.cloud.employee.service.ISysMenuService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SysMenuServiceImpl extends ServiceImpl<SysMenuMapper, SysMenu> implements ISysMenuService {

    @Override
    public List<SysMenu> selectMenuTree() {
        List<SysMenu> menus = this.list(new LambdaQueryWrapper<SysMenu>().orderByAsc(SysMenu::getOrderNum));
        return buildMenuTree(menus, 0L);
    }

    private List<SysMenu> buildMenuTree(List<SysMenu> menus, Long parentId) {
        return menus.stream()
                .filter(m -> m.getParentId().equals(parentId))
                .peek(m -> m.setChildren(buildMenuTree(menus, m.getMenuId())))
                .collect(Collectors.toList());
    }
}
