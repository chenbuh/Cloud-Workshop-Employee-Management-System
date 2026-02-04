# 开发指南 - 热更新支持

本文档介绍了项目中如何启用和使用热更新功能（Hot Reload/HMR）。

## 前端 (Frontend)

前端项目基于 **Vite** 构建，原生支持 **HMR (Hot Module Replacement)**。

- **工作方式**：当您修改 `.vue`、`.ts` 或 `.css` 文件时，Vite 会仅更新修改的模块，而无需刷新整个页面。
- **如何使用**：
  1. 进入 `frontend` 目录。
  2. 运行 `npm run dev`。
  3. 保持浏览器打开，修改代码后，页面会即时呈现变化。

## 后端 (Backend)

后端项目基于 **Spring Boot**，已集成 **Spring Boot DevTools**。

- **工作方式**：当 classpath 下的文件（如 `.java` 类文件、配置文件）发生变化时，DevTools 会自动重启应用。由于是重启（Restart）而非完整的冷启动，速度会非常快。
- **如何使用**：
  1. 使用 IDE（如 IntelliJ IDEA 或 VS Code）运行后端主类。
  2. 修改代码并**保存**。
  3. 如果在 IDEA 中，请确保开启了 "Build project automatically" 选项，或者手动触发 `Ctrl + F9` (Build Project)。
- **配置详情**：
  - 依赖项：`spring-boot-devtools` 已添加到 `backend/pom.xml`。
  - 自动重启：项目检测到编译后的字节码变化时会自动重启。

## 故障排除

1. **后端不重启？**
   - 检查 IDE 设置，确保启用了保存时自动编译。
   - 检查控制台输出，确认 DevTools 是否已激活。
2. **前端不刷新？**
   - 确认终端中 `npm run dev` 运行正常。
   - 检查浏览器控制台是否有网络连接或模块更新报错。
