# 课程答辩演示手册

> 目标：把当前项目收敛成一条稳定、可重复、可直接照着操作的课程答辩演示路径。

## 1. 演示目标

建议把现场演示压缩成 5 个连续动作：

1. 展示三条主线已经贯通：代码度量、设计图度量、项目估算。
2. 展示代码度量支持面向对象指标、LK 课程口径、传统复杂度与 LoC。
3. 展示设计图既支持结构化输入，也支持图片识别输入。
4. 展示项目估算同时支持 `ucp` 与 `function_point` 两条路径。
5. 展示统一导出报告，证明结果可追溯、可写入课程报告。

## 2. 演示前检查

### 2.1 推荐提前打开的资料

- [课程证据索引](./course-evidence-index.zh-CN.md)
- [课程要求对照矩阵](./course-requirement-matrix.zh-CN.md)
- [LK 课程口径映射](./lk-course-alignment.zh-CN.md)
- [指标定义与公式](./metric-definitions.md)
- [项目估算方法](./estimation-method.md)

### 2.2 首次环境准备

在项目根目录执行：

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\bootstrap-models.ps1 -SkipTests
```

该步骤会修复 `diagram-recognition-service/.venv311` 并预热 OCR 模型。

### 2.3 开发态启动方式

先启动 Python 识别服务：

```powershell
cd .\diagram-recognition-service
.\.venv311\Scripts\python.exe -m uvicorn app.main:app --host 127.0.0.1 --port 8090
```

再在项目根目录启动前后端：

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\dev.ps1
```

启动后确认：

- 前端：`http://localhost:5173`
- 后端：`http://localhost:8080`
- 识别服务：`http://localhost:8090`
- 页面顶部状态显示：
  - `Code backend: UP`
  - `Recognition service: UP / READY`

### 2.4 演示包模式（可选）

如果答辩环境更适合直接运行 Jar，可使用：

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\build-demo.ps1
powershell -ExecutionPolicy Bypass -File .\scripts\run-demo.ps1
powershell -ExecutionPolicy Bypass -File .\scripts\smoke-demo.ps1
```

其中 `smoke-demo.ps1` 会对 `http://localhost:8080/api/metrics/health` 做快速检查。

## 3. 启动顺序

建议现场始终按这个顺序操作：

1. 识别服务
2. 后端 + 前端
3. 打开前端首页，先展示 `Three Mainlines`
4. 再按“代码度量 -> 设计图 -> 项目估算 -> 导出”顺序演示

这样老师先建立全局认知，后面每一步就不会显得像“临时拼出来的功能点”。

## 4. 推荐演示主路径

### 步骤 1：先展示整体结构

- 页面位置：首页顶部 `Three Mainlines`
- 要点：
  - `Mainline 1` 是 AST 驱动的代码度量
  - `Mainline 2` 是结构化设计图 + 图片识别设计图
  - `Mainline 3` 是项目估算
- 对应课程条款：总体要求

### 步骤 2：代码度量

- 输入方式：`Multiple Files`
- 样例：`samples/ui-demo-inputs/multiple-files/`
- 操作：
  - 上传该目录下全部 `.java` 文件
  - 点击分析
- 重点展示：
  - `OverviewCards` 里的项目级规模指标
  - `LK Course-Aligned View`
  - `MetricsTables` 中的 `CK` 指标
  - `MetricsCharts` 中的复杂度趋势
- 推荐话术：
  - “这里同时覆盖了面向对象度量、LK 课程口径展示，以及圈复杂度、LoC 等传统指标。”
  - “Java 代码解析底层使用 Eclipse JDT `ASTParser`，不是简单正则统计。”
- 对应课程条款：总体要求、`(1)`、`(3)`、`(4)`、`(5)`

### 步骤 3：结构化设计图度量

- 输入方式：`Structured Input`
- 推荐样例：
  - 类图：`samples/diagram-inputs/structured/class/library-domain.puml`
  - 流程图：`samples/diagram-inputs/structured/flow/order-approval.mmd`
  - 用例图：`samples/diagram-inputs/structured/usecase/campus-repair.puml`
- 推荐现场优先展示：`library-domain.puml`
- 重点展示：
  - `Diagram Type`
  - `Source: structured`
  - 识别出的节点、关系、度量结果
- 推荐话术：
  - “结构化设计图路径不依赖 OCR，适合稳定展示图关系抽取和图度量。”
  - “这一步证明工具不仅分析代码，也能分析设计阶段软件实体。”
- 对应课程条款：总体要求、`(2)`、`(4)`

### 步骤 4：图片设计图识别

- 输入方式：`Image Input`
- 推荐样例：
  - 类图：`samples/diagram-inputs/images/class/library-domain-zh-en.png`
  - 流程图：`samples/diagram-inputs/images/flow/order-approval-zh-en.jpg`
  - 用例图：`samples/diagram-inputs/images/usecase/campus-repair-zh-en.png`
