# 项目维护与清理日志

本文档记录了项目进行过的重大重构、文件清理以及环境优化操作。

## 2026-02-04: WebSocket 连接异常修复

### 问题描述
后端日志频繁出现 `jakarta.websocket.DecodeException: Failed to decode path parameter value [undefined]` 错误。
错误原因是前端在建立 WebSocket 连接时，传递了 `undefined` 作为用户ID参数，导致后端无法将字符串 "undefined" 解析为 Long 类型。

### 修复内容
1. **前端 `store/chat.ts`**:
   - 在 `initWebSocket` 方法中添加用户ID有效性检查
   - 添加类型验证：确保 userId 存在且为 number 类型
   - 优化重连机制：只在用户ID有效时才执行重连

2. **前端 `ChatView.vue`**:
   - 在 `onMounted` 钩子中添加 currentUserId 验证
   - 只有在用户ID有效时才初始化 WebSocket 连接
   - 添加警告日志便于排查问题

3. **前端 `InternalChat.vue`**:
   - 增强用户ID验证逻辑
   - 确保类型安全的 WebSocket 连接初始化

4. **文档更新**:
   - 更新 `docs/INTERNAL_CHAT.md`
   - 添加故障排除章节
   - 添加最佳实践说明
   - 添加维护日志

### 技术要点
- **参数验证**: 在传递路径参数到 WebSocket 连接前必须验证参数有效性
- **类型安全**: TypeScript 类型注解不能防止运行时的 undefined 值传递
- **防御性编程**: 在关键入口点添加验证逻辑，防止无效数据流入

### 结果
- 消除了 WebSocket 连接异常错误
- 提高了系统的健壮性和可维护性
- 完善了错误日志，便于后续问题排查

---

## 2026-02-03: 项目结构清理与文档完善

### 1. 结构清理
删除了 17 个冗余文件（SQL 补丁、导出文本、调试批处理），优化了根目录整洁度。

### 2. 文档完善
- **新增 `README.md`**: 完善了项目首页说明，明确了技术栈、核心特性及本地开发启动流程，并集成了 GitHub 仓库地址。
- **创建 `MAINTENANCE.md`**: 用于记录项目长期的维护、重构与清理行为。

### 3. 登录逻辑修复
- 修复了 `admin` 账号登录后因状态同步问题返回登录页的 Bug。
- 完善了全局路由守卫逻辑。

### 结果
- 根目录目前仅保留核心项目文件夹（frontend, backend, docs, .agent, .idea, uploads）。
- 提高了代码库的安全性和可维护性。
- 建议后续所有的数据库变更均通过 `docs/db/update_db.sql` 统一进行，或者使用版本化迁移工具。
