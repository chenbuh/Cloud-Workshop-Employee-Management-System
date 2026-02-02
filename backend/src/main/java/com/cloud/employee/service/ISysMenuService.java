package com.cloud.employee.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cloud.employee.entity.SysMenu;
import java.util.List;

public interface ISysMenuService extends IService<SysMenu> {
    List<SysMenu> selectMenuTree();
}
