package com.cloud.employee.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cloud.employee.entity.SysFlowInstance;
import com.cloud.employee.entity.SysFlowStep;
import com.cloud.employee.entity.SysFlowTask;
import com.cloud.employee.entity.SysFlowTemplate;
import com.cloud.employee.mapper.SysFlowInstanceMapper;
import com.cloud.employee.mapper.SysFlowStepMapper;
import com.cloud.employee.mapper.SysFlowTaskMapper;
import com.cloud.employee.mapper.SysFlowTemplateMapper;
import com.cloud.employee.service.ISysFlowService;
import cn.dev33.satoken.stp.StpUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Service
public class SysFlowServiceImpl extends ServiceImpl<SysFlowTemplateMapper, SysFlowTemplate> implements ISysFlowService {

    @Autowired
    private SysFlowStepMapper stepMapper;

    @Autowired
    private SysFlowInstanceMapper instanceMapper;

    @Autowired
    private SysFlowTaskMapper taskMapper;

    @Override
    public List<SysFlowStep> getSteps(Long templateId) {
        return stepMapper.selectList(new LambdaQueryWrapper<SysFlowStep>()
                .eq(SysFlowStep::getTemplateId, templateId)
                .orderByAsc(SysFlowStep::getSortOrder));
    }

    @Override
    @Transactional
    public Long startFlow(Long templateId, Long employeeId) {
        // 1. Create Instance
        SysFlowInstance instance = new SysFlowInstance();
        instance.setTemplateId(templateId);
        instance.setEmployeeId(employeeId);
        instance.setStatus(0);
        instance.setProgress(0);
        instance.setStartTime(new Date());
        instanceMapper.insert(instance);

        // 2. Create Tasks from Steps
        List<SysFlowStep> steps = getSteps(templateId);
        for (SysFlowStep step : steps) {
            SysFlowTask task = new SysFlowTask();
            task.setInstanceId(instance.getId());
            task.setStepId(step.getId());
            task.setTitle(step.getTitle());
            task.setAssigneeRole(step.getAssigneeRole());
            task.setStatus(0);
            taskMapper.insert(task);
        }
        return instance.getId();
    }

    @Override
    public List<Map<String, Object>> getInstanceList() {
        return instanceMapper.selectInstanceList();
    }

    @Override
    public List<SysFlowTask> getTasks(Long instanceId) {
        return taskMapper.selectList(new LambdaQueryWrapper<SysFlowTask>()
                .eq(SysFlowTask::getInstanceId, instanceId));
    }

    @Override
    @Transactional
    public void completeTask(Long taskId, String remark) {
        SysFlowTask task = taskMapper.selectById(taskId);
        if (task == null || task.getStatus() == 1)
            return;

        task.setStatus(1);
        task.setRemark(remark);
        task.setOperatorId(StpUtil.getLoginIdAsLong());
        task.setCompleteTime(new Date());
        taskMapper.updateById(task);

        // Update progress
        updateInstanceProgress(task.getInstanceId());
    }

    private void updateInstanceProgress(Long instanceId) {
        List<SysFlowTask> tasks = getTasks(instanceId);
        if (tasks.isEmpty())
            return;

        long completed = tasks.stream().filter(t -> t.getStatus() == 1).count();
        int progress = (int) (completed * 100 / tasks.size());

        SysFlowInstance instance = instanceMapper.selectById(instanceId);
        instance.setProgress(progress);
        if (progress == 100) {
            instance.setStatus(1);
            instance.setEndTime(new Date());
        }
        instanceMapper.updateById(instance);
    }
}
