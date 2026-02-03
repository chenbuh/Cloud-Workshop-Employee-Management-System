package com.cloud.employee.websocket;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.cloud.employee.entity.SysChatMessage;
import com.cloud.employee.entity.SysUser;
import com.cloud.employee.service.ISysChatMessageService;
import com.cloud.employee.service.ISysUserService;
import jakarta.websocket.*;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@ServerEndpoint("/ws/chat/{userId}")
public class ChatWebSocketServer {

    private static final Map<Long, Session> sessionPool = new ConcurrentHashMap<>();

    private static ISysChatMessageService chatService;
    private static ISysUserService userService;

    @Autowired
    public void setChatService(ISysChatMessageService chatService) {
        ChatWebSocketServer.chatService = chatService;
    }

    @Autowired
    public void setUserService(ISysUserService userService) {
        ChatWebSocketServer.userService = userService;
    }

    @OnOpen
    public void onOpen(Session session, @PathParam("userId") Long userId) {
        sessionPool.put(userId, session);
        log.info("Chat WebSocket opened for user: {}", userId);

        // Broadcast online status
        broadcastStatus(userId, "online");

        // Send current online user list to the new user
        JSONObject initMsg = new JSONObject();
        initMsg.put("type", "user_list");
        initMsg.put("onlineUsers", sessionPool.keySet());
        try {
            session.getBasicRemote().sendText(initMsg.toJSONString());
        } catch (Exception e) {
            log.error("Error sending initial user list", e);
        }
    }

    @OnClose
    public void onClose(@PathParam("userId") Long userId) {
        sessionPool.remove(userId);
        log.info("Chat WebSocket closed for user: {}", userId);

        // Broadcast offline status
        broadcastStatus(userId, "offline");
    }

    private void broadcastStatus(Long userId, String status) {
        JSONObject msg = new JSONObject();
        msg.put("type", "status_change");
        msg.put("userId", userId);
        msg.put("status", status); // online, offline
        String msgStr = msg.toJSONString();

        for (Session session : sessionPool.values()) {
            if (session.isOpen()) {
                try {
                    session.getBasicRemote().sendText(msgStr);
                } catch (Exception e) {
                    log.error("Error broadcasting status", e);
                }
            }
        }
    }

    @OnMessage
    public void onMessage(String message, Session session, @PathParam("userId") Long userId) {
        log.info("Received message from user {}: {}", userId, message);
        try {
            JSONObject json = JSON.parseObject(message);
            Long toUserId = json.getLong("toUserId");
            String content = json.getString("content");
            String type = json.getString("type");

            // Save to database
            SysChatMessage chatMsg = new SysChatMessage();
            chatMsg.setFromUserId(userId);
            chatMsg.setToUserId(toUserId);
            chatMsg.setContent(content);
            chatMsg.setType(type == null ? "text" : type);
            chatMsg.setIsRead(0);
            chatMsg.setCreateTime(new Date());
            chatService.save(chatMsg);

            // Prepare VO or enriched message for realtime delivery
            JSONObject response = (JSONObject) JSON.toJSON(chatMsg);
            SysUser fromUser = userService.getById(userId);
            if (fromUser != null) {
                response.put("fromUserNick", fromUser.getNickName());
                response.put("fromUserAvatar", fromUser.getAvatar());
            }

            String msgStr = response.toJSONString();

            // Send to target user
            if (toUserId != null) {
                if (sessionPool.containsKey(toUserId)) {
                    sessionPool.get(toUserId).getBasicRemote().sendText(msgStr);
                }
            } else {
                // Broadcast to all online users
                for (Map.Entry<Long, Session> entry : sessionPool.entrySet()) {
                    if (!entry.getKey().equals(userId) && entry.getValue().isOpen()) {
                        entry.getValue().getBasicRemote().sendText(msgStr);
                    }
                }
            }

            // Also send back to self (optional, usually frontend handles its own sent
            // message)
            // session.getBasicRemote().sendText(msgStr);

        } catch (Exception e) {
            log.error("Error processing message", e);
        }
    }

    @OnError
    public void onError(Session session, Throwable error) {
        log.debug("WebSocket error for session {}: {}", session.getId(), error.getMessage());
    }
}
