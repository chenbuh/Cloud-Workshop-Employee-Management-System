package com.cloud.employee.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cloud.employee.entity.SysSkill;
import com.cloud.employee.mapper.SysSkillMapper;
import com.cloud.employee.service.ISysSkillService;
import org.springframework.stereotype.Service;

@Service
public class SysSkillServiceImpl extends ServiceImpl<SysSkillMapper, SysSkill> implements ISysSkillService {
}
