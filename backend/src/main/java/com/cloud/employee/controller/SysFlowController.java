package com.cloud.employee.controller;

import com.cloud.employee.common.result.Result;
import com.cloud.employee.entity.SysFlowTask;
import com.cloud.employee.entity.SysFlowTemplate;
import com.cloud.employee.service.ISysFlowService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/flow")
public class SysFlowController {

    @Autowired
    private ISysFlowService flowService;

    @GetMapping("/templates")
    public Result<List<SysFlowTemplate>> listTemplates() {
        return Result.success(flowService.list());
    }

    @GetMapping("/instances")
    public Result<List<Map<String, Object>>> listInstances() {
        return Result.success(flowService.getInstanceList());
    }

    @PostMapping("/start")
    public Result<Long> startFlow(@RequestBody Map<String, Long> params) {
        Long templateId = params.get("templateId");
        Long employeeId = params.get("employeeId");
        return Result.success(flowService.startFlow(templateId, employeeId));
    }

    @GetMapping("/tasks/{instanceId}")
    public Result<List<SysFlowTask>> listTasks(@PathVariable Long instanceId) {
        return Result.success(flowService.getTasks(instanceId));
    }

    @PutMapping("/task/complete")
    public Result<String> completeTask(@RequestBody Map<String, Object> params) {
        Long taskId = Long.valueOf(params.get("taskId").toString());
        String remark = params.get("remark") != null ? params.get("remark").toString() : "";
        flowService.completeTask(taskId, remark);
        return Result.success("任务已完成");
    }
}
