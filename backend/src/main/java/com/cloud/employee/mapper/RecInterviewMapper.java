package com.cloud.employee.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cloud.employee.entity.RecInterview;
import com.cloud.employee.vo.RecInterviewVO;
import org.apache.ibatis.annotations.Select;
import java.util.List;

public interface RecInterviewMapper extends BaseMapper<RecInterview> {

    @Select("SELECT i.*, c.name as candidate_name, j.job_title, j.id as job_id " +
            "FROM rec_interview i " +
            "LEFT JOIN rec_candidate c ON i.candidate_id = c.id " +
            "LEFT JOIN rec_job j ON c.apply_job_id = j.id " +
            "ORDER BY i.interview_time ASC")
    List<RecInterviewVO> selectAllWithCandidate();
}
