package com.cloud.employee.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cloud.employee.entity.SysDept;
import java.util.List;

public interface ISysDeptService extends IService<SysDept> {
    List<SysDept> selectDeptTree();

    boolean hasChildren(Long deptId);

    String getDeptNameById(Long deptId);
}
