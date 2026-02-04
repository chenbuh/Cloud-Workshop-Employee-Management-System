package com.cloud.employee.controller;

import com.cloud.employee.common.result.Result;
import com.cloud.employee.entity.RecCandidate;
import com.cloud.employee.entity.RecJob;
import com.cloud.employee.vo.RecCandidateVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.cloud.employee.service.IRecCandidateService;
import com.cloud.employee.service.IRecInterviewService;
import com.cloud.employee.service.IRecJobService;
import com.cloud.employee.vo.RecInterviewVO;
import com.cloud.employee.entity.RecInterview;

import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/api/v1/recruitment")
public class RecruitmentController {

    @Autowired
    private IRecJobService jobService;

    @Autowired
    private IRecCandidateService candidateService;

    @Autowired
    private IRecInterviewService interviewService;

    // --- Job Management ---

    @GetMapping("/jobs")
    public Result<List<RecJob>> listJobs() {
        return Result.success(jobService.list());
    }

    @PostMapping("/jobs")
    public Result<Boolean> createJob(@RequestBody RecJob job) {
        job.setCreateTime(new Date());
        job.setStatus(1);
        jobService.save(job);
        return Result.success(true);
    }

    // --- Candidate Management (Kanban) ---

    @GetMapping("/candidates")
    public Result<List<RecCandidateVO>> listCandidates() {
        return Result.success(candidateService.selectListWithJob());
    }

    @PostMapping("/candidates")
    public Result<Boolean> createCandidate(@RequestBody RecCandidate candidate) {
        candidate.setCreateTime(new Date());
        candidate.setStatus(1); // Default: New Resume
        candidateService.save(candidate);
        return Result.success(true);
    }

    @PutMapping("/candidates/status")
    public Result<Boolean> updateCandidateStatus(@RequestBody RecCandidate candidate) {
        RecCandidate existing = candidateService.getById(candidate.getId());
        if (existing == null)
            return Result.error("Candidate not found");

        existing.setStatus(candidate.getStatus());
        existing.setUpdateTime(new Date());
        candidateService.updateById(existing);
        return Result.success(true);
    }

    // --- Interview Management ---

    @GetMapping("/interviews")
    public Result<List<RecInterviewVO>> listInterviews(
            @RequestParam(required = false) Long candidateId) {
        if (candidateId != null) {
            // Mapping RecInterview to RecInterviewVO might be better, but for simplicity we
            // can return VO list from service
            // However, the current service.list returns RecInterview.
            // Let's create a specific method for this if needed.
            // For now, let's just use the existing selectAllWithCandidate or filter it.
            return Result.success(interviewService.selectAllWithCandidate().stream()
                    .filter(i -> i.getCandidateId().equals(candidateId))
                    .collect(java.util.stream.Collectors.toList()));
        } else {
            return Result.success(interviewService.selectAllWithCandidate());
        }
    }

    @Autowired
    private com.cloud.employee.service.ISysNotificationService notificationService;

    @PostMapping("/interviews")
    public Result<Boolean> addInterview(@RequestBody RecInterview interview) {
        interview.setCreateTime(new Date());
        interview.setStatus(0); // Pending
        interviewService.save(interview);

        // Update candidate status to "Interviewing" if not already
        RecCandidate candidate = candidateService.getById(interview.getCandidateId());
        if (candidate != null && candidate.getStatus() < 3) {
            candidate.setStatus(3);
            candidateService.updateById(candidate);
        }

        // Send notification to the interviewer
        if (interview.getInterviewerId() != null) {
            com.cloud.employee.entity.SysNotification notify = new com.cloud.employee.entity.SysNotification();
            notify.setUserId(interview.getInterviewerId());
            notify.setTitle("新面试安排提醒");
            notify.setContent("您有一场新的面试安排，候选人：" + (candidate != null ? candidate.getName() : "未知") + "，时间："
                    + interview.getInterviewTime());
            notify.setType("info");
            notify.setIsRead(0);
            notify.setCreateTime(new Date());
            notificationService.save(notify);
        }

        return Result.success(true);
    }

    @PutMapping("/interviews")
    public Result<Boolean> updateInterview(@RequestBody RecInterview interview) {
        interviewService.updateById(interview);
        return Result.success(true);
    }
}
