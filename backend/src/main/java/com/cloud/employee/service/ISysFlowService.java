package com.cloud.employee.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cloud.employee.entity.SysFlowTemplate;
import com.cloud.employee.entity.SysFlowStep;
import java.util.List;

public interface ISysFlowService extends IService<SysFlowTemplate> {
    List<SysFlowStep> getSteps(Long templateId);

    Long startFlow(Long templateId, Long employeeId);

    List<java.util.Map<String, Object>> getInstanceList();

    List<com.cloud.employee.entity.SysFlowTask> getTasks(Long instanceId);

    void completeTask(Long taskId, String remark);
}
