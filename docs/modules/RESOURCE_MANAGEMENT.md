# 行政资源管理模块 (Resource Management)

## 1. 模块概述
行政资源管理模块旨在解决企业内部会议室、公用设备等资源的调度与预约问题。通过直观的日历视图展示资源占用情况，并提供冲突检测机制。

## 2. 核心功能
- **资源概览**：侧边栏展示所有可用资源（会议室、设备）及其基本信息（容量、地点、设施）。
- **日历看板**：支持以日历形式查看各日期的预约明细。
- **冲突检测**：在预约时自动检查所选资源在目标时间段内是否已被占用。
- **我的预约**：用户可查看并取消自己发起的预约记录。
- **系统通知**：预约成功后，系统会自动推送通知给预约人。

## 3. 数据库设计
### 3.1 行政资源表 (`sys_resource`)
| 字段 | 类型 | 说明 |
| --- | --- | --- |
| id | BIGINT | 主键 |
| name | VARCHAR | 资源名称 |
| capacity | INT | 容纳人数 |
| location | VARCHAR | 地点描述 |
| facilities | VARCHAR | 设施详情 (投影仪等) |
| status | TINYINT | 状态 (1可用 0停用) |

### 3.2 资源预约表 (`sys_resource_booking`)
| 字段 | 类型 | 说明 |
| --- | --- | --- |
| id | BIGINT | 主键 |
| resource_id | BIGINT | 关联资源ID |
| user_id | BIGINT | 预约人ID |
| title | VARCHAR | 会议主题/用途 |
| start_time | DATETIME | 开始时间 |
| end_time | DATETIME | 结束时间 |
| status | TINYINT | 状态 (1预约 0取消) |

## 4. 关键接口
- `GET /api/v1/resource/list`: 获取所有资源。
- `GET /api/v1/resource/bookings`: 获取全量预约记录（包含联表信息）。
- `POST /api/v1/resource/booking`: 发起预约（含冲突校验）。
- `DELETE /api/v1/resource/booking/{id}`: 取消预约。

## 5. 交互规范
- **玻璃拟态卡片**：采用半透明毛玻璃效果展示侧边栏。
- **日历沉浸式体验**：Calendar 组件占据核心区域，支持点击日历格子直接触发详情查看。
- **实时反馈**：预约成功或失败均有全局 Message 提示，并伴有通知中心红点提醒。
