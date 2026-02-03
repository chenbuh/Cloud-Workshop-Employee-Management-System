package com.cloud.employee.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cloud.employee.entity.RecCandidate;
import com.cloud.employee.vo.RecCandidateVO;
import org.apache.ibatis.annotations.Select;
import java.util.List;

public interface RecCandidateMapper extends BaseMapper<RecCandidate> {

    @Select("SELECT c.*, j.job_title FROM rec_candidate c LEFT JOIN rec_job j ON c.apply_job_id = j.id ORDER BY c.create_time DESC")
    List<RecCandidateVO> selectListWithJob();
}
