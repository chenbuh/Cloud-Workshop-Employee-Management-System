# UI/UX 视觉与交互规范

## 1. 设计语言定义
本系统采用 **Nebula Glass (星云玻璃)** 设计语言，旨在打破管理系统“灰暗、沉闷、格子化”的固有印象。

### 核心配色 (Color Palette)
- **Primary (主色)**: `#6366f1` (Indigo - 科技感十足的靛蓝色)
- **Success (成功)**: `#10b981` (Emerald)
- **Warning (警告)**: `#f59e0b` (Amber)
- **Dark Background**: `#0f172a` (Deep Slate - 仅在深色模式下使用)
- **Surface**: `rgba(255, 255, 255, 0.7)` (带毛玻璃效果的透明白)

## 2. 交互亮点
### 2.1 侧边栏 (Sidebar)
- **不只是菜单**: 侧边栏底部显示当前登录者的微缩卡片，点击可快速切换状态。
- **悬浮动态**: 选中项不仅有背景色变化，还会有一个细长的光条随滚动跳动。

### 2.2 卡片容器 (Cards)
- **无边框阴影**: 移除 `border`，改用 `box-shadow: 0 10px 15px -3px rgba(0, 0, 0, 0.1)`。
- **玻璃化**: 在卡片内部使用 `backdrop-filter: blur(8px)`。

### 2.3 列表与表格
- **行间距**: 相比 Ruoyi 的紧凑列表，我们的表格行高更大 (`row-height: 64px`)。
- **操作列**: 平时隐藏，Hover 行时才以渐入效果显示，减少视觉干扰。

### 2.4 数据可视化 (Data Viz)
- **图表**: 禁用百度 Echarts 的默认边框，统一使用由 `Canvas` 绘制的、带阴影的渐变面积图。
- **趋势箭头**: 上升、持平、下降均有动态微动画。

## 3. Naive UI 配置要点 (Theme Overrides)
我们将通过 Naive UI 的 `NConfigProvider` 注入自定义全局主题：
- `borderRadius: "12px"`
- `fontSize: "15px"`
- `common: { primaryColor: "#6366f1" }`
- `Layout: { color: "transparent" }` (配合背景渐变)
