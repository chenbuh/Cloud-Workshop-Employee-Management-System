# 云工坊员工管理系统 (Cloud Workshop EMS)

[![GitHub stars](https://img.shields.io/github/stars/chenbuh/Cloud-Workshop-Employee-Management-System?style=flat-square)](https://github.com/chenbuh/Cloud-Workshop-Employee-Management-System/stargazers)
[![License](https://img.shields.io/github/license/chenbuh/Cloud-Workshop-Employee-Management-System?style=flat-square)](LICENSE)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.0.5-brightgreen?style=flat-square)](https://spring.io/)
[![Vue](https://img.shields.io/badge/Vue-3.5-blue?style=flat-square)](https://vuejs.org/)

云工坊员工管理系统（Cloud Workshop EMS）是一款面向现代化企业的全生命周期 HR 管理平台。我们拒绝传统管理系统（如若依等）的沉重感，通过 **玻璃拟态 (Glassmorphism)** 设计语言，将复杂的人力资源管理转化为流畅、透明且极具未来感的数字体验。

🔗 **项目地址**: [https://github.com/chenbuh/Cloud-Workshop-Employee-Management-System](https://github.com/chenbuh/Cloud-Workshop-Employee-Management-System)

---

## ✨ 核心特性

### 1. 极致视觉体验
- **玻璃拟态设计**: 背景模糊（Backdrop-filter）、动态发光阴影与柔和渐变色板，打造真正意义上的“下一代”办公界面。
- **微交互反馈**: 按钮、卡片、导航项均配备丝滑的 UI 动效，让每一次点击都有温度。

### 2. 核心业务全覆盖
- **智慧仪表盘**: 集成 ECharts 5，实时展示员工增长趋势、部门分布热力图以及关键 KPI（入职数、待办、预支出）。
- **员工黄金档案**: 侧滑式档案详情页，涵盖基本信息、薪资历程、合同变更及成长轨迹。
- **动态组织架构**: 交互式拓扑树状图，支持拖拽缩放，直观管理企业层级。
- **财务结算中心**: 自动化薪资核算，支持考勤自动扣款对账、一键生成 PDF/Excel 工资明细、站内信实时发放通知。
- **企业知识库**: 集成 Markdown 编辑与渲染，管理公司规章、制度流转及技术规范。

### 3. 企业级安全基石
- **权限管理 (RBAC)**: 基于 **Sa-Token** 的权限模型，支持多端登录验证、自动续期及按钮级组件权限拦截。
- **全方位审计**: 基于 AOP 的日志切面，精准记录所有敏感操作（如调薪、删人），并支持详情回溯。

---

## 🏗️ 技术架构

### 前端开发栈
- **核心框架**: Vue 3 (Composition API)
- **构建工具**: Vite (秒级冷启动体验)
- **状态管理**: Pinia (轻量、TS 友好)
- **UI 组件库**: Naive UI (极简审美，完美契合玻璃拟态)
- **图表引擎**: ECharts 6 + Number Animation
- **工具链**: Axios, Vue Router, Moment.js, Markdown-it

### 后端技术栈
- **基础框架**: Spring Boot 3.0.5
- **ORM & 数据源**: MyBatis-Plus 3.5+, MySQL 8.0, Redis
- **安全认证**: Sa-Token 1.34+ (极速上手，功能强大)
- **辅助工具**: Lombok, FastJSON2, Hutool, EasyExcel

---

## 📁 目录结构说明

```text
├── backend                # Spring Boot 后端源码
│   └── src/main/java      # 核心逻辑 (Controller, Service, Mapper, Entity)
├── frontend               # Vue 3 前端源码
│   ├── src/api            # 全局 API 接口定义
│   ├── src/layout         # 玻璃拟态布局组件
│   ├── src/store          # Pinia 状态管理
│   └── src/views          # 业务逻辑页面 (Dashboard, Employee, Salary, etc.)
├── docs                   # 项目文档中心
│   ├── db                 # SQL 脚本 (含结构与初始数据)
│   ├── api                # RESTful 接口规格
│   └── PROJECT_PLAN.md    # 开发规划与功能路线图
└── uploads                # 系统附件上传根目录
```

---

## 🛠️ 快速部署指南

### 第一步：获取源码
```bash
git clone https://github.com/chenbuh/Cloud-Workshop-Employee-Management-System.git
cd Cloud-Workshop-Employee-Management-System
```

### 第二步：环境准备
- **Java**: JDK 17 或以上
- **MySQL**: 8.0.x
- **Redis**: 任意版本即可
- **Node.js**: 16.x +
- **IDE**: IntelliJ IDEA (推荐) + VS Code

### 第三步：数据库初始化
1. 创建数据库：`CREATE DATABASE cloud_ems CHARACTER SET utf8mb4;`
2. 运行 `docs/db/schema.sql` (创建表结构)
3. 运行 `docs/db/init_full.sql` (插入演示数据及初始权限)

### 第四步：启动后端
1. 进入 `backend` 目录，检查 `application.yml` 中的数据库配置（宿主机 IP、账号、密码）。
2. 运行 `mvn clean install`。
3. 启动 `com.cloud.employee.EmployeeManagementApplication`。

### 第五步：启动前端
1. 进入 `frontend` 目录。
2. 安装依赖：`npm install`。
3. 开发模式启动：`npm run dev`。
4. 编译构建：`npm run build`。

### 第六步：访问及账号
访问地址：`http://localhost:5173`

**超级管理员登录信息：**
- **用户名**: `admin`
- **默认密码**: `admin123`

> ⚠️ **注意**: 演示环境密码已通过 BCrypt 加密存储。为确保系统安全，请在首次进入系统后，点击左下角头像旁的“钥匙”按钮修改密码。

---

## � 开源说明
本项目采用 **MIT License**。您可以自由用于二次开发、学习或商业用途，但请保留原项目作者说明。

---

## 🌟 感谢支持
如果您觉得这个项目对您有帮助，欢迎在 GitHub 上点一个 **Star**！您的支持是我们持续优化的动力。

- **GitHub**: [Cloud-Workshop-Employee-Management-System](https://github.com/chenbuh/Cloud-Workshop-Employee-Management-System)
