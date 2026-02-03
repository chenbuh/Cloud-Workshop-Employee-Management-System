package com.cloud.employee.controller;

import com.cloud.employee.common.result.Result;
import com.cloud.employee.entity.RecCandidate;
import com.cloud.employee.entity.RecJob;
import com.cloud.employee.mapper.RecCandidateMapper;
import com.cloud.employee.mapper.RecJobMapper;
import com.cloud.employee.vo.RecCandidateVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/api/v1/recruitment")
public class RecruitmentController {

    @Autowired
    private RecJobMapper jobMapper;

    @Autowired
    private RecCandidateMapper candidateMapper;

    // --- Job Management ---

    @GetMapping("/jobs")
    public Result<List<RecJob>> listJobs() {
        return Result.success(jobMapper.selectList(null));
    }

    @PostMapping("/jobs")
    public Result<Boolean> createJob(@RequestBody RecJob job) {
        job.setCreateTime(new Date());
        job.setStatus(1);
        jobMapper.insert(job);
        return Result.success(true);
    }

    // --- Candidate Management (Kanban) ---

    @GetMapping("/candidates")
    public Result<List<RecCandidateVO>> listCandidates() {
        return Result.success(candidateMapper.selectListWithJob());
    }

    @PostMapping("/candidates")
    public Result<Boolean> createCandidate(@RequestBody RecCandidate candidate) {
        candidate.setCreateTime(new Date());
        candidate.setStatus(1); // Default: New Resume
        candidateMapper.insert(candidate);
        return Result.success(true);
    }

    @PutMapping("/candidates/status")
    public Result<Boolean> updateCandidateStatus(@RequestBody RecCandidate candidate) {
        RecCandidate existing = candidateMapper.selectById(candidate.getId());
        if (existing == null)
            return Result.error("Candidate not found");

        existing.setStatus(candidate.getStatus());
        existing.setUpdateTime(new Date());
        candidateMapper.updateById(existing);
        return Result.success(true);
    }

    // --- Interview Management ---

    @Autowired
    private com.cloud.employee.mapper.RecInterviewMapper interviewMapper;

    @GetMapping("/interviews")
    public Result<List<com.cloud.employee.entity.RecInterview>> listInterviews(
            @RequestParam(required = false) Long candidateId) {
        if (candidateId != null) {
            return Result.success(interviewMapper.selectList(
                    new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<com.cloud.employee.entity.RecInterview>()
                            .eq("candidate_id", candidateId)
                            .orderByDesc("create_time")));
        } else {
            return Result.success(new java.util.ArrayList<com.cloud.employee.entity.RecInterview>(
                    interviewMapper.selectAllWithCandidate()));
        }
    }

    @PostMapping("/interviews")
    public Result<Boolean> addInterview(@RequestBody com.cloud.employee.entity.RecInterview interview) {
        interview.setCreateTime(new Date());
        interview.setStatus(0); // Pending
        interviewMapper.insert(interview);

        // Update candidate status to "Interviewing" if not already
        RecCandidate candidate = candidateMapper.selectById(interview.getCandidateId());
        if (candidate != null && candidate.getStatus() < 3) {
            candidate.setStatus(3);
            candidateMapper.updateById(candidate);
        }

        return Result.success(true);
    }

    @PutMapping("/interviews")
    public Result<Boolean> updateInterview(@RequestBody com.cloud.employee.entity.RecInterview interview) {
        interviewMapper.updateById(interview);
        return Result.success(true);
    }
}