- 推荐现场优先展示：`library-domain-zh-en.png`
- 重点展示：
  - `Source: image`
  - `Confidence`
  - 图片输入经过识别后仍然能进入统一的度量结果链路
- 推荐话术：
  - “这里展示的是真实 image recognition pipeline，不是只支持结构化文本。”
  - “如果 OCR 置信度偏低，系统会保留置信度提示，答辩时可以解释这是工程上真实的可信边界。”
- 对应课程条款：总体要求、`(2)`、`(4)`

### 步骤 5：项目估算，先走 UCP

- 页面位置：`Mainline 3: Project Estimation`
- 操作建议：
  - 保持前面代码度量自动回填的 `Total LoC / Class Count / Relationship Count`
  - `Estimation Method` 选择 `ucp`
  - 可填写一组稳定示例：
    - `Use Case Count = 6`
    - `Simple Actor Count = 1`
    - `Average Actor Count = 2`
    - `Complex Actor Count = 1`
    - `Simple Use Case Count = 2`
    - `Average Use Case Count = 3`
    - `Complex Use Case Count = 1`
    - `Technical Complexity Factor = 1.05`
    - `Environmental Factor = 0.95`
- 重点展示：
  - `workloadPersonMonths`
  - `cost`
  - `scheduleMonths`
  - `suggestedStaffing`
  - `ucpBreakdown`
- 推荐话术：
  - “这里不是只给单一估算值，而是把工作量、成本、工期、建议人员数一起输出。”
- 对应课程条款：`(2)`、`(3)`、`(4)`

### 步骤 6：项目估算，再切换到 Function Point

- 操作建议：
  - `Estimation Method` 切换为 `function_point`
  - 演示两种方式任选其一：
    - 留空 FP 细项，让后端基于当前项目指标做简化推导
    - 或手动填写：
      - `External Input Count = 6`
      - `External Output Count = 4`
      - `External Inquiry Count = 3`
      - `Internal Logical File Count = 2`
      - `External Interface File Count = 1`
      - `Value Adjustment Factor = 1.00`
- 重点展示：
  - `functionPointBreakdown`
  - 估算结果会随方法切换而变化
- 推荐话术：
  - “这一步证明项目估算主线不是只有 UCP，一共支持至少两种明确命名的方法。”
- 对应课程条款：`(2)`、`(3)`、`(4)`

### 步骤 7：统一导出报告

- 页面位置：`Unified Export`
- 操作：
  - 点击 `Export Markdown` 或 `Export CSV`
- 重点展示：
  - 代码度量、设计图结果、项目估算会统一进入同一份导出
  - 导出内容包含：
    - `LK Course-Aligned View`
    - `Function Point Breakdown`
- 推荐话术：
  - “这一步说明项目结果不只是页面展示，还能沉淀成报告证据，用于课程文档和答辩材料。”
- 对应课程条款：总体要求、`(1)`、`(2)`、`(3)`

## 5. 时间不足时的最小通过路径

如果现场时间只够 3 到 5 分钟，建议保留这 4 步：

1. 代码度量：上传 `samples/ui-demo-inputs/multiple-files/`
2. 结构化设计图：分析 `samples/diagram-inputs/structured/class/library-domain.puml`
3. 项目估算：先跑 `ucp`，再切到 `function_point`
4. 导出报告：导出 Markdown

这条最小路径已经能覆盖：

- 面向对象度量
- 设计阶段图度量
- 项目估算
- 多方法并存
- 报告输出

## 6. 运行异常时的兜底路径

### 6.1 图片识别异常

如果图片识别服务没有正常返回：

1. 先展示 `Structured Input`
2. 再展示页面顶部的 `Recognition service` 状态
3. 说明图片识别链路已集成，但运行依赖 OCR 模型与本地 Python 环境
4. 用 [课程证据索引](./course-evidence-index.zh-CN.md) 指向图片识别相关接口、脚本和测试证据

### 6.2 文件上传不方便

如果现场环境不方便选择文件夹：

1. 代码度量改用 `Code Input` 粘贴 `samples/ui-demo-inputs/code-input/CodeInputSample.java`
2. 或改用 `Single File` 上传 `samples/ui-demo-inputs/single-file/SingleFileDemo.java`
3. 同时打开 [samples/ui-demo-inputs/README.md](../samples/ui-demo-inputs/README.md) 说明系统支持四种代码输入模式

## 7. 答辩问答快速索引

### 如果老师问“课程要求逐条是怎么对上的？”

打开：[课程要求对照矩阵](./course-requirement-matrix.zh-CN.md)

### 如果老师问“LK 是不是只是你们自己包装出来的展示层？”

打开：[LK 课程口径映射](./lk-course-alignment.zh-CN.md)

### 如果老师问“具体公式和指标定义在哪里？”

打开：

- [指标定义与公式](./metric-definitions.md)
- [项目估算方法](./estimation-method.md)

### 如果老师问“有哪些证据能支撑你们刚才的演示？”

打开：[课程证据索引](./course-evidence-index.zh-CN.md)
