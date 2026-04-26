# Metrics Tool Workbench

[中文说明](README.zh-CN.md)

## Core Mainlines

The course project is explicitly converged to three mainlines:

- code metrics
- design diagram metrics
- project estimation

## Environment

- JDK 17
- Maven 3.9+
- Node.js 24
- Python 3.11 (for `diagram-recognition-service`)

## Desktop Usage

The product is now delivered as a desktop application. Launch it from the Electron shell or build the Windows installer.

## Desktop Development

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\dev.ps1
```

The script reuses `metrics-backend\target\metrics-backend-0.0.1-SNAPSHOT.jar` when it already exists; otherwise it builds the backend jar first, then starts:

- the renderer dev server in `metrics-frontend`
- the Electron shell in `metrics-desktop`

## Renderer Build

```powershell
Set-Location .\metrics-frontend
npm run build
```

## Desktop Verification

```powershell
Set-Location .\metrics-frontend
npm test -- --run
npm run build

Set-Location ..\metrics-desktop
npm run test
```

## Installer Build

```powershell
powershell -ExecutionPolicy Bypass -File .\metrics-desktop\scripts\build-installer.ps1
```

## Course Defense References

- pasted code analysis
- single file analysis
- multiple file analysis
- folder scan
- design metrics workspace for class diagrams, use-case diagrams, and flow diagrams
- project estimation workspace with manual estimation and use case point estimation
- project, class, method, and LK summary metrics
- CSV and Markdown export

## Course Requirement Mapping

| 课程要求 | 当前功能 | 对应界面 / 接口 |
| --- | --- | --- |
| 代码阶段度量 | Java AST 分析，覆盖 `WMC / CBO / RFC / LCOM / DIT / NOC` 与 `LOC / 圈复杂度 / 最大嵌套深度 / 分支数` | Desktop `代码度量` 工作区；`/api/metrics/analyze/text` |
| LK 与 CK 相关度量点 | LK 结果区块展示平均新增方法数、平均覆写方法数、最大特化指数、有继承关系的类数量 | Desktop `LK 指标视图`；`codeMetrics.lkSummary` |
| 设计阶段度量 | 支持 `class / use-case / flow` 手工输入，OCR 仅作辅助建议 | Desktop `设计度量` 工作区；`/api/metrics/design/analyze`、`/api/metrics/design/suggest` |
| 设计结果课程化表达 | 结果区分原始计数、派生指标、OCR 是否参与；类图展示 `relationshipDensity`，用例图展示 `useCasesPerActor` | Desktop `设计度量结果`；`designMetrics.relationshipDensity / useCasesPerActor` |
| 项目估算 | 展示 `LoC / 人员 / 工期 / 成本 / 工作量（人月）`，并给出生产率与单位 LoC 成本 | Desktop `项目估算 -> 人工估算`；`/api/metrics/estimation/manual` |
| 额外度量族 | 新增 `Use Case Points`，输出 `UAW / UUCW / UUCP / UCP` | Desktop `项目估算 -> 用例点估算`；`/api/metrics/estimation/use-case-points` |
| 报告导出 | Markdown 报告独立章节包含 `LK Metrics / Design Metrics / Project Estimation / Use Case Points` | Desktop 导出按钮；renderer `exporters.js` |
