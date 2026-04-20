# Metrics Tool 工作台（中文）

[English README](README.md)

## 三条主线

课程项目能力明确收敛为三条主线：

- 代码度量
- 设计图度量
- 项目估算

## 概览

本项目是一个软件度量与设计图分析的综合工具，包含：

- `metrics-backend`：Java / Spring Boot 后端（对外提供统一 API）
- `metrics-frontend`：Vue 前端工作台（页面操作与结果展示）
- `diagram-recognition-service`：Python / FastAPI 识别服务（结构化与图片识别管线）

## 环境要求

- JDK 17
- Maven 3.9+
- Node.js 24
- Python 3.11（用于 `diagram-recognition-service`）

## 快速开始

### 1. 初始化/修复识别运行时（首次必做）

在项目根目录执行：

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\bootstrap-models.ps1 -SkipTests
```

该脚本会创建/修复 `diagram-recognition-service/.venv311`，并将 OCR 相关模型预热到 `models/` 缓存目录。

### 2. 启动 Python 识别服务（端口 8090）

在新窗口进入识别服务目录并启动：

```powershell
cd .\diagram-recognition-service
.\.venv311\Scripts\python.exe -m uvicorn app.main:app --host 127.0.0.1 --port 8090
```

后端默认通过 `http://localhost:8090` 调用识别服务；如需修改端口，请同步更新 `metrics-backend/src/main/resources/application.yml` 中的 `recognition.base-url`。

### 3. 启动后端 + 前端（开发模式）

在项目根目录执行：

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\dev.ps1
```

- 后端：`http://localhost:8080`
- 前端：`http://localhost:5173`

## API 说明（后端统一入口）

- 代码度量：
  - `POST /api/metrics/analyze/text`
  - `POST /api/metrics/analyze/files`
  - `POST /api/metrics/analyze/folder`
- 设计图分析：
  - `POST /api/design/analyze/structured`
  - `POST /api/design/analyze/image`
- 识别状态：
  - `GET /api/recognition/health`
  - `GET /api/recognition/models/status`
- 项目估算：
  - `POST /api/estimate/project`

## 测试命令

### Python（识别服务）

```powershell
cd .\diagram-recognition-service
.\.venv311\Scripts\python.exe -m pytest tests\test_runtime_environment.py -q
.\.venv311\Scripts\python.exe -m pytest tests -q --basetemp "C:\Users\<user>\AppData\Local\Temp\codex-pytest\diagram-recognition-service"
```

说明：在部分 Windows 环境下，pytest 默认临时目录可能触发权限问题，建议使用可写的 `--basetemp` 目录。

### Java（后端）

```powershell
mvn -f .\metrics-backend\pom.xml -q test
```

### 前端

```powershell
cd .\metrics-frontend
npm run test -- --run src/components/__tests__/InputWorkspace.test.js src/components/__tests__/DashboardFlow.test.js
```

## Demo 打包与运行（可选）

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\build-demo.ps1
powershell -ExecutionPolicy Bypass -File .\scripts\run-demo.ps1
powershell -ExecutionPolicy Bypass -File .\scripts\smoke-demo.ps1
```

## 配置提示

- 模型缓存目录：
  - 后端配置：`metrics-backend/src/main/resources/application.yml` 中的 `recognition.model-cache-path`
  - 识别服务配置：环境变量 `METRICS_TOOL_MODEL_CACHE`（未设置时会使用默认路径）
  - 如果你将仓库放在非 `D:/Projects/SQA/Metrics_Tool` 路径下，建议显式设置 `METRICS_TOOL_MODEL_CACHE` 并调整后端配置。
