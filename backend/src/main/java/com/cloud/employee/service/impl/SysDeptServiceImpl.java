package com.cloud.employee.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cloud.employee.entity.SysDept;
import com.cloud.employee.mapper.SysDeptMapper;
import com.cloud.employee.service.ISysDeptService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class SysDeptServiceImpl extends ServiceImpl<SysDeptMapper, SysDept> implements ISysDeptService {

    @org.springframework.beans.factory.annotation.Autowired
    private com.cloud.employee.mapper.EmpProfileMapper empProfileMapper;

    @Override
    public List<SysDept> selectDeptTree() {
        // 1. Get all enabled departments
        List<SysDept> depts = this.list(new LambdaQueryWrapper<SysDept>()
                .eq(SysDept::getStatus, "0")
                .orderByAsc(SysDept::getOrderNum));

        // 2. Get headcount stats
        List<Map<String, Object>> stats = empProfileMapper.countGroupedByDeptId();
        Map<Long, Long> statsMap = stats.stream()
                .filter(m -> m.get("deptId") != null)
                .collect(Collectors.toMap(
                        m -> Long.valueOf(m.get("deptId").toString()),
                        m -> Long.valueOf(m.get("count").toString())));

        // 3. Populate empCount
        for (SysDept dept : depts) {
            dept.setEmpCount(statsMap.getOrDefault(dept.getId(), 0L));
        }

        // 4. Build Tree
        return buildTree(depts);
    }

    @Override
    public boolean hasChildren(Long deptId) {
        return this.count(new LambdaQueryWrapper<SysDept>()
                .eq(SysDept::getParentId, deptId)) > 0;
    }

    @Override
    public String getDeptNameById(Long deptId) {
        if (deptId == null) {
            return "Unknown";
        }
        SysDept dept = this.getById(deptId);
        return dept != null ? dept.getDeptName() : "Unknown";
    }

    private List<SysDept> buildTree(List<SysDept> depts) {
        List<SysDept> returnList = new ArrayList<>();
        List<Long> tempList = depts.stream().map(SysDept::getId).collect(Collectors.toList());

        for (SysDept dept : depts) {
            // Check if top level node
            if (!tempList.contains(dept.getParentId())) {
                recursionFn(depts, dept);
                returnList.add(dept);
            }
        }
        if (returnList.isEmpty()) {
            returnList = depts;
        }
        return returnList;
    }

    private void recursionFn(List<SysDept> list, SysDept t) {
        // Get children list
        List<SysDept> childList = getChildList(list, t);
        t.setChildren(childList);
        for (SysDept tChild : childList) {
            if (hasChild(list, tChild)) {
                recursionFn(list, tChild);
            }
        }
    }

    private List<SysDept> getChildList(List<SysDept> list, SysDept t) {
        List<SysDept> tlist = new ArrayList<>();
        for (SysDept n : list) {
            if (n.getParentId() != null && n.getParentId().equals(t.getId())) {
                tlist.add(n);
            }
        }
        return tlist;
    }

    private boolean hasChild(List<SysDept> list, SysDept t) {
        return !getChildList(list, t).isEmpty();
    }
}
