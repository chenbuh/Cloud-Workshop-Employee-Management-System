package com.cloud.employee.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cloud.employee.entity.RecInterview;

import com.cloud.employee.vo.RecInterviewVO;

import java.util.List;

public interface IRecInterviewService extends IService<RecInterview> {
    List<RecInterviewVO> selectAllWithCandidate();
}
