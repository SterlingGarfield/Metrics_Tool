# Course Defense Evidence Pack Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add a defense-ready evidence pack that turns the existing samples, scripts, docs, and course-alignment materials into a repeatable demonstration path.

**Architecture:** Keep the product behavior unchanged and add a documentation/navigation layer around it. Introduce two new Chinese docs for the defense walkthrough and evidence indexing, upgrade the sample operator guide, and expose the new material from the Chinese README and report outline so defense preparation and report writing both follow the same evidence chain.

**Tech Stack:** Markdown documentation, PowerShell script references, existing Vue/Spring Boot/FastAPI repository structure

---

### Task 1: Create The Defense Walkthrough Document

**Files:**
- Create: `docs/course-defense-demo.zh-CN.md`

- [ ] **Step 1: Write the walkthrough skeleton with the required sections**

Create the file with this structure:

```markdown
# 课程答辩演示手册

## 1. 演示目标
## 2. 演示前检查
## 3. 启动顺序
## 4. 推荐演示主路径
## 5. 时间不足时的最小通过路径
## 6. 运行异常时的兜底路径
## 7. 答辩问答快速索引
```

- [ ] **Step 2: Fill the pre-demo checklist and startup order with exact commands**

Add the concrete commands and checkpoints:

```markdown
```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\bootstrap-models.ps1 -SkipTests
powershell -ExecutionPolicy Bypass -File .\scripts\dev.ps1
```

- 前端：`http://localhost:5173`
- 后端：`http://localhost:8080`
- 识别服务：`http://localhost:8090`
- 如需打包演示 Jar：

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\build-demo.ps1
powershell -ExecutionPolicy Bypass -File .\scripts\run-demo.ps1
powershell -ExecutionPolicy Bypass -File .\scripts\smoke-demo.ps1
```
```

- [ ] **Step 3: Fill the main walkthrough with the recommended defense order**

Add one subsection per stage using this pattern:

```markdown
### 步骤 1：代码度量
- 输入：`samples/ui-demo-inputs/code-input/CodeInputSample.java`
- 操作：在 `Code Input` 模式粘贴代码并分析
- 重点展示：`CK`、`LK Course-Aligned View`、圈复杂度、LoC
- 对应课程条款：总体要求、(1)、(3)、(4)、(5)
```

Also include subsections for:

- structured diagram analysis using `samples/diagram-inputs/structured/...`
- image diagram recognition using `samples/diagram-inputs/images/...`
- project estimation using both `ucp` and `function_point`
- export/report evidence using the report/export path

- [ ] **Step 4: Add the fallback and Q&A guidance**

Include these points in the document:

```markdown
- 时间不足时，保留：代码度量 -> 一个结构化设计图 -> 一个估算路径 -> 导出报告
- 图片识别异常时，先展示结构化设计图，再展示识别状态或说明运行时依赖边界
- Q&A 文档入口：
  - `docs/course-requirement-matrix.zh-CN.md`
  - `docs/lk-course-alignment.zh-CN.md`
  - `docs/metric-definitions.md`
  - `docs/estimation-method.md`
```

- [ ] **Step 5: Verify the walkthrough contains the expected anchors**

Run:

```powershell
rg -n "推荐演示主路径|最小通过路径|兜底路径|function_point|LK Course-Aligned View" docs/course-defense-demo.zh-CN.md
```

Expected: five matching lines covering the main walkthrough, fallback route, and FP/LK references.

### Task 2: Create The Course Evidence Index

**Files:**
- Create: `docs/course-evidence-index.zh-CN.md`

- [ ] **Step 1: Create the evidence index header and usage note**

Start the file with:

```markdown
# 课程证据索引

> 用途：把“课程要求 -> 页面/API/样例/文档/测试”的证据链压缩成答辩和报告可直接引用的索引表。
```

- [ ] **Step 2: Add the requirement-to-evidence table**

Create a table with these columns:

```markdown
| 课程条款 | 页面 / 工作流 | API / 合同 | 样例输入 | 文档证据 | 测试证据 |
| --- | --- | --- | --- | --- | --- |
```

Populate at least these rows:

- overall requirement
- `(1)` OO metrics with LK/CK/traditional metrics
- `(2)` additional methods with `ucp` and `function_point`
- `(3)` entities for LoC/workload/cost/schedule/staffing
- `(4)` inputs for code/class diagram/flow/use-case/user input
- `(5)` Java `ASTParser`

- [ ] **Step 3: Add the defense-use notes below the table**

Add a short note block like:

```markdown
## 使用建议

1. 现场演示时，优先按 `docs/course-defense-demo.zh-CN.md` 的顺序走主路径。
2. 被追问课程覆盖度时，直接打开本索引表定位证据。
3. 被追问 LK/公式时，跳转到 `docs/lk-course-alignment.zh-CN.md` 与 `docs/metric-definitions.md`。
```

