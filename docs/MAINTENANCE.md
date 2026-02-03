# 项目维护与清理日志

本文档记录了项目进行过的重大重构、文件清理以及环境优化操作。

## 2026-02-04: 账号安全体系全面升级

### 功能描述
为了提升用户账号的安全可控性，对个人中心的“账号安全”模块进行了深度重构与功能扩展。

### 核心功能点
1. **安全审计日志 (Security Audit Log)**:
   - 实现了基于用户维度的操作日志查询接口。
   - 前端新增“安全审计”页签，用户可实时查看自己的登录 IP、操作内容及时间。

2. **多端会话管理 (Active Sessions)**:
   - 集成 Sa-Token 的 Token 管理能力，支持查看当前账号下的所有活动连接。
   - 提供“强制下线”功能，允许用户远程注销其他设备的异常登录。

3. **动态安全评分系统**:
   - 根据用户资料完整度（手机、邮箱）、会话状态、近期活动等维度动态计算安全得分。
   - 增加了直观的安全进度条与等级评价（极高、良好、中等）。

4. **两步验证 (MFA) 预研**:
   - 预留了 MFA 开启入口，并增强了 UI 交互提示。

---

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
- 消除 WebSocket 连接异常错误
- 提高系统的健壮性和可维护性
- 完善错误日志，便于后续问题排查

---

## 2026-02-04: 聊天初始化“无效用户 ID”错误修复

### 问题描述
控制台报错 `InternalChat.vue:53 Cannot initialize chat: Invalid user ID`。
原因是 `InternalChat.vue` 和 `ChatView.vue` 在静态初始化阶段直接通过 `localStorage` 读取用户信息。在应用启动或登录状态变更的临界点，`localStorage` 可能尚未同步或为空，导致 `currentUserId` 为 `undefined`。

### 修复内容
1. **ChatStore 增强 (`store/chat.ts`)**:
   - 增加 `closeWebSocket()` 方法，支持在退出登录时主动切断连接并停止重连逻辑。

2. **全局组件优化 (`InternalChat.vue`)**:
   - 移除 top-level 的 `localStorage` 读取。
   - 引入 `watch` 机制监听 `userStore.userInfo.id`。
   - 实现**响应式初始化**：一旦用户 ID 变为有效数字，自动启动 WebSocket；若 ID 消失（登出），自动关闭 WebSocket。

3. **视图组件同步 (`ChatView.vue`)**:
   - 将 `currentUserId` 改造为 `computed` 属性，适配 UserStore 的响应式变化。
   - 更新所有消息对比和初始化逻辑，确保使用 `.value` 访问最新用户 ID。

### 结果
- 彻底消除了因读取时序问题导致的“无效用户 ID”警告。
- 聊天系统现在能完美适配登录/登出状态切换，无需手动刷新页面。
---

## 2026-02-04: 文件上传大小限制调整

### 问题描述
用户在上传较大头像时遇到 `Maximum upload size exceeded` 错误。这是由于 Spring Boot 默认的文件上传限制（通常为 1MB）过小导致的。

### 修复内容
1. **后端配置 `application.yml`**:
   - 修正了 `spring.servlet.multipart` 的**层级缩进**错误（此前错误地放在了 `spring.mvc.pathmatch` 下）。
   - 确认 `max-file-size: 10MB` 和 `max-request-size: 10MB` 已正确置于 `spring.servlet` 下。
   - 将限制提高到 10MB，以覆盖常见的头像及文档上传需求。

### 结果
- 解决了上传较大文件时抛出异常的问题。
- 提升了系统的可用性，支持高质量图片的上传。

---

## 2026-02-04: 个人中心功能深度完善

### 功能迭代
在初步上线的基础上，对“个人中心”进行了深度功能增强，使其从单一的展示页面转变为完整的个人管理终端。

### 核心改进点
1. **异步交互与数据同步**:
   - **后端授权接口**: 在 `SysUserController` 扩展了 `updateProfile` 接口，严格限制只能修改本人受限字段（昵称、头像等）。
   - **Store 实时响应**: 实现资料修改后自动更新 Pinia `userStore` 与 `localStorage`，确保全站头像与昵称同步更新，无需刷新。

2. **多媒体能力接入**:
   - **动态头像上传**: 集成 `FileController` 的二进制流处理，支持头像点击上传、自动压缩提示及实时 API 关联。

3. **安全闭环**:
   - **独立密码修改**: 在个人中心直接嵌入密码重置模态框，包含复杂度提示与登出重导向逻辑。
   - **增强账户审计**: 提供了上次登录 IP、登录时间等详细审计信息的展示。

4. **UI/UX 精雕细琢**:
   - 优化了 Web 端的毛玻璃 (Glassmorphism) 层级关系。
   - 增加了头像悬浮态提示及上传时的 loading 状态反馈。

### 技术架构
- **API 驱动**: 封装了 `frontend/src/api/user.ts` 模块化处理用户信息变更。
- **状态一致性**: 采用 `Object.assign` 与 Pinia 响应式系统保持前端数据源的单一真相。
- **安全性**: 结合 Sa-Token 的 `StpUtil.getLoginIdAsLong()` 确保越权防护。

---

## 2026-02-04: 个人中心功能实现

### 功能描述
为了提升员工自我管理体验，系统新增了“个人中心” (Profile Center) 模块。这是一个集成了个人资料、职业发展和账号安全的综合性页面。

### 主要改进
1. **全新视图 `ProfileView.vue`**:
   - 采用玻璃拟态 (Glassmorphism) 设计语言。
   - **职业画像**: 集成人职天数、部门职位、关键绩效指标展示。
   - **技能动态**: 基于 ECharts 实现技能雷达图，可视化展示核心竞争力。
   - **成长记录**: 自动加载成长时间轴，记录员工在公司的每一个里程碑。
   - **安全管理**: 整合账号详情与安全设置。

2. **路由与导航优化**:
   - 在 `router/index.ts` 中新增 `/profile` 路径。
   - 优化 `MainLayout.vue` 侧边栏底部用户区域：
     - 点击头像或昵称可直接跳转至个人中心。
     - 增加“个人中心”快捷按钮 (PersonCircleOutline)。

3. **文档同步**:
   - 在 `docs/USER_MANAGEMENT.md` 中增加了个人中心的使用说明及访问路径。

### 技术要点
- **动态组件**: 结合 Naive UI 的布局组件与 ECharts 增强页面交互感。
- **响应式设计**: 优化了侧边栏在折叠/展开状态下的用户区域显示效果。

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
