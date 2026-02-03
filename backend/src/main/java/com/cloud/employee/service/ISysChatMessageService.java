package com.cloud.employee.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cloud.employee.entity.SysChatMessage;
import java.util.List;

public interface ISysChatMessageService extends IService<SysChatMessage> {
    List<SysChatMessage> getChatHistory(Long userId1, Long userId2);

    void markAsRead(Long fromUserId, Long toUserId);
}
