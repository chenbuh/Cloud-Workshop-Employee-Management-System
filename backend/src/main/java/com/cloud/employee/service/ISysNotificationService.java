package com.cloud.employee.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cloud.employee.entity.SysNotification;

public interface ISysNotificationService extends IService<SysNotification> {
    void send(Long userId, String title, String content, String type);
}
