package com.cloud.employee.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cloud.employee.common.result.Result;
import com.cloud.employee.entity.SysCandidate;
import com.cloud.employee.entity.SysInterview;
import com.cloud.employee.service.ISysCandidateService;
import com.cloud.employee.service.ISysInterviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/onboarding")
public class OnboardingController {

    @Autowired
    private ISysCandidateService candidateService;

    @Autowired
    private ISysInterviewService interviewService;

    // Candidate List
    @GetMapping("/candidates")
    public Result<Page<SysCandidate>> listCandidates(Page<SysCandidate> page,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Integer status) {
        LambdaQueryWrapper<SysCandidate> query = new LambdaQueryWrapper<>();
        query.like(name != null, SysCandidate::getFullName, name);
        query.eq(status != null, SysCandidate::getStatus, status);
        query.orderByDesc(SysCandidate::getCreateTime);
        return Result.success(candidateService.page(page, query));
    }

    // Add Candidate
    @PostMapping("/candidates")
    public Result<Boolean> addCandidate(@RequestBody SysCandidate candidate) {
        return Result.success(candidateService.save(candidate));
    }

    // Update Status
    @PutMapping("/candidates/status")
    public Result<Boolean> updateStatus(@RequestParam Long candidateId, @RequestParam Integer status) {
        SysCandidate candidate = new SysCandidate();
        candidate.setCandidateId(candidateId);
        candidate.setStatus(status);
        return Result.success(candidateService.updateById(candidate));
    }

    // Interview Records
    @GetMapping("/interviews")
    public Result<List<SysInterview>> getInterviews(@RequestParam Long candidateId) {
        return Result.success(interviewService.list(new LambdaQueryWrapper<SysInterview>()
                .eq(SysInterview::getCandidateId, candidateId)
                .orderByAsc(SysInterview::getRound)));
    }

    @PostMapping("/interviews")
    public Result<Boolean> addInterview(@RequestBody SysInterview interview) {
        return Result.success(interviewService.save(interview));
    }

    @Autowired
    private com.cloud.employee.service.IEmpProfileService empProfileService;

    // Convert to Employee
    @PostMapping("/candidates/convert")
    public Result<Boolean> convertToEmployee(@RequestParam Long candidateId, @RequestParam Long deptId) {
        SysCandidate candidate = candidateService.getById(candidateId);
        if (candidate == null)
            return Result.error("Candidate not found");

        com.cloud.employee.entity.EmpProfile profile = new com.cloud.employee.entity.EmpProfile();
        profile.setFullName(candidate.getFullName());
        profile.setEmergencyPhone(candidate.getPhone());
        profile.setPostId(candidate.getPostId());
        profile.setResumeUrl(candidate.getResumeUrl());
        profile.setDeptId(deptId);
        profile.setStatus(2); // Probation
        profile.setEntryDate(new java.util.Date());
        profile.setEmpCode("E" + System.currentTimeMillis());
        profile.setCreateTime(new java.util.Date());

        boolean success = empProfileService.save(profile);
        if (success) {
            candidate.setStatus(5); // Hired
            candidateService.updateById(candidate);
        }
        return Result.success(success);
    }
}
