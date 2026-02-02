package com.cloud.employee.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cloud.employee.entity.SysDocument;

public interface ISysDocumentService extends IService<SysDocument> {
    void incrementViewCount(Long id);
}
