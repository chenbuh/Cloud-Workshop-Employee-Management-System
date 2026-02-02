# 考勤管理 (Attendance Management)

## 概述
考勤管理模块包含员工端的日常打卡功能和管理员端的考勤数据统计、修正与报表导出功能。

## 功能特性

### 1. 员工端
- **实时打卡**: 
  - 上班/下班打卡。
  - 实时显示当前时间与日期。
  - 自动记录 IP 地址。
  - 智能判定状态：正常、迟到、早退。
- **考勤历史**: 查看个人历史考勤记录。

### 2. 管理员端 (AttendanceManagement)
- **多维筛选**:
  - 按日期范围筛选。
  - 按员工姓名/工号筛选。
- **数据管理**:
  - **查看**: 包含员工信息、打卡时间、时长、状态、IP。
  - **修正**: 管理员可手动修改考勤时间与状态（用于处理漏打卡、设备故障等情况）。
  - **删除**: 可删除异常记录。
- **报表导出**:
  - **导出 CSV**: 支持一键导出当前筛选条件下的考勤报表，包含 UTF-8 BOM 头，可直接用 Excel 打开。

## 技术实现

### 后端 API
- `POST /attendance/clock-in`: 上班打卡。
- `POST /attendance/clock-out`: 下班打卡。
- `GET /attendance/today`: 获取今日打卡状态。
- `GET /attendance/list`: 分页获取个人考勤。
- `GET /attendance/all`: (管理员) 获取所有考勤数据，支持关联查询员工信息。
- `POST /attendance/update`: (管理员) 修正考勤记录。
- `DELETE /attendance/delete`: (管理员) 删除考勤记录。

### 权限控制
- `system:attendance:list`: 允许查看所有考勤。
- `system:attendance:update`: 允许修正考勤。
- `system:attendance:delete`: 允许删除考勤。

## 数据库
- 表名: `sys_attendance`
- 关联表: `sys_user`, `emp_profile`, `sys_dept`
