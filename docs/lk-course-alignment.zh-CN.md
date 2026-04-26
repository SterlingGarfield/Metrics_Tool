# LK 课程口径映射说明（P5）

> 目标：把当前系统中的 `LK metrics` 明确映射为“课程可解释口径”，形成“字段 -> 公式 -> 证据”的闭环。

> 说明：`codeMetrics.lkMetrics` 是当前课程对齐的主契约；`codeMetrics.lkPresentation` 仅作为过渡期兼容字段保留，便于前后端平滑切换。

## 1. 口径边界

- 本项目当前实现的是 **LK 课程对齐展示层**：基于可稳定提取的 AST 与类关系数据，给出课程答辩可复核的 LK 相关指标；其中 `codeMetrics.lkMetrics` 是后端主契约，页面与导出是围绕该契约形成的课程对齐呈现。
- 不夸大为“完整学术版 LK 全指标重建”；对未单列的学术细项，使用现有可追溯字段进行等价或近似说明。

## 2. 当前 LK 字段与计算规则

以下字段由后端统一在 `CodeMetricsResult.LkMetrics` 生成，兼容字段 `CodeMetricsResult.LkPresentation` 暂时保留同构数据：

| 字段 | 计算规则 | 实现位置 |
| --- | --- | --- |
| `classCount` | `ProjectSummary.totalClasses`（无摘要时回退 `classMetrics.size`） | `metrics-backend/src/main/java/com/metrics/model/response/CodeMetricsResult.java` |
| `methodCount` | `ProjectSummary.totalMethods`（无摘要时回退 `methodMetrics.size`） | 同上 |
| `attributeCount` | `Σ classMetrics.noa` | 同上 |
| `relationshipCount` | `Σ classMetrics.cbo`（聚合关系/耦合链接） | 同上 |
| `averageMethodsPerClass` | `methodCount / classCount` | 同上 |
| `averageAttributesPerClass` | `attributeCount / classCount` | 同上 |
| `relationDensity` | `relationshipCount / (classCount * (classCount - 1))`；单类场景定义为 `0.0` | 同上 |
| `inheritanceDepthDistribution` | `sort(classMetrics.dit)` | 同上 |

## 3. 课程 LK 关注点的一一映射

| 课程 LK 关注点 | 系统映射字段 | 证据位置 |
| --- | --- | --- |
| 类规模（类数量） | `lkMetrics.classCount`，兼容字段 `lkPresentation.classCount` | `CodeMetricsResult.java` + `/api/metrics/analyze/*` 输出 |
| 操作规模（方法数量） | `lkMetrics.methodCount`，并可下钻 `classMetrics.nom`，兼容字段 `lkPresentation.methodCount` | `ClassMetrics.java` / `CodeMetricsResult.java` |
| 属性规模（属性数量） | `lkMetrics.attributeCount`，并可下钻 `classMetrics.noa`，兼容字段 `lkPresentation.attributeCount` | `ClassMetrics.java` / `CodeMetricsResult.java` |
| 平均每类操作数 | `lkMetrics.averageMethodsPerClass`，兼容字段 `lkPresentation.averageMethodsPerClass` | `CodeMetricsResult.java` |
| 平均每类属性数 | `lkMetrics.averageAttributesPerClass`，兼容字段 `lkPresentation.averageAttributesPerClass` | `CodeMetricsResult.java` |
| 类间关系/耦合规模 | `lkMetrics.relationshipCount`（来自 `ΣCBO`），兼容字段 `lkPresentation.relationshipCount` | `ClassMetricsAnalyzer.java` / `CodeMetricsResult.java` |
| 关系密度 | `lkMetrics.relationDensity`，兼容字段 `lkPresentation.relationDensity` | `CodeMetricsResult.java` + `CodeMetricsResultTest.java` |
| 继承层次结构 | `lkMetrics.inheritanceDepthDistribution`，下钻 `classMetrics.dit/noc`，兼容字段 `lkPresentation.inheritanceDepthDistribution` | `ClassMetrics.java` / `ClassMetricsAnalyzer.java` |
| 响应规模（课程问到行为响应时） | `classMetrics.rfc`（按类） | `ClassMetrics.java` / `ClassMetricsAnalyzer.java` |
| 复杂度补充（课程允许加入传统点） | `methodMetrics.cyclomaticComplexity`、`classMetrics.wmc` | `MethodMetrics.java` / `ClassMetrics.java` |
| LoC 补充（课程允许加入传统点） | `projectSummary.totalLoc`、`methodMetrics.loc`、`classMetrics.loc` | `ProjectSummary.java` / `MethodMetrics.java` / `ClassMetrics.java` |

## 4. API、页面与导出证据链

- API 输出：
  - `POST /api/metrics/analyze/text`
  - `POST /api/metrics/analyze/files`
  - `POST /api/metrics/analyze/folder`
  - 返回体主契约包含 `codeMetrics.lkMetrics`，并保留兼容字段 `codeMetrics.lkPresentation`。
- 页面证据：
  - `metrics-frontend/src/components/MetricsTables.vue`（CK/类与方法指标）
  - `metrics-frontend/src/components/MetricsCharts.vue`（复杂度可视化）
  - `metrics-frontend/src/utils/exporters.js`（课程对齐导出）
- 报告导出证据：
  - `metrics-frontend/src/utils/exporters.js` 增加 `LK Course-Aligned View` 区块与 CSV 的 `codeMetrics.lkMetrics.*` 字段，并兼容读取 `codeMetrics.lkPresentation.*`。
  - 对应测试：`metrics-frontend/src/utils/__tests__/exporters.test.js`。

## 5. 答辩口径（建议直接使用）

可用表述：

“我们当前实现的是 LK 课程对齐展示层。LK 相关指标不是孤立展示，而是由 AST 解析得到的类/方法/关系数据统一推导，并通过 `codeMetrics.lkMetrics` 固化为主契约接口；`codeMetrics.lkPresentation` 仅作为过渡期兼容字段保留。与此同时，我们保留 CK 与传统复杂度/LoC 指标用于交叉解释，因此能够支持课程要求中的面向对象度量说明与证据追溯。”
