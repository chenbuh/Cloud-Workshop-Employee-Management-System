package com.cloud.employee.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cloud.employee.entity.SysChatMessage;
import com.cloud.employee.mapper.SysChatMessageMapper;
import com.cloud.employee.service.ISysChatMessageService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SysChatMessageServiceImpl extends ServiceImpl<SysChatMessageMapper, SysChatMessage>
        implements ISysChatMessageService {

    @Override
    public List<SysChatMessage> getChatHistory(Long userId1, Long userId2) {
        LambdaQueryWrapper<SysChatMessage> wrapper = new LambdaQueryWrapper<>();
        if (userId2 == null) {
            // Public channel or broad cast
            wrapper.isNull(SysChatMessage::getToUserId);
        } else {
            // One-to-one chat
            wrapper.and(w -> w
                    .and(w1 -> w1.eq(SysChatMessage::getFromUserId, userId1).eq(SysChatMessage::getToUserId, userId2))
                    .or(w2 -> w2.eq(SysChatMessage::getFromUserId, userId2).eq(SysChatMessage::getToUserId, userId1)));
        }
        wrapper.orderByAsc(SysChatMessage::getCreateTime);
        return list(wrapper);
    }

    @Override
    public void markAsRead(Long fromUserId, Long toUserId) {
        LambdaUpdateWrapper<SysChatMessage> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(SysChatMessage::getFromUserId, fromUserId)
                .eq(SysChatMessage::getToUserId, toUserId)
                .eq(SysChatMessage::getIsRead, 0)
                .set(SysChatMessage::getIsRead, 1);
        update(wrapper);
    }
}
