启动前后端开发服务器。

## 操作步骤

1. 先停止已有的 Java 进程（后端）
2. 启动后端 Spring Boot（MySQL 默认 profile）
3. 启动前端 Vite 开发服务器
4. 验证两个服务都正常运行

## 后端启动命令

```bash
cd d:/xm/AI/claudedemo/src/backend
JAVA_TOOL_OPTIONS="-Dfile.encoding=UTF-8" ./mvnw spring-boot:run
```

- 端口：8080
- 数据库：MySQL（默认 mysql profile）
- 启动后通过 `curl http://localhost:8080/api/auth/login` 验证

## 前端启动命令

```bash
cd d:/xm/AI/claudedemo/src/frontend
npm run dev
```

- 端口：3000
- 确保 `.env.local` 中 `VITE_ENABLE_MOCK=false`（对接真实后端）

## 验证

- 后端：`curl -s http://localhost:8080/api/auth/login -X POST -H "Content-Type: application/json" -d '{"username":"admin","password":"123456"}'` 应返回 token
- 前端：`curl -s -o /dev/null -w '%{http_code}' http://localhost:3000/` 应返回 200

## 注意事项

- Windows 环境必须设置 `JAVA_TOOL_OPTIONS="-Dfile.encoding=UTF-8"` 否则中文乱码
- 后端和前端应并行启动（后端放后台），后端约需 10-15 秒启动完成
- 启动前先用 `powershell.exe -Command "Stop-Process -Name java -Force -ErrorAction SilentlyContinue"` 停掉旧的 Java 进程
