# 薪资与酬劳管理 (Salary & Compensation)

## 概述
本模块负责员工的薪资方案配置以及月度工资单的生成、发放与统计。
系统支持**基本工资 + 津贴 + 绩效 - 考勤扣款**的自动计算模式。

## 1. 薪资方案配置
- **对象**: 已入职的正式员工。
- **配置项**:
  - **基本工资 (Base Salary)**: 岗位基础薪资。
  - **津贴 (Subsidy)**: 餐补、车补、房补等合计。
  - **绩效奖金 (Bonus)**: 预设的绩效基数（实际发放时可调整）。
  - **五险一金 (Insurance)**: 个人承担部分的社保公积金。
- **功能**: 新增、修改、移除配置。

## 2. 月度工资单 (Payroll)
- **生成逻辑**:
  1. 选择发放月份（例如 2023-10）。
  2. 系统自动读取该月所有员工的**薪资配置方案**。
  3. 系统读取该月的**考勤记录** (Attendance)。
     - 迟到/早退: 每次扣除 50 元。
     - 缺勤: 每次扣除 200 元。
  4. 自动生成工资条：`实发 = 基本 + 津贴 + 绩效 - 五险一金 - 考勤扣款`。
- **发放管理**:
  - 生成后默认为“待发放”状态。
  - 管理员点击“发放”后，状态变为“已发放”，并向员工发送站内通知。
- **导出**: 支持导出 Excel 格式的月度薪资明细表。

## 数据库设计
- **`sys_salary` (Configuration)**: 存储员工的默认薪资设定。
- **`sys_payroll` (Transaction)**: 存储每月的实际发放记录。
  - `payroll_month`: 月份 (YYYY-MM)
  - `deduction`: 考勤扣款金额
  - `actual_amount`: 实发金额
  - `status`: 0-草稿, 1-已发放

## API 接口
- `GET /api/v1/salary/list`: 获取配置列表
- `GET /api/v1/payroll/list?month=yyyy-MM`: 获取月度工资单
- `POST /api/v1/payroll/generate?month=yyyy-MM`: 触发本月计算
- `PUT /api/v1/payroll/issue/{id}`: 发放工资
- `GET /api/v1/payroll/export`: 导出 Excel