- [ ] **Step 4: Verify the evidence index covers all numbered requirements**

Run:

```powershell
rg -n "\(1\)|\(2\)|\(3\)|\(4\)|\(5\)|function_point|ASTParser" docs/course-evidence-index.zh-CN.md
```

Expected: matches for every numbered requirement and the explicit FP/AST evidence rows.

### Task 3: Upgrade The Sample Operator Guide

**Files:**
- Modify: `samples/ui-demo-inputs/README.md`

- [ ] **Step 1: Replace the bare input list with a demo-oriented guide**

Rewrite the document so it starts with:

```markdown
# UI Demo Inputs

These sample files support the recommended course-defense flow in `docs/course-defense-demo.zh-CN.md`.

## Recommended Order
1. Code Input
2. Single File
3. Multiple Files
4. Folder Scan
```

- [ ] **Step 2: Add expected observations for each mode**

For each section, include bullets in this shape:

```markdown
## Code Input
- Use: `samples/ui-demo-inputs/code-input/CodeInputSample.java`
- Expected result: class/method/project metrics plus `LK Course-Aligned View`
- Talking point: show that the tool supports direct code input and OO metrics
```

Repeat the same pattern for:

- `Single File`
- `Multiple Files`
- `Folder Scan`

- [ ] **Step 3: Add a note that connects the operator guide to the defense and report docs**

Append:

```markdown
## Related References

- Defense walkthrough: `docs/course-defense-demo.zh-CN.md`
- Evidence index: `docs/course-evidence-index.zh-CN.md`
- Course requirement matrix: `docs/course-requirement-matrix.zh-CN.md`
```

- [ ] **Step 4: Verify the sample guide mentions all four input modes and the new references**

Run:

```powershell
rg -n "Recommended Order|Code Input|Single File|Multiple Files|Folder Scan|course-defense-demo|course-evidence-index" samples/ui-demo-inputs/README.md
```

Expected: matches for the four input modes plus the two new doc references.

### Task 4: Expose The Evidence Pack From Existing Entry Points

**Files:**
- Modify: `README.zh-CN.md`
- Modify: `docs/report-outline.md`

- [ ] **Step 1: Add a defense-materials subsection to the Chinese README**

Insert a new subsection under the existing course-material area:

```markdown
## 课程答辩与报告入口

- 答辩演示手册：`docs/course-defense-demo.zh-CN.md`
- 课程证据索引：`docs/course-evidence-index.zh-CN.md`
- 课程要求对照矩阵：`docs/course-requirement-matrix.zh-CN.md`
- LK 课程口径映射：`docs/lk-course-alignment.zh-CN.md`
- 指标定义与公式：`docs/metric-definitions.md`
- 项目估算方法：`docs/estimation-method.md`
```

- [ ] **Step 2: Add defense-pack references to the report outline appendix**

Extend the appendix list in `docs/report-outline.md` with:

```markdown
- Defense walkthrough: `docs/course-defense-demo.zh-CN.md`
- Defense evidence index: `docs/course-evidence-index.zh-CN.md`
```

Also add one note under section 4 or 6 that these docs support the demo/results narrative.

- [ ] **Step 3: Verify the new entry points are discoverable**

Run:

```powershell
rg -n "course-defense-demo|course-evidence-index|课程答辩与报告入口" README.zh-CN.md docs/report-outline.md
```

Expected: matches in both files.

### Task 5: Final Documentation Verification

**Files:**
- Verify: `docs/course-defense-demo.zh-CN.md`
- Verify: `docs/course-evidence-index.zh-CN.md`
- Verify: `samples/ui-demo-inputs/README.md`
- Verify: `README.zh-CN.md`
- Verify: `docs/report-outline.md`

- [ ] **Step 1: Run a combined repository grep for the new evidence-pack links**

Run:

```powershell
rg -n "course-defense-demo|course-evidence-index|LK Course-Aligned View|function_point" docs README.zh-CN.md samples/ui-demo-inputs/README.md
```

Expected: matches across the new docs, README, and sample guide.

- [ ] **Step 2: Inspect the final diff summary**

Run:

```powershell
git -C "D:\Projects\SQA\Metrics_Tool\.worktrees\diagram-recognition-enhancement" diff --stat -- docs README.zh-CN.md samples/ui-demo-inputs/README.md
```

Expected: diff summary shows the two new docs plus the updated sample guide and entry-point documents.

- [ ] **Step 3: Record the completion note**

Use this note in the final handoff:

```markdown
Course-defense evidence pack added: stable walkthrough, requirement-to-evidence index, demo-operator sample guide, and top-level documentation links.
```
