# 校园智慧农场平台

当前仓库提供一套可运行的 MVP：Express 后端模拟统一设备接入 API，React/Vite 前端实现登录、总览、光伏、灌溉控制、气象、环境和监控模块。外部平台密钥仅应由后端接入层管理，当前数据为演示数据。

## 运行

```bash
npm install
npm run dev
```

浏览器访问 `http://localhost:5173`。

演示账号：`admin / admin123`、`farmer / farm123`、`viewer / view123`。

后端 API 默认运行在 `http://localhost:8080`，主要接口为 `/api/auth/login`、`/api/overview`、`/api/zones`、`/api/irrigation/:id/:action` 和 `/api/audit`。生产环境应将 `server/index.js` 中的模拟数据替换为 Spring Boot 服务及 FusionSolar/Hydrawise 官方接入实现。

`backend/` 是对应的 Spring Boot 2.7（Java 8）服务，默认 H2 内存数据库用于本地验证，生产部署可通过 `DB_URL`、`DB_USERNAME`、`DB_PASSWORD` 环境变量切换 MySQL。进入 `backend/` 后执行 `mvn spring-boot:run` 即可启动（需安装 JDK 8 和 Maven）。当前外部设备接入采用统一服务边界，待拿到厂商授权后实现 FusionSolar/Hydrawise client、定时同步、重试和同步日志。
