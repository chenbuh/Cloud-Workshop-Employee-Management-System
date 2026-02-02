package com.cloud.employee.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cloud.employee.entity.SysDocument;
import com.cloud.employee.mapper.SysDocumentMapper;
import com.cloud.employee.service.ISysDocumentService;
import org.springframework.stereotype.Service;

@Service
public class SysDocumentServiceImpl extends ServiceImpl<SysDocumentMapper, SysDocument> implements ISysDocumentService {
    @Override
    public void incrementViewCount(Long id) {
        baseMapper.incrementViewCount(id);
    }
}
