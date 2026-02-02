# 云工坊员工管理系统 - 日期本地化文档

## 概述
系统已配置全局中文日期显示，所有日期和时间都将使用中文格式。

## 配置说明

### Moment.js 中文本地化
在 `main.ts` 中已全局配置：
```typescript
import moment from 'moment'
import 'moment/dist/locale/zh-cn'

moment.locale('zh-cn')
```

### 日期格式说明

#### 常用格式
- `LL`: 显示为 "2026年2月1日"
- `ll`: 显示为 "2026年2月1日" (简短版)
- `dddd`: 显示为 "星期六"（完整星期）
- `ddd`: 显示为 "周六"（简写星期）
- `MMMM`: 显示为 "二月"（完整月份）
- `MMM`: 显示为 "2月"（简写月份）

#### 组合格式示例
- `LL dddd`: "2026年2月1日 星期六"
- `ll dddd`: "2026年2月1日 星期六"
- `YYYY年M月D日 dddd`: "2026年2月1日 星期六"

### 相对时间显示
```typescript
moment(date).fromNow()  // "3分钟前"、"2小时前"、"5天前"
```

## 已更新的页面

### 1. Dashboard（仪表盘）
- 顶部日期：`LL dddd` → "2026年2月1日 星期六"

### 2. AttendanceView（考勤管理）
- 打卡页面日期：`LL dddd` → "2026年2月1日 星期六"

### 3. MainLayout（主布局）
- 通知时间：使用 `fromNow()` 相对时间
- 显示为："刚刚"、"3分钟前"、"2小时前"

## 其他时间格式保持
- 时间显示：`HH:mm:ss` → "17:30:45"
- 日期显示：`YYYY-MM-DD` → "2026-02-01"
- 日期时间：`YYYY-MM-DD HH:mm` → "2026-02-01 17:30"
- 月度选择：`YYYY-MM` → "2026-02"

## 注意事项
1. 所有新增页面和组件都会自动使用中文日期格式
2. 如需自定义格式，参考上述格式说明
3. Moment.js 文档: https://momentjs.com/docs/#/displaying/
