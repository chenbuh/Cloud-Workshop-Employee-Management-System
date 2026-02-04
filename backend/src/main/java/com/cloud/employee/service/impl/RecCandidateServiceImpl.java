package com.cloud.employee.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cloud.employee.entity.RecCandidate;
import com.cloud.employee.mapper.RecCandidateMapper;
import com.cloud.employee.service.IRecCandidateService;
import com.cloud.employee.vo.RecCandidateVO;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RecCandidateServiceImpl extends ServiceImpl<RecCandidateMapper, RecCandidate>
        implements IRecCandidateService {
    @Override
    public List<RecCandidateVO> selectListWithJob() {
        return baseMapper.selectListWithJob();
    }
}
