package com.cloud.employee.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cloud.employee.common.result.Result;
import com.cloud.employee.entity.EmpProfile;
import com.cloud.employee.entity.SysDept;
import com.cloud.employee.service.IEmpProfileService;
import com.cloud.employee.service.ISysDeptService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/search")
public class SearchController {

    @Autowired
    private IEmpProfileService empProfileService;

    @Autowired
    private ISysDeptService sysDeptService;

    @Autowired
    private com.cloud.employee.service.ISysDocumentService sysDocumentService;

    @GetMapping("/global")
    public Result<Map<String, List<Map<String, Object>>>> globalSearch(@RequestParam String keyword) {
        Map<String, List<Map<String, Object>>> result = new HashMap<>();

        if (keyword == null || keyword.trim().isEmpty()) {
            return Result.success(result);
        }

        // 1. Employee Search (Name or Code) - Limit 5
        List<EmpProfile> employees = empProfileService.list(new LambdaQueryWrapper<EmpProfile>()
                .like(EmpProfile::getFullName, keyword)
                .or()
                .like(EmpProfile::getEmpCode, keyword)
                .last("LIMIT 5"));

        List<Map<String, Object>> employeeList = employees.stream().map(e -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", e.getId());
            map.put("title", e.getFullName());
            map.put("desc", e.getEmpCode() + " - " + sysDeptService.getDeptNameById(e.getDeptId()));
            map.put("type", "employee");
            return map;
        }).collect(Collectors.toList());
        result.put("employees", employeeList);

        // 2. Department Search (Name) - Limit 3
        List<SysDept> depts = sysDeptService.list(new LambdaQueryWrapper<SysDept>()
                .like(SysDept::getDeptName, keyword)
                .last("LIMIT 3"));

        List<Map<String, Object>> deptList = depts.stream().map(d -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", d.getId());
            map.put("title", d.getDeptName());
            map.put("desc", "部门");
            map.put("type", "dept");
            return map;
        }).collect(Collectors.toList());
        result.put("depts", deptList);

        // 3. Document Search (Title) - Limit 5
        List<com.cloud.employee.entity.SysDocument> docs = sysDocumentService
                .list(new LambdaQueryWrapper<com.cloud.employee.entity.SysDocument>()
                        .like(com.cloud.employee.entity.SysDocument::getTitle, keyword)
                        .last("LIMIT 5"));

        List<Map<String, Object>> docList = docs.stream().map(doc -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", doc.getId());
            map.put("title", doc.getTitle());
            map.put("desc", "知识库文档");
            map.put("type", "doc");
            return map;
        }).collect(Collectors.toList());
        result.put("docs", docList);

        return Result.success(result);
    }

    @GetMapping("/suggest/emp")
    public Result<List<Map<String, Object>>> suggestEmployee(@RequestParam String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return Result.success(new ArrayList<>());
        }

        List<EmpProfile> employees = empProfileService.list(new LambdaQueryWrapper<EmpProfile>()
                .like(EmpProfile::getFullName, keyword)
                .or()
                .like(EmpProfile::getEmpCode, keyword)
                .last("LIMIT 10"));

        List<Map<String, Object>> list = employees.stream().map(e -> {
            Map<String, Object> map = new HashMap<>();
            map.put("label", e.getFullName() + " (" + e.getEmpCode() + ")");
            map.put("value", e.getId().toString());
            return map;
        }).collect(Collectors.toList());

        return Result.success(list);
    }
}
