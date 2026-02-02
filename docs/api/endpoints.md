# API 接口规范说明

## 1. 基础规范
- **Base URL**: `/api/v1`
- **认证方式**: Header 携带 `Authorization: Bearer <token>` (由 Sa-Token 签发)
- **数据格式**: JSON
- **响应结构**:
```json
{
  "code": 200,
  "msg": "操作成功",
  "data": { ... },
  "timestamp": 1675234567890
}
```

## 2. 鉴权接口 (Auth)
- `POST /auth/login`: 用户登录，返回 Token 及用户信息。
- `POST /auth/logout`: 退出登录，作废当前 Token。
- `GET /auth/info`: 获取当前在线用户信息及路由菜单。

## 3. 组织架构 (Organization)
- `GET /dept/tree`: 获取美化的嵌套部门树结构 (包含各部门实时人数)。
- `POST /dept`: 创建部门。
- `PUT /dept/{id}`: 更新部门信息及负责人。

## 4. 员工全生命周期 (Employee Lifecycle)
- `GET /employee/profile/{id}`: 获取员工“全景画像”，包含基础信息、薪资趋势、KPI 历史。
- `POST /employee/onboard`: 入职流程启动，自动生成工号并发送欢迎邮件。
- `GET /employee/statistics/attrition`: 离职率分析数据 (用于前端图表展示)。
- `POST /employee/transfer`: 调岗/调薪申请。

## 5. 极速搜索与建议 (UX Specialized)
- `GET /search/global?keyword=xxx`: 全局极速搜索，返回员工、部门、功能的组合建议。
- `GET /search/suggest/emp`: 员工选择器专用建议接口，支持首字母/拼音。

## 6. 特色：员工评价 (Performance Evaluation)
- `POST /eval/record`: 提交季度/月度评价。
- `GET /eval/report/{empId}`: 生成可视化的能力雷达图数据。

## 7. 考勤与审批 (Attendance & Approval)
### 考勤打卡 (Attendance)
- `GET /attendance/today`: 获取今日打卡状态及记录。
- `POST /attendance/clock-in`: 上班打卡。
- `POST /attendance/clock-out`: 下班打卡。
- `GET /attendance/list`: 获取当前用户的个人考勤历史 (分页)。
- `GET /attendance/all`: **[Admin]** 获取全员考勤记录，支持按日期/员工筛选。

### 流程审批 (Leave & Approval)
- `GET /leave/list`: 获取申请列表 (支持按状态过滤)。
- `POST /leave/apply`: 提交请假/加班申请。
- `PUT /leave/approve`: **[Admin]** 审批通过或驳回申请。

### 通知中心 (Notification)
- `GET /notify/list`: 获取通知列表。
- `GET /notify/unread`: 获取未读数量。
- `PUT /notify/read/{id}`: 标记单条通知为已读。
- `PUT /notify/read/all`: 标记所有通知为已读。

## 8. 薪资与酬劳 (Salary & Compensation)
### 薪资配置 (Salary)
- `GET /salary/list`: 获取全员薪资配置列表。
- `PUT /salary/update`: 更新员工基本工资与津贴。

### 财务结算 (Payroll)
- `GET /payroll/list`: 获取指定月份的工资单列表 (支持分页/搜索)。
- `POST /payroll/generate`: **[Admin]** 一键生成指定月份的工资单 (自动计算考勤扣款)。
- `PUT /payroll/issue/{id}`: **[Admin]** 发放单人工资并发送通知。
- `GET /payroll/export`: **[Admin]** 导出指定月份的工资报表 (Excel)。
