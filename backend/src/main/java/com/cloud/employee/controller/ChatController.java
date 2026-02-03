package com.cloud.employee.controller;

import com.cloud.employee.common.result.Result;
import com.cloud.employee.entity.SysChatMessage;
import com.cloud.employee.service.ISysChatMessageService;
import com.cloud.employee.service.ISysUserService;
import cn.dev33.satoken.stp.StpUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/chat")
public class ChatController {

    @Autowired
    private ISysChatMessageService chatService;

    @Autowired
    private ISysUserService userService;

    @GetMapping("/history")
    public Result<List<SysChatMessage>> getHistory(@RequestParam(required = false) Long toUserId) {
        Long currentUserId = StpUtil.getLoginIdAsLong();
        return Result.success(chatService.getChatHistory(currentUserId, toUserId));
    }

    @GetMapping("/users")
    public Result<java.util.List<java.util.Map<String, Object>>> getChatUsers() {
        // Return all active users with profile information
        return Result.success(userService.getChatUsers());
    }

    @PostMapping("/read")
    public Result<Boolean> markAsRead(@RequestParam Long fromUserId) {
        Long currentUserId = StpUtil.getLoginIdAsLong();
        chatService.markAsRead(fromUserId, currentUserId);
        return Result.success(true);
    }
}
