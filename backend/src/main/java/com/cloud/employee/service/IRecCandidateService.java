package com.cloud.employee.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cloud.employee.entity.RecCandidate;
import com.cloud.employee.vo.RecCandidateVO;

import java.util.List;

public interface IRecCandidateService extends IService<RecCandidate> {
    List<RecCandidateVO> selectListWithJob();
}
