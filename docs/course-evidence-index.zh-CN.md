# 课程证据索引

> 用途：把“课程要求 -> 页面 / API / 样例 / 文档 / 测试”的证据链压缩成一张可直接用于答辩和报告引用的索引表。

## 证据索引表

| 课程条款 | 页面 / 工作流 | API / 合同 | 样例输入 | 文档证据 | 测试证据 |
| --- | --- | --- | --- | --- | --- |
| 总体要求：开发一个中小型软件度量自动化工具，覆盖主要课程知识，支持跨阶段度量 | `metrics-frontend/src/App.vue` 的 `Three Mainlines`、代码度量区、设计图度量区、项目估算区、统一导出区 | `MetricsController.java`、`DesignAnalysisController.java`、`EstimationController.java` | `samples/ui-demo-inputs/`、`samples/diagram-inputs/` | `README.zh-CN.md`、`docs/course-defense-demo.zh-CN.md`、`docs/course-requirement-matrix.zh-CN.md` | `DashboardFlow.test.js`、`InputWorkspace.test.js` |
| `(1)` 面向对象度量为主，实现 LK 与 CK，并加入圈复杂度、LoC 等传统度量 | `LkMetricsPanel.vue`、`MetricsTables.vue`、`MetricsCharts.vue`、代码分析结果区 | `POST /api/metrics/analyze/text`、`POST /api/metrics/analyze/files`、`POST /api/metrics/analyze/folder`、`CodeMetricsResult.java`、`ClassMetrics.java`、`MethodMetrics.java` | `samples/ui-demo-inputs/multiple-files/`、`samples/ui-demo-inputs/folder-scan/` | `docs/lk-course-alignment.zh-CN.md`、`docs/metric-definitions.md`、`docs/course-defense-demo.zh-CN.md` | `MetricsAnalysisServiceTest.java`、`CodeMetricsResultTest.java`、`DashboardFlow.test.js` |
| `(2)` 可加入其它度量方法：功能点、用例度量、复杂性度量等 | 项目估算区的 `ucp` / `function_point` 方法切换；设计图分析区的结构化与图片度量路径 | `POST /api/estimate/project`、`EstimationService.java`、`FunctionPointBreakdown.java`、结构化图指标抽取链路 | `samples/diagram-inputs/structured/usecase/campus-repair.puml`、`samples/diagram-inputs/images/class/library-domain-zh-en.png` | `docs/estimation-method.md`、`docs/metric-definitions.md`、`docs/course-defense-demo.zh-CN.md` | `EstimationControllerTest.java`、`EstimationServiceBehaviorTest.java`、`DesignAnalysisControllerTest.java`、`DesignAnalysisServiceTest.java`、`exporters.test.js` |
| `(3)` 度量实体包括 LoC、工作量、成本、开发时间、人员等 | 代码结果概览区 + 项目估算结果区 + 导出区 | `ProjectSummary.java`、`ProjectEstimation.java`、`EstimateProjectRequest.java` | `samples/ui-demo-inputs/multiple-files/`，随后在估算区填写参数 | `docs/metric-definitions.md`、`docs/estimation-method.md`、`docs/course-defense-demo.zh-CN.md` | `EstimationServiceBehaviorTest.java`、`EstimationControllerTest.java`、`exporters.test.js` |
| `(4)` 输入项包括类图、流程图、用例图、程序代码、用户输入等 | `InputWorkspace.vue` 的 4 种代码输入模式；设计图分析区的 `Structured Input` / `Image Input`；项目估算表单 | `MetricsController.java`、`DesignAnalysisController.java`、`EstimateProjectRequest.java` | `samples/ui-demo-inputs/`、`samples/diagram-inputs/structured/class/library-domain.puml`、`samples/diagram-inputs/structured/flow/order-approval.mmd`、`samples/diagram-inputs/structured/usecase/campus-repair.puml`、对应图片样例 | `samples/ui-demo-inputs/README.md`、`docs/course-defense-demo.zh-CN.md`、`README.zh-CN.md` | `InputWorkspace.test.js`、`DesignAnalysisControllerTest.java`、`RecognitionStatusControllerTest.java` |
| `(5)` Java 程序建议利用 Eclipse ASTParser 获得 AST 后再统计分析 | 代码度量主线与 LK / CK / 复杂度结果展示区 | `JavaSourceParser.java`、`MetricsAnalysisService.java`、`ClassMetricsAnalyzer.java`、`MethodMetricsAnalyzer.java` | `samples/ui-demo-inputs/code-input/CodeInputSample.java`、`samples/ui-demo-inputs/multiple-files/` | `docs/course-requirement-matrix.zh-CN.md`、`docs/course-defense-demo.zh-CN.md` | `MetricsAnalysisServiceTest.java`、`MetricsControllerMultipartTest.java` |

## 使用建议

1. 现场演示时，优先按 [课程答辩演示手册](./course-defense-demo.zh-CN.md) 的主路径走，不要临时切来切去。
2. 被追问“课程要求到底对应到哪里”时，直接打开本页，从“课程条款”列往右读，答辩会很稳。
3. 被追问 `LK`、`function_point`、公式细节时，跳转到 [LK 课程口径映射](./lk-course-alignment.zh-CN.md)、[指标定义与公式](./metric-definitions.md)、[项目估算方法](./estimation-method.md)。
4. 被追问“是否真的做过验证”时，优先引用本页最后一列中的测试文件。
