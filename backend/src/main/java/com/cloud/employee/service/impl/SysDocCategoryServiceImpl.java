package com.cloud.employee.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cloud.employee.entity.SysDocCategory;
import com.cloud.employee.mapper.SysDocCategoryMapper;
import com.cloud.employee.service.ISysDocCategoryService;
import org.springframework.stereotype.Service;

@Service
public class SysDocCategoryServiceImpl extends ServiceImpl<SysDocCategoryMapper, SysDocCategory>
        implements ISysDocCategoryService {
}
