# 内部通讯系统 (Internal Chat System)

本项目集成了一个仿飞书样式的实时内部通讯功能，支持单聊、公共频道广播以及实时消息通知。

## 技术实现

### 后端 (Spring Boot)
- **WebSocket**: 使用 `spring-boot-starter-websocket` 实现双工通信。
- **持久化**: 聊天记录存储在 `sys_chat_message` 表中，支持历史记录回溯。
- **安全性**: 集成 Sa-Token 获取当前登录用户信息，确保通信安全。
- **配置类**: `WebSocketConfig.java` 注册 `ServerEndpointExporter`。
- **服务端**: `ChatWebSocketServer.java` 处理连接挂载、消息转发及存储。

### 前端 (Vue 3 + Naive UI)
- **页面**: `ChatView.vue` 提供全屏即时通讯界面，包含联系人搜索、历史记录加载、实时聊天窗口等。
- **状态管理**: 引入 `store/chat.ts` (Pinia) 统一管理 WebSocket 连接、未读消息状态及全局消息分发。
- **全局通知**: `InternalChat.vue` 转型为后台通知处理器。当用户不在聊天页面时，新消息会触发全局气泡通知，支持点击跳转至聊天页面。
- **消息提醒**: 
    - 集成 Naive UI `notification` 服务。
    - 仿飞书样式：当有新消息且当前不在聊天页面时，显示带头像的气泡提醒。
    - 支持点击提醒进入对应的私聊界面。
- **实时性**: 基于 Pinia + 原生 `WebSocket`。

## 数据库设计
```sql
CREATE TABLE `sys_chat_message` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `from_user_id` BIGINT NOT NULL COMMENT '发送者ID',
    `to_user_id` BIGINT COMMENT '接收者ID (NULL表示群发)',
    `content` TEXT COMMENT '内容',
    `type` VARCHAR(10) DEFAULT 'text' COMMENT '类型',
    `is_read` TINYINT DEFAULT 0 COMMENT '已读状态',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`)
);
```

## 功能亮点
1. **实时触达**：毫秒级消息延迟。
2. **多模式聊天**：支持部门内同事单聊及公共频道全员喊话。
3. **桌面级交互**：支持 Enter 发送、Shift+Enter 换行，自动滚动到底部。
4. **消息兜底**：离线消息在下次上线打开聊天框时即可查询历史记录并清除未读状态。

## 故障排除

### WebSocket 连接错误
#### 问题: `DecodeException: Failed to decode path parameter value [undefined]`
**原因**: 前端在建立 WebSocket 连接时传递了 `undefined` 作为用户ID参数。

**解决方案**:
1. 确保在初始化 WebSocket 前验证用户ID的有效性
2. 在 `chat.ts` 的 `initWebSocket` 方法中添加了用户ID类型检查
3. 只有当用户ID存在且为有效数字时才建立连接

**代码示例**:
```typescript
const initWebSocket = (userId: number) => {
    // 检查 userId 是否有效
    if (!userId || typeof userId !== 'number') {
        console.warn('Invalid userId for WebSocket connection:', userId)
        return
    }
    // ... 建立连接
}
```

## 最佳实践
1. **连接前验证**: 始终在建立 WebSocket 连接前验证用户ID的有效性
2. **错误处理**: 添加完善的错误处理机制，防止无效参数导致的连接失败
3. **重连机制**: 实现智能重连，但只在用户ID有效时才重连
4. **日志记录**: 使用 console.warn 记录连接失败的原因，便于调试

## 维护日志
- **2026-02-04**: 修复 WebSocket 连接时 userId 为 undefined 的问题，添加参数验证机制
