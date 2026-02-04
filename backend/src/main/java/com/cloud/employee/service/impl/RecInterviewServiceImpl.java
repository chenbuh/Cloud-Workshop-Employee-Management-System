package com.cloud.employee.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cloud.employee.entity.RecInterview;
import com.cloud.employee.mapper.RecInterviewMapper;
import com.cloud.employee.service.IRecInterviewService;
import com.cloud.employee.vo.RecInterviewVO;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RecInterviewServiceImpl extends ServiceImpl<RecInterviewMapper, RecInterview>
        implements IRecInterviewService {
    @Override
    public List<RecInterviewVO> selectAllWithCandidate() {
        return baseMapper.selectAllWithCandidate();
    }
}
