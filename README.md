# Metrics_Tool（桌面端软件度量实验平台）

> 面向课程实验与演示的桌面工具：代码度量、设计度量（含 OCR 辅助）与项目估算一体化工作台。

## 项目简介

`Metrics_Tool` 采用 Electron + Vue + Spring Boot 的桌面架构，提供“输入 -> 分析 -> 结果 -> 导出”的完整流程。  
当前重点是桌面端使用体验，支持在本地直接完成课程常见的软件度量任务，并导出报告用于答辩或提交。

## 主要功能

- 代码度量：支持粘贴源码、单文件、多文件、文件夹扫描
- 指标覆盖：`WMC / CBO / RFC / LCOM / DIT / NOC / LOC / 圈复杂度 / 最大嵌套深度 / 分支数`
- LK 指标展示：包含新增方法、覆写方法、特化指数等摘要
- 设计度量：支持 `class / use-case / flow` 三类图
- 设计 OCR：上传图片后可给出建议指标（支持自动识别链路）
- 项目估算：人工估算与用例点（UCP）估算
- 报告导出：CSV 与 Markdown

## 技术栈与环境要求

- JDK 17
- Maven 3.9+
- Node.js 24
- Windows PowerShell（推荐）

## 快速开始（开发模式）

在仓库根目录执行：

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\dev.ps1
```

该脚本会：

1. 检查并按需构建后端 `jar`
2. 启动 `metrics-frontend` 开发服务
3. 启动 `metrics-desktop` Electron 桌面壳

## 常用命令

### 前端构建

```powershell
Set-Location .\metrics-frontend
npm run build
```

### 前端测试 + 构建验收

```powershell
Set-Location .\metrics-frontend
npm test -- --run
npm run build
```

### 桌面端测试

```powershell
Set-Location ..\metrics-desktop
npm run test
```

### 打包安装程序

```powershell
powershell -ExecutionPolicy Bypass -File .\metrics-desktop\scripts\build-installer.ps1
```

## 设计度量 OCR 说明

- OCR 作为设计度量的辅助输入能力，不影响手工录入主流程
- 本地运行时目录（如 `.ocr311`、`models/*`）为环境资源，不建议提交到 Git
- 若 OCR 环境缺失，系统应退化为手工录入并保持其他功能可用

## 目录结构（核心）

```text
metrics-backend/    # Spring Boot 后端与分析逻辑
metrics-frontend/   # Vue 前端界面
metrics-desktop/    # Electron 桌面壳与运行时桥接
scripts/            # 开发启动脚本
docs/               # 课程文档与设计说明
```

## 课程要求映射（简表）

| 课程要求 | 当前能力 | 对应入口 |
| --- | --- | --- |
| 代码阶段度量 | Java AST 指标分析与风险提示 | 代码度量工作区 |
| 设计阶段度量 | 类图 / 用例图 / 流程图计数与派生指标 | 设计度量工作区 |
| 项目估算 | 人工估算 + UCP 估算 | 项目估算工作区 |
| 结果可交付 | CSV / Markdown 导出 | 结果区导出按钮 |

## 许可与说明

本仓库用于教学与实验用途。若用于正式项目，请根据课程与组织规范补充许可证、隐私和合规说明。
