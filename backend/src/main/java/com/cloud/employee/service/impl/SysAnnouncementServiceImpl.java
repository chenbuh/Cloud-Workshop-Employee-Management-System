package com.cloud.employee.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cloud.employee.entity.SysAnnouncement;
import com.cloud.employee.mapper.SysAnnouncementMapper;
import com.cloud.employee.service.ISysAnnouncementService;
import org.springframework.stereotype.Service;

@Service
public class SysAnnouncementServiceImpl extends ServiceImpl<SysAnnouncementMapper, SysAnnouncement>
        implements ISysAnnouncementService {
}
