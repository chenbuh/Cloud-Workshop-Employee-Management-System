# 绩效与人才盘点 (Performance Management)

## 概述
本模块旨在对员工进行周期性的绩效考核，并基于绩效结果进行人才盘点（九宫格分析）。

## 功能特性

### 1. 周期管理
- **开启新周期**: 管理员可设定绩效考核的时间范围（如：2023 Q4）。
- **状态管理**: 包含 `进行中`, `已归档` 等状态。

### 2. 绩效评价
- **新增/编辑评价**:
  - **对象**: 选择员工。
  - **绩效评分**: 0-100 分。
  - **潜力维度**: 1-5 星（用于九宫格 Y 轴）。
  - **综合评级**: S (卓越), A (优秀), B (良好), C (合格), D (需改进)。
  - **评价反馈**: 具体的文本反馈。

### 3. 数据可视化 (Talent Mapping)
- **人才九宫格 (9-Box Grid)**:
  - **X 轴**: 绩效 (Performance) - 基于评分 (Score)。
  - **Y 轴**: 潜力 (Potential) - 基于潜力星级 (Potential Score)。
  - **分布区域**:
    - **右上 (High/High)**: 明星人才 (Star) - 重点激励与培养。
    - **左上 (Low/High)**: 潜力股 (Potential) - 需要更多历练。
    - **右下 (High/Low)**: 熟练工 (Performer) - 稳定贡献者。
    - **左下 (Low/Low)**: 待调整 (Underperformer) - 需要改进或淘汰。
- **绩效排行榜**: 实时展示高绩效员工。

## 数据库
- **`sys_performance_cycle`**: 考核周期表。
- **`sys_performance_appraisal`**: 具体的考核记录表。

## API 接口
- `GET /api/v1/performance/cycles`: 获取周期列表。
- `POST /api/v1/performance/cycles`: 创建新周期。
- `GET /api/v1/performance/appraisals?cycleId=xx`: 获取某周期下的所有评价详情。
- `POST /api/v1/performance/appraisals`: 提交或更新评价。
