# Apple-Inspired Workbench Redesign Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Rebuild the frontend into a Chinese-first, Apple-inspired product workbench with a polished overview layer, section-based navigation, and upgraded input/result modules without changing analysis behavior.

**Architecture:** Keep data fetching, submission, and export orchestration inside `metrics-frontend/src/App.vue`, but move the new overview and curated result surfaces into focused presentational components. Apply the redesign through localized copy, section-aware markup, and a unified `theme.css` system so the page feels like one product while the existing code/diagram/estimation workflows remain intact.

**Tech Stack:** Vue 3 (`<script setup>`), Vitest, `@testing-library/vue`, ECharts, CSS custom properties

---

## File Structure

### New files

- `metrics-frontend/src/components/ProductHero.vue`
  - Chinese-first hero with product narrative, live status strip, and CTA buttons into the workbench
- `metrics-frontend/src/components/MainlineOverview.vue`
  - Three premium capability cards for code metrics, diagram metrics, and project estimation
- `metrics-frontend/src/components/WorkbenchIntro.vue`
  - Transition band between overview and workbench with quick section-entry buttons
- `metrics-frontend/src/components/DiagramResultPanel.vue`
  - Curated diagram analysis summary with confidence, counts, and optional raw JSON details
- `metrics-frontend/src/components/EstimationResultPanel.vue`
  - Curated estimation summary with workload/cost/schedule/staffing and basis details
- `metrics-frontend/src/components/__tests__/DiagramResultPanel.test.js`
  - Focused regression for diagram result rendering
- `metrics-frontend/src/components/__tests__/EstimationResultPanel.test.js`
  - Focused regression for estimation result rendering

### Modified files

- `metrics-frontend/src/App.vue`
  - Navigation, section anchors, overview/workbench split, export module placement, and integration of the new presentational components
- `metrics-frontend/src/styles/theme.css`
  - Complete visual language reset: palette, spacing, typography, cards, navigation, inputs, results, responsive behavior
- `metrics-frontend/src/components/InputWorkspace.vue`
  - Chinese-first copy, refined segmented controls, improved empty/upload states
- `metrics-frontend/src/components/LkMetricsPanel.vue`
  - Updated premium LK evidence panel styling and Chinese UI
- `metrics-frontend/src/components/OverviewCards.vue`
  - Chinese summary labels and stronger stat-card hierarchy
- `metrics-frontend/src/components/MetricInfoDrawer.vue`
  - Chinese metric guide copy aligned with LK/CK defense language
- `metrics-frontend/src/components/MetricsCharts.vue`
  - Chinese chart title and palette aligned with the new accent variables
- `metrics-frontend/src/components/MetricsTables.vue`
  - Chinese table headings and refined table grouping copy
- `metrics-frontend/src/components/RiskPanel.vue`
  - Chinese heading and empty-state copy
- `metrics-frontend/src/components/__tests__/DashboardFlow.test.js`
  - Updated expectations for Chinese-first navigation, hero copy, localized result modules, and export buttons
- `metrics-frontend/src/components/__tests__/InputWorkspace.test.js`
  - Updated expectations for Chinese input modes and labels

### Verification files

- `metrics-frontend/src/utils/__tests__/exporters.test.js`
  - Must continue to pass unchanged unless button label refactors accidentally change export behavior

## Task 1: Product Shell And Section Navigation

**Files:**
- Create: `metrics-frontend/src/components/ProductHero.vue`
- Create: `metrics-frontend/src/components/MainlineOverview.vue`
- Create: `metrics-frontend/src/components/WorkbenchIntro.vue`
- Modify: `metrics-frontend/src/App.vue`
- Test: `metrics-frontend/src/components/__tests__/DashboardFlow.test.js`

- [ ] **Step 1: Write the failing dashboard-flow test for the new Chinese product shell**

```js
import { fireEvent, render, screen } from '@testing-library/vue'
import App from '../../App.vue'

test('renders the Chinese product shell and section navigation before analysis', async () => {
  render(App)

  expect(await screen.findByRole('link', { name: '产品概览' })).toBeInTheDocument()
  expect(screen.getByRole('link', { name: '代码度量' })).toBeInTheDocument()
  expect(screen.getByRole('link', { name: '设计图度量' })).toBeInTheDocument()
  expect(screen.getByRole('link', { name: '项目估算' })).toBeInTheDocument()
  expect(screen.getByRole('link', { name: '报告导出' })).toBeInTheDocument()

  expect(
    screen.getByRole('heading', { name: '面向课程项目的软件度量工作台' })
  ).toBeInTheDocument()
  expect(screen.getByText('三条主线，一套本地化分析流程。')).toBeInTheDocument()
  expect(screen.getByRole('button', { name: '进入代码度量' })).toBeInTheDocument()
  expect(screen.getByRole('button', { name: '进入分析工作区' })).toBeInTheDocument()
})
```

- [ ] **Step 2: Run the dashboard-flow test and confirm it fails on the old English shell**

Run: `npm run test -- --run src/components/__tests__/DashboardFlow.test.js`

Expected: FAIL because the current page still renders `Metrics Workbench`, `Three Mainlines`, and no Chinese top navigation.

- [ ] **Step 3: Implement the new shell components and wire them into `App.vue`**

`metrics-frontend/src/components/ProductHero.vue`

```vue
<template>
  <section class="hero-shell">
    <div class="hero-shell__copy">
      <p class="hero-shell__eyebrow">软件度量课程项目</p>
      <h1>面向课程项目的软件度量工作台</h1>
      <p class="hero-shell__lede">三条主线，一套本地化分析流程。</p>
      <p class="hero-shell__summary">
        从 Java 代码度量、设计图识别与分析，到项目估算和报告导出，统一在一个前端工作台中完成。
      </p>
      <div class="hero-shell__actions">
        <button type="button" class="primary-button" @click="$emit('jump', 'code-metrics')">进入代码度量</button>
        <button type="button" class="secondary-button" @click="$emit('jump', 'workbench')">进入分析工作区</button>
      </div>
    </div>

    <div class="hero-shell__status" aria-label="运行状态">
      <article class="status-chip">
        <span>代码后端</span>
        <strong>{{ healthStatus }}</strong>
      </article>
      <article class="status-chip">
        <span>识别服务</span>
        <strong>{{ recognitionStatus }}</strong>
      </article>
    </div>
  </section>
</template>

<script setup>
defineProps({
  healthStatus: { type: String, required: true },
  recognitionStatus: { type: String, required: true }
})

defineEmits(['jump'])
</script>
```

`metrics-frontend/src/components/MainlineOverview.vue`

```vue
<template>
  <section class="overview-section">
    <header class="section-heading">
      <p class="section-heading__eyebrow">核心能力</p>
      <h2>覆盖课程答辩的三条主线</h2>
    </header>

    <div class="mainline-showcase">
      <article v-for="item in items" :key="item.id" class="showcase-card">
        <p class="showcase-card__index">{{ item.index }}</p>
        <h3>{{ item.title }}</h3>
        <p>{{ item.description }}</p>
        <p class="showcase-card__meta">支持对象：{{ item.artifact }}</p>
        <button type="button" class="text-button" @click="$emit('select', item.id)">查看此主线</button>
      </article>
    </div>
  </section>
</template>

<script setup>
defineProps({
  items: { type: Array, required: true }
})

defineEmits(['select'])
</script>
```

`metrics-frontend/src/components/WorkbenchIntro.vue`

```vue
<template>
  <section id="workbench" class="workbench-intro">
    <div>
      <p class="section-heading__eyebrow">分析工作区</p>
      <h2>按阶段进入你要执行的度量任务</h2>
    </div>
    <div class="workbench-intro__actions">
      <button
        v-for="item in items"
        :key="item.id"
        type="button"
        class="mode-pill"
        @click="$emit('select', item.id)"
      >
        {{ item.label }}
      </button>
    </div>
  </section>
</template>

<script setup>
defineProps({
  items: { type: Array, required: true }
})

defineEmits(['select'])
</script>
```

`metrics-frontend/src/App.vue` (top-level structure excerpt)

```vue
<template>
  <main class="app-shell">
    <nav class="top-nav" aria-label="主导航">
      <a
        v-for="item in sectionItems"
        :key="item.id"
        :href="`#${item.id}`"
        class="top-nav__link"
        :class="{ 'is-active': activeSection === item.id }"
        @click.prevent="scrollToSection(item.id)"
      >
        {{ item.label }}
      </a>
    </nav>

    <section id="overview" :ref="bindSection('overview')" class="app-section app-section--hero">
      <ProductHero
        :health-status="healthStatus"
        :recognition-status="recognitionStatus"
        @jump="scrollToSection"
      />
    </section>

    <MainlineOverview :items="mainlineCards" @select="scrollToSection" />

    <section id="workbench" :ref="bindSection('workbench')" class="app-section app-section--workbench-intro">
      <WorkbenchIntro :items="workbenchItems" @select="scrollToSection" />
    </section>

    <section id="export" :ref="bindSection('export')" class="panel panel--feature">
      <header class="section-heading">
        <p class="section-heading__eyebrow">报告导出</p>
        <h2>统一汇总已有分析结果</h2>
      </header>
      <p class="panel-copy">导出 CSV 或 Markdown，直接作为课程报告与答辩材料的基础。</p>
      <div class="action-row">
        <button type="button" class="primary-button" :disabled="!hasAnyTrackData" @click="downloadCsv">导出 CSV</button>
        <button type="button" class="secondary-button" :disabled="!hasAnyTrackData" @click="downloadMarkdown">导出 Markdown</button>
      </div>
    </section>
  </main>
</template>

<script setup>
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'
import ProductHero from './components/ProductHero.vue'
import MainlineOverview from './components/MainlineOverview.vue'
import WorkbenchIntro from './components/WorkbenchIntro.vue'

const sectionItems = [
  { id: 'overview', label: '产品概览' },
  { id: 'code-metrics', label: '代码度量' },
  { id: 'diagram-metrics', label: '设计图度量' },
  { id: 'estimation', label: '项目估算' },
  { id: 'export', label: '报告导出' }
]

const mainlineCards = [
  { id: 'code-metrics', index: '01', title: '代码度量', description: '基于 AST 的 Java 代码度量与风险分析。', artifact: 'Java 源码 / 文件夹' },
  { id: 'diagram-metrics', index: '02', title: '设计图度量', description: '支持结构化输入与图片识别的设计图分析。', artifact: '类图 / 流程图 / 用例图' },
  { id: 'estimation', index: '03', title: '项目估算', description: '使用 UCP 与 Function Point 进行工作量、成本和工期估算。', artifact: '项目指标 / 用户输入' }
]

const workbenchItems = [
  { id: 'code-metrics', label: '进入代码度量' },
  { id: 'diagram-metrics', label: '进入设计图度量' },
  { id: 'estimation', label: '进入项目估算' },
  { id: 'export', label: '进入报告导出' }
]

const activeSection = ref('overview')
const sectionMap = new Map()
let sectionObserver

function bindSection(id) {
  return (element) => {
    if (element) {
      sectionMap.set(id, element)
    }
  }
}

function scrollToSection(id) {
  activeSection.value = id
  sectionMap.get(id)?.scrollIntoView({ behavior: 'smooth', block: 'start' })
}

onMounted(() => {
  sectionObserver = new IntersectionObserver(
    (entries) => {
      const visible = entries.find((entry) => entry.isIntersecting)
      if (visible?.target?.id) {
        activeSection.value = visible.target.id
      }
    },
    { rootMargin: '-30% 0px -50% 0px', threshold: 0.2 }
  )

  sectionMap.forEach((element) => sectionObserver.observe(element))
})

onBeforeUnmount(() => {
  sectionObserver?.disconnect()
})
</script>
```

- [ ] **Step 4: Re-run the dashboard-flow test and confirm the new shell is rendered**

Run: `npm run test -- --run src/components/__tests__/DashboardFlow.test.js`

Expected: PASS for the new navigation links, Chinese hero copy, and section-entry CTAs.

- [ ] **Step 5: Commit the shell scaffolding**

```bash
git add metrics-frontend/src/App.vue \
  metrics-frontend/src/components/ProductHero.vue \
  metrics-frontend/src/components/MainlineOverview.vue \
  metrics-frontend/src/components/WorkbenchIntro.vue \
  metrics-frontend/src/components/__tests__/DashboardFlow.test.js
git commit -m "frontend: scaffold Chinese product shell and section nav"
```

## Task 2: Unified Theme System And Responsive Layout

**Files:**
- Modify: `metrics-frontend/src/styles/theme.css`
- Modify: `metrics-frontend/src/App.vue`
- Modify: `metrics-frontend/src/components/ProductHero.vue`
- Modify: `metrics-frontend/src/components/MainlineOverview.vue`
- Modify: `metrics-frontend/src/components/WorkbenchIntro.vue`
- Test: `metrics-frontend/src/components/__tests__/DashboardFlow.test.js`

- [ ] **Step 1: Reuse the shell test as the safety net before changing the visual system**

Run: `npm run test -- --run src/components/__tests__/DashboardFlow.test.js`

Expected: PASS so the theme rewrite starts from a stable structural baseline.

- [ ] **Step 2: Replace the warm editorial theme with the cooler Apple-inspired system**

`metrics-frontend/src/styles/theme.css` (core token and layout excerpt)

```css
:root {
  --bg-canvas: #f5f7fb;
  --bg-ambient: radial-gradient(circle at top center, rgba(129, 150, 255, 0.18), transparent 34%),
    linear-gradient(180deg, #fbfcff 0%, #eef2f8 100%);
  --surface-primary: rgba(255, 255, 255, 0.84);
  --surface-secondary: rgba(244, 247, 252, 0.82);
  --surface-elevated: rgba(255, 255, 255, 0.94);
  --line-soft: rgba(18, 28, 45, 0.08);
  --line-strong: rgba(18, 28, 45, 0.14);
  --text-primary: #0f1728;
  --text-secondary: rgba(15, 23, 40, 0.68);
  --accent-strong: #5370ff;
  --accent-soft: rgba(83, 112, 255, 0.12);
  --success-soft: rgba(20, 184, 116, 0.12);
  --danger-soft: rgba(235, 87, 87, 0.14);
  --radius-xl: 32px;
  --radius-lg: 24px;
  --radius-md: 18px;
  --shadow-soft: 0 24px 70px rgba(15, 23, 40, 0.08);
  --shadow-card: 0 12px 28px rgba(15, 23, 40, 0.06);
  font-family: "PingFang SC", "SF Pro Display", "Microsoft YaHei", sans-serif;
}

body {
  margin: 0;
  color: var(--text-primary);
  background: var(--bg-ambient);
}

.app-shell {
  width: min(1280px, calc(100vw - 40px));
  margin: 0 auto;
  padding: 24px 0 96px;
}

.top-nav {
  position: sticky;
  top: 16px;
  z-index: 20;
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  padding: 12px;
  margin-bottom: 28px;
  border: 1px solid var(--line-soft);
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.7);
  backdrop-filter: blur(24px);
  box-shadow: var(--shadow-card);
}

.top-nav__link {
  padding: 10px 16px;
  border-radius: 999px;
  color: var(--text-secondary);
  text-decoration: none;
  transition: background-color 180ms ease, color 180ms ease, transform 180ms ease;
}

.top-nav__link.is-active {
  color: white;
  background: var(--accent-strong);
}

.panel,
.hero-shell,
.overview-section,
.workbench-intro {
  border: 1px solid var(--line-soft);
  border-radius: var(--radius-xl);
  background: var(--surface-primary);
  backdrop-filter: blur(18px);
  box-shadow: var(--shadow-soft);
}

@media (max-width: 840px) {
  .app-shell {
    width: min(100vw - 24px, 1280px);
    padding-top: 16px;
  }

  .top-nav {
    top: 10px;
    border-radius: 24px;
  }
}
```

`metrics-frontend/src/App.vue` (section wrappers excerpt)

```vue
<section id="code-metrics" :ref="bindSection('code-metrics')" class="app-section app-section--workbench">
  <section class="panel panel--feature">
    <header class="section-heading">
      <p class="section-heading__eyebrow">代码度量</p>
      <h2>基于 AST 的 Java 软件度量分析</h2>
    </header>
    <InputWorkspace
      @submit-text="handleSubmitText"
      @submit-file="handleSubmitFile"
      @submit-files="handleSubmitFile"
      @submit-folder="handleSubmitFolder"
    />
  </section>
</section>

<section id="diagram-metrics" :ref="bindSection('diagram-metrics')" class="app-section app-section--workbench">
  <section class="panel panel--feature">
    <header class="section-heading">
      <p class="section-heading__eyebrow">设计图度量</p>
      <h2>结构化与图像识别双输入</h2>
    </header>
  </section>
</section>

<section id="estimation" :ref="bindSection('estimation')" class="app-section app-section--workbench">
  <section class="panel panel--feature">
    <header class="section-heading">
      <p class="section-heading__eyebrow">项目估算</p>
      <h2>UCP 与 Function Point 双路径估算</h2>
    </header>
  </section>
</section>
```

- [ ] **Step 3: Run the dashboard-flow test again to confirm the theme refactor did not break the shell**

Run: `npm run test -- --run src/components/__tests__/DashboardFlow.test.js`

Expected: PASS with no text/query regressions after the CSS and wrapper changes.

- [ ] **Step 4: Build the frontend to catch template/class-name mistakes before deeper component work**

Run: `npm run build`

Expected: PASS with a production bundle generated successfully.

- [ ] **Step 5: Commit the theme system**

```bash
git add metrics-frontend/src/styles/theme.css metrics-frontend/src/App.vue \
  metrics-frontend/src/components/ProductHero.vue \
  metrics-frontend/src/components/MainlineOverview.vue \
  metrics-frontend/src/components/WorkbenchIntro.vue
git commit -m "frontend: add Apple-inspired visual system and layout shell"
```

## Task 3: Chinese-First Input Workspace

**Files:**
- Modify: `metrics-frontend/src/components/InputWorkspace.vue`
- Modify: `metrics-frontend/src/styles/theme.css`
- Test: `metrics-frontend/src/components/__tests__/InputWorkspace.test.js`

- [ ] **Step 1: Write the failing input-workspace test for the localized segmented input module**

```js
import { fireEvent, render, screen } from '@testing-library/vue'
import InputWorkspace from '../InputWorkspace.vue'

describe('InputWorkspace', () => {
  test('renders Chinese input modes and emits a text analysis payload', async () => {
    const { emitted } = render(InputWorkspace)

    expect(screen.getByText('选择代码输入方式')).toBeInTheDocument()
    expect(screen.getByRole('button', { name: '代码输入' })).toBeInTheDocument()
    expect(screen.getByRole('button', { name: '单文件上传' })).toBeInTheDocument()
    expect(screen.getByRole('button', { name: '多文件上传' })).toBeInTheDocument()
    expect(screen.getByRole('button', { name: '文件夹扫描' })).toBeInTheDocument()

    await fireEvent.update(screen.getByLabelText('Java 源码'), 'public class Demo {}')
    await fireEvent.click(screen.getByRole('button', { name: '开始分析' }))

    expect(emitted()['submit-text'][0][0]).toEqual({
      fileName: 'Snippet.java',
      sourceCode: 'public class Demo {}'
    })
  })
})
```

- [ ] **Step 2: Run the input-workspace test and confirm it fails with the old English labels**

Run: `npm run test -- --run src/components/__tests__/InputWorkspace.test.js`

Expected: FAIL because the component still renders `Code Input`, `Single File`, and `Analyze Text`.

- [ ] **Step 3: Implement the Chinese-first segmented input experience**

`metrics-frontend/src/components/InputWorkspace.vue`

```vue
<template>
  <section class="panel input-panel">
    <header class="section-heading section-heading--compact">
      <p class="section-heading__eyebrow">代码输入</p>
      <h3>选择代码输入方式</h3>
      <p class="panel-copy">支持直接粘贴 Java 代码，也支持单文件、多文件和文件夹批量分析。</p>
    </header>

    <div class="mode-row mode-row--segmented" role="tablist" aria-label="代码输入模式">
      <button
        v-for="item in modes"
        :key="item.key"
        type="button"
        class="mode-pill"
        :class="{ active: mode === item.key }"
        @click="mode = item.key"
      >
        {{ item.label }}
      </button>
    </div>

    <div v-if="mode === 'text'" class="pane pane--editor">
      <label for="source-input">Java 源码</label>
      <textarea id="source-input" v-model="textSource" rows="14" placeholder="在这里粘贴待分析的 Java 代码片段"></textarea>
      <button type="button" class="primary-button" @click="emitText">开始分析</button>
    </div>

    <div v-else class="pane pane--upload">
      <label :for="activeInputId">{{ activeLabel }}</label>
      <input :id="activeInputId" :accept="activeAccept" type="file" :multiple="mode !== 'file'" :webkitdirectory="mode === 'folder'" :directory="mode === 'folder'" @change="emitActiveFiles" />
      <p class="field-note">{{ activeNote }}</p>
    </div>
  </section>
</template>

<script setup>
import { computed, ref } from 'vue'

const emit = defineEmits(['submit-text', 'submit-file', 'submit-files', 'submit-folder'])
const mode = ref('text')
const textSource = ref('')
const modes = [
  { key: 'text', label: '代码输入' },
  { key: 'file', label: '单文件上传' },
  { key: 'files', label: '多文件上传' },
  { key: 'folder', label: '文件夹扫描' }
]

const uploadConfig = computed(() => ({
  file: { id: 'single-file', label: 'Java 文件', note: '选择一个 .java 文件进行分析。', accept: '.java' },
  files: { id: 'multi-file', label: 'Java 文件集合', note: '可一次上传多个 .java 文件。', accept: '.java' },
  folder: { id: 'folder-file', label: 'Java 源码文件夹', note: '选择包含源码的目录，前端会递交目录下的 Java 文件。', accept: '.java' }
})[mode.value] ?? { id: 'single-file', label: 'Java 文件', note: '', accept: '.java' })

const activeInputId = computed(() => uploadConfig.value.id)
const activeLabel = computed(() => uploadConfig.value.label)
const activeNote = computed(() => uploadConfig.value.note)
const activeAccept = computed(() => uploadConfig.value.accept)

function emitText() {
  emit('submit-text', { fileName: 'Snippet.java', sourceCode: textSource.value })
}

function emitActiveFiles(event) {
  const files = Array.from(event.target.files || [])
  if (mode.value === 'file') emit('submit-file', files)
  else if (mode.value === 'files') emit('submit-files', files)
  else emit('submit-folder', files)
}
</script>
```

`metrics-frontend/src/styles/theme.css` (input styling excerpt)

```css
.mode-row--segmented {
  padding: 8px;
  border-radius: 20px;
  background: var(--surface-secondary);
}

.pane--editor,
.pane--upload {
  padding: 20px;
  border: 1px solid var(--line-soft);
  border-radius: var(--radius-lg);
  background: var(--surface-elevated);
}

.field-note {
  margin: 0;
  color: var(--text-secondary);
}

textarea,
input[type='file'],
input[type='number'],
select {
  width: 100%;
  border-radius: 18px;
  border: 1px solid var(--line-soft);
  padding: 16px;
  background: white;
  color: var(--text-primary);
}
```

- [ ] **Step 4: Re-run the input-workspace test and confirm the localized module passes**

Run: `npm run test -- --run src/components/__tests__/InputWorkspace.test.js`

Expected: PASS for the Chinese buttons, label, and `submit-text` payload.

- [ ] **Step 5: Commit the input workspace redesign**

```bash
git add metrics-frontend/src/components/InputWorkspace.vue \
  metrics-frontend/src/styles/theme.css \
  metrics-frontend/src/components/__tests__/InputWorkspace.test.js
git commit -m "frontend: localize and restyle code input workspace"
```

## Task 4: Localize And Curate The Code-Metrics Result Surfaces

**Files:**
- Modify: `metrics-frontend/src/components/OverviewCards.vue`
- Modify: `metrics-frontend/src/components/LkMetricsPanel.vue`
- Modify: `metrics-frontend/src/components/MetricsCharts.vue`
- Modify: `metrics-frontend/src/components/MetricsTables.vue`
- Modify: `metrics-frontend/src/components/RiskPanel.vue`
- Modify: `metrics-frontend/src/components/MetricInfoDrawer.vue`
- Modify: `metrics-frontend/src/styles/theme.css`
- Test: `metrics-frontend/src/components/__tests__/DashboardFlow.test.js`

- [ ] **Step 1: Extend the dashboard-flow test to assert the localized result surfaces**

```js
test('renders Chinese summary and LK result panels after analysis completes', async () => {
  render(App)

  await fireEvent.update(screen.getByLabelText('Java 源码'), 'public class Demo { void go() {} }')
  await fireEvent.click(screen.getByRole('button', { name: '开始分析' }))

  expect(await screen.findByText('项目概览')).toBeInTheDocument()
  expect(await screen.findByText('方法总数')).toBeInTheDocument()
  expect(await screen.findByText('LK 课程对齐视图')).toBeInTheDocument()
  expect(await screen.findByText('关系密度')).toBeInTheDocument()
  expect(await screen.findByText('复杂度视图')).toBeInTheDocument()
  expect(await screen.findByText('类级指标')).toBeInTheDocument()
  expect(await screen.findByText('风险提示')).toBeInTheDocument()
  expect(await screen.findByText(/CK 指标钻取/)).toBeInTheDocument()
})
```

- [ ] **Step 2: Run the dashboard-flow test and confirm it fails on the old English result copy**

Run: `npm run test -- --run src/components/__tests__/DashboardFlow.test.js`

Expected: FAIL because the current result area still renders `Files`, `Complexity View`, `Risk Findings`, and `Metric Guide`.

- [ ] **Step 3: Localize the result components and upgrade their hierarchy**

`metrics-frontend/src/components/OverviewCards.vue`

```vue
<template>
  <section class="result-section">
    <header class="section-heading section-heading--compact">
      <p class="section-heading__eyebrow">代码概览</p>
      <h2>项目概览</h2>
    </header>

    <div class="card-grid">
      <article class="metric-card">
        <span>文件总数</span>
        <strong>{{ summary.totalFiles }}</strong>
      </article>
      <article class="metric-card">
        <span>类总数</span>
        <strong>{{ summary.totalClasses }}</strong>
      </article>
      <article class="metric-card">
        <span>方法总数</span>
        <strong>{{ summary.totalMethods }}</strong>
      </article>
      <article class="metric-card">
        <span>总代码行数</span>
        <strong>{{ summary.totalLoc }}</strong>
      </article>
    </div>
  </section>
</template>
```

`metrics-frontend/src/components/LkMetricsPanel.vue`

```vue
<template>
  <section class="panel panel--evidence">
    <header class="section-heading section-heading--compact">
      <p class="section-heading__eyebrow">LK 指标</p>
      <h2>LK 课程对齐视图</h2>
      <p class="panel-copy">用于课程答辩和导出报告的 LK 对齐指标汇总。</p>
    </header>

    <section class="lk-grid">
      <article v-for="item in metricCards" :key="item.label" class="lk-card">
        <span>{{ item.label }}</span>
        <strong>{{ item.value }}</strong>
      </article>
    </section>

    <div class="lk-evidence">
      <p><strong>继承深度分布</strong></p>
      <p>{{ formatDistribution(lkMetrics.inheritanceDepthDistribution) }}</p>
      <p class="lk-note">建议结合 CK 指标钻取表格和导出报告一起展示。</p>
    </div>
  </section>
</template>
```

`metrics-frontend/src/components/MetricsCharts.vue`

```vue
<template>
  <section class="panel">
    <header class="section-heading section-heading--compact">
      <p class="section-heading__eyebrow">复杂度分布</p>
      <h2>复杂度视图</h2>
    </header>
    <div ref="complexityChart" class="chart-surface"></div>
  </section>
</template>

<script setup>
function chartAccent() {
  return getComputedStyle(document.documentElement).getPropertyValue('--accent-strong').trim() || '#5370ff'
}

chart.setOption({
  tooltip: { trigger: 'axis' },
  grid: { left: 40, right: 16, top: 24, bottom: 32 },
  xAxis: {
    type: 'category',
    data: topMethods.map((item) => item.methodName),
    axisLabel: { color: '#64748b' }
  },
  yAxis: {
    type: 'value',
    axisLabel: { color: '#64748b' },
    splitLine: { lineStyle: { color: 'rgba(15, 23, 40, 0.08)' } }
  },
  series: [
    {
      type: 'bar',
      data: topMethods.map((item) => item.cyclomaticComplexity),
      itemStyle: { color: chartAccent() }
    }
  ]
})
</script>
```

`metrics-frontend/src/components/MetricsTables.vue`

```vue
<template>
  <section class="panel table-stack">
    <header class="section-heading section-heading--compact">
      <p class="section-heading__eyebrow">CK 指标钻取</p>
      <h2>类级指标</h2>
    </header>
    <table>
      <thead>
        <tr>
          <th>类名</th>
          <th>WMC</th>
          <th>CBO</th>
          <th>RFC</th>
          <th>DIT</th>
          <th>NOC</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="item in classMetrics" :key="item.fileName + item.className">
          <td>{{ item.className }}</td>
          <td>{{ item.wmc }}</td>
          <td>{{ item.cbo }}</td>
          <td>{{ item.rfc }}</td>
          <td>{{ item.dit }}</td>
          <td>{{ item.noc }}</td>
        </tr>
      </tbody>
    </table>
    <h3>方法级指标</h3>
    <table>
      <thead>
        <tr>
          <th>方法名</th>
          <th>圈复杂度</th>
          <th>代码行数</th>
          <th>最大嵌套深度</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="item in methodMetrics" :key="item.className + item.methodName">
          <td>{{ item.className }}#{{ item.methodName }}</td>
          <td>{{ item.cyclomaticComplexity }}</td>
          <td>{{ item.loc }}</td>
          <td>{{ item.maxNestingDepth }}</td>
        </tr>
      </tbody>
    </table>
  </section>
</template>
```

`metrics-frontend/src/components/RiskPanel.vue`

```vue
<template>
  <section class="panel">
    <header class="section-heading section-heading--compact">
      <p class="section-heading__eyebrow">风险分析</p>
      <h2>风险提示</h2>
    </header>
    <ul v-if="riskFindings.length">
      <li v-for="item in riskFindings" :key="item.scope + item.target">
        {{ item.scope }}：{{ item.target }} - {{ item.message }}
      </li>
    </ul>
    <p v-else>本次分析没有发现高风险项。</p>
  </section>
</template>
```

`metrics-frontend/src/components/MetricInfoDrawer.vue`

```vue
<template>
  <aside class="panel">
    <header class="section-heading section-heading--compact">
      <p class="section-heading__eyebrow">指标说明</p>
      <h2>课程答辩讲解提示</h2>
    </header>
    <ul class="guide-list">
      <li>LK 课程对齐视图：用于展示类规模、方法规模、属性规模、关系数量、关系密度与继承深度分布。</li>
      <li>CK 指标钻取：通过类级表格查看 WMC、CBO、RFC、DIT、NOC 等指标。</li>
      <li>WMC：类内所有方法圈复杂度之和。</li>
      <li>CBO：类对外部类型的耦合数量。</li>
      <li>RFC：可响应的方法集合规模。</li>
      <li>LCOM：字段使用重叠程度对应的内聚性缺失估计。</li>
    </ul>
  </aside>
</template>
```

`metrics-frontend/src/styles/theme.css` (result-surface excerpt)

```css
.section-heading {
  display: grid;
  gap: 8px;
  margin-bottom: 20px;
}

.section-heading__eyebrow {
  margin: 0;
  color: var(--accent-strong);
  font-size: 0.85rem;
  letter-spacing: 0.08em;
}

.metric-card,
.lk-card {
  padding: 22px;
  border: 1px solid var(--line-soft);
  border-radius: var(--radius-lg);
  background: var(--surface-elevated);
}

.metric-card span,
.lk-card span,
.panel-copy,
.guide-list,
.lk-note {
  color: var(--text-secondary);
}
```

- [ ] **Step 4: Re-run the dashboard-flow test and confirm the localized results pass**

Run: `npm run test -- --run src/components/__tests__/DashboardFlow.test.js`

Expected: PASS for the Chinese summary cards, LK panel, complexity chart heading, CK drill-down table, risk panel, and metric guide.

- [ ] **Step 5: Commit the localized code-metrics result modules**

```bash
git add metrics-frontend/src/components/OverviewCards.vue \
  metrics-frontend/src/components/LkMetricsPanel.vue \
  metrics-frontend/src/components/MetricsCharts.vue \
  metrics-frontend/src/components/MetricsTables.vue \
  metrics-frontend/src/components/RiskPanel.vue \
  metrics-frontend/src/components/MetricInfoDrawer.vue \
  metrics-frontend/src/styles/theme.css \
  metrics-frontend/src/components/__tests__/DashboardFlow.test.js
git commit -m "frontend: localize and polish code metrics result surfaces"
```

## Task 5: Curated Diagram And Estimation Result Panels

**Files:**
- Create: `metrics-frontend/src/components/DiagramResultPanel.vue`
- Create: `metrics-frontend/src/components/EstimationResultPanel.vue`
- Create: `metrics-frontend/src/components/__tests__/DiagramResultPanel.test.js`
- Create: `metrics-frontend/src/components/__tests__/EstimationResultPanel.test.js`
- Modify: `metrics-frontend/src/App.vue`
- Modify: `metrics-frontend/src/styles/theme.css`

- [ ] **Step 1: Write the failing diagram-result component test**

```js
import { render, screen } from '@testing-library/vue'
import DiagramResultPanel from '../DiagramResultPanel.vue'

test('renders a curated Chinese summary for diagram analysis', () => {
  render(DiagramResultPanel, {
    props: {
      result: {
        diagramType: 'class',
        sourceType: 'image',
        confidence: { overall: 0.91 },
        nodes: [{ id: 'A' }, { id: 'B' }],
        relations: [{ from: 'A', to: 'B' }]
      }
    }
  })

  expect(screen.getByText('设计图分析结果')).toBeInTheDocument()
  expect(screen.getByText('图类型')).toBeInTheDocument()
  expect(screen.getByText('class')).toBeInTheDocument()
  expect(screen.getByText('识别置信度')).toBeInTheDocument()
  expect(screen.getByText('0.91')).toBeInTheDocument()
})
```

- [ ] **Step 2: Write the failing estimation-result component test**

```js
import { render, screen } from '@testing-library/vue'
import EstimationResultPanel from '../EstimationResultPanel.vue'

test('renders the estimation summary and basis details in Chinese', () => {
  render(EstimationResultPanel, {
    props: {
      result: {
        workloadPersonMonths: 12.4,
        cost: 186000,
        scheduleMonths: 3.1,
        suggestedStaffing: 4,
        basis: { summary: 'Function Point estimation using direct counts.' },
        functionPointBreakdown: {
          adjustedFunctionPoints: 178.2,
          valueAdjustmentFactor: 1.1
        }
      }
    }
  })

  expect(screen.getByText('项目估算结果')).toBeInTheDocument()
  expect(screen.getByText('工作量（人月）')).toBeInTheDocument()
  expect(screen.getByText('12.40')).toBeInTheDocument()
  expect(screen.getByText('估算依据')).toBeInTheDocument()
  expect(screen.getByText(/Function Point estimation/)).toBeInTheDocument()
})
```

- [ ] **Step 3: Run the new component tests to confirm the panels do not exist yet**

Run: `npm run test -- --run src/components/__tests__/DiagramResultPanel.test.js src/components/__tests__/EstimationResultPanel.test.js`

Expected: FAIL because the new components and tests are not wired into the codebase yet.

- [ ] **Step 4: Implement the curated result components and replace raw JSON boxes in `App.vue`**

`metrics-frontend/src/components/DiagramResultPanel.vue`

```vue
<template>
  <section class="panel panel--result">
    <header class="section-heading section-heading--compact">
      <p class="section-heading__eyebrow">设计图识别</p>
      <h3>设计图分析结果</h3>
    </header>

    <div class="card-grid">
      <article class="metric-card"><span>图类型</span><strong>{{ result.diagramType || 'n/a' }}</strong></article>
      <article class="metric-card"><span>来源</span><strong>{{ result.sourceType || 'n/a' }}</strong></article>
      <article class="metric-card"><span>识别置信度</span><strong>{{ formatNumber(result.confidence?.overall) }}</strong></article>
      <article class="metric-card"><span>节点数量</span><strong>{{ result.nodes?.length ?? 0 }}</strong></article>
    </div>

    <details class="raw-details">
      <summary>查看原始结果</summary>
      <pre>{{ JSON.stringify(result, null, 2) }}</pre>
    </details>
  </section>
</template>

<script setup>
defineProps({
  result: { type: Object, required: true }
})

function formatNumber(value) {
  return typeof value === 'number' ? value.toFixed(2) : 'n/a'
}
</script>
```

`metrics-frontend/src/components/EstimationResultPanel.vue`

```vue
<template>
  <section class="panel panel--result">
    <header class="section-heading section-heading--compact">
      <p class="section-heading__eyebrow">项目估算</p>
      <h3>项目估算结果</h3>
    </header>

    <div class="card-grid">
      <article class="metric-card"><span>工作量（人月）</span><strong>{{ formatNumber(result.workloadPersonMonths) }}</strong></article>
      <article class="metric-card"><span>成本</span><strong>{{ formatCurrency(result.cost) }}</strong></article>
      <article class="metric-card"><span>工期（月）</span><strong>{{ formatNumber(result.scheduleMonths) }}</strong></article>
      <article class="metric-card"><span>建议人数</span><strong>{{ result.suggestedStaffing ?? 'n/a' }}</strong></article>
    </div>

    <div class="evidence-block">
      <p><strong>估算依据</strong></p>
      <p>{{ result.basis?.summary || '暂无说明' }}</p>
      <p v-if="result.functionPointBreakdown">
        Function Point：{{ formatNumber(result.functionPointBreakdown.adjustedFunctionPoints) }}
        / VAF {{ formatNumber(result.functionPointBreakdown.valueAdjustmentFactor) }}
      </p>
    </div>
  </section>
</template>

<script setup>
defineProps({
  result: { type: Object, required: true }
})

function formatNumber(value) {
  return typeof value === 'number' ? value.toFixed(2) : 'n/a'
}

function formatCurrency(value) {
  return typeof value === 'number' ? `¥${value.toLocaleString('zh-CN')}` : 'n/a'
}
</script>
```

`metrics-frontend/src/App.vue` (integration excerpt)

```vue
<section id="diagram-metrics" :ref="bindSection('diagram-metrics')" class="app-section app-section--workbench">
  <section class="panel panel--feature">
    <header class="section-heading">
      <p class="section-heading__eyebrow">设计图度量</p>
      <h2>结构化与图像识别双输入</h2>
    </header>
    <div class="two-column-grid">
      <form class="pane pane--upload" @submit.prevent="runStructured">
        <label for="structured-type">图类型</label>
        <select id="structured-type" v-model="structuredType">
          <option value="class">class</option>
          <option value="flow">flow</option>
          <option value="usecase">usecase</option>
        </select>
        <label for="structured-file">结构化文件（.puml / .mmd）</label>
        <input id="structured-file" type="file" accept=".puml,.mmd,.txt" @change="onStructuredFileChange" />
        <button type="submit" class="primary-button" :disabled="diagramLoading">分析结构化设计图</button>
      </form>

      <form class="pane pane--upload" @submit.prevent="runImage">
        <label for="image-type">图类型</label>
        <select id="image-type" v-model="imageType">
          <option value="class">class</option>
          <option value="flow">flow</option>
          <option value="usecase">usecase</option>
        </select>
        <label for="image-file">设计图图片（.png / .jpg）</label>
        <input id="image-file" type="file" accept=".png,.jpg,.jpeg" @change="onImageFileChange" />
        <button type="submit" class="primary-button" :disabled="diagramLoading">分析图片设计图</button>
      </form>
    </div>
    <p v-if="diagramLoading" class="status-banner">正在运行设计图识别...</p>
    <p v-if="diagramError" class="status-banner error">{{ diagramError }}</p>
    <DiagramResultPanel v-if="diagramResult" :result="diagramResult" />
  </section>
</section>

<section id="estimation" :ref="bindSection('estimation')" class="app-section app-section--workbench">
  <section class="panel panel--feature">
    <header class="section-heading">
      <p class="section-heading__eyebrow">项目估算</p>
      <h2>UCP 与 Function Point 双路径估算</h2>
    </header>
    <form class="estimation-grid" @submit.prevent="runEstimation">
      <label for="estimate-method">估算方法</label>
      <select id="estimate-method" v-model="estimateForm.estimationMethod">
        <option value="ucp">ucp</option>
        <option value="function_point">function_point</option>
      </select>
      <label for="estimate-loc">总代码行数</label>
      <input id="estimate-loc" type="number" min="0" v-model.number="estimateForm.totalLoc" />
      <label for="estimate-cost-rate">每人月成本</label>
      <input id="estimate-cost-rate" type="number" min="1" v-model.number="estimateForm.costRatePerPersonMonth" />
      <label for="estimate-schedule">目标工期（月）</label>
      <input id="estimate-schedule" type="number" min="0.1" step="0.1" v-model.number="estimateForm.targetScheduleMonths" />
      <button type="submit" class="primary-button" :disabled="estimationLoading">开始估算</button>
    </form>
    <p v-if="estimationLoading" class="status-banner">正在计算项目估算结果...</p>
    <p v-if="estimationError" class="status-banner error">{{ estimationError }}</p>
    <EstimationResultPanel v-if="estimationResult" :result="estimationResult" />
  </section>
</section>
```

`metrics-frontend/src/styles/theme.css` (details styling excerpt)

```css
.panel--result .metric-card strong {
  font-size: 1.8rem;
}

.raw-details {
  margin-top: 20px;
  padding: 16px 18px;
  border: 1px solid var(--line-soft);
  border-radius: 20px;
  background: var(--surface-secondary);
}

.raw-details summary {
  cursor: pointer;
  font-weight: 600;
}

.evidence-block {
  margin-top: 20px;
  padding: 18px;
  border-radius: 20px;
  background: var(--surface-secondary);
}
```

- [ ] **Step 5: Run the new panel tests and confirm they pass**

Run: `npm run test -- --run src/components/__tests__/DiagramResultPanel.test.js src/components/__tests__/EstimationResultPanel.test.js`

Expected: PASS for the new Chinese summary cards and basis details.

- [ ] **Step 6: Commit the curated diagram/estimation result panels**

```bash
git add metrics-frontend/src/components/DiagramResultPanel.vue \
  metrics-frontend/src/components/EstimationResultPanel.vue \
  metrics-frontend/src/components/__tests__/DiagramResultPanel.test.js \
  metrics-frontend/src/components/__tests__/EstimationResultPanel.test.js \
  metrics-frontend/src/App.vue \
  metrics-frontend/src/styles/theme.css
git commit -m "frontend: add curated diagram and estimation result panels"
```

## Task 6: Full Regression, Export Safety, And Final Polish

**Files:**
- Modify: `metrics-frontend/src/App.vue`
- Modify: `metrics-frontend/src/styles/theme.css`
- Verify: `metrics-frontend/src/components/__tests__/InputWorkspace.test.js`
- Verify: `metrics-frontend/src/components/__tests__/DashboardFlow.test.js`
- Verify: `metrics-frontend/src/components/__tests__/DiagramResultPanel.test.js`
- Verify: `metrics-frontend/src/components/__tests__/EstimationResultPanel.test.js`
- Verify: `metrics-frontend/src/utils/__tests__/exporters.test.js`

- [ ] **Step 1: Verify the targeted frontend suite still passes together**

Run: `npm run test -- --run src/components/__tests__/InputWorkspace.test.js src/components/__tests__/DashboardFlow.test.js src/components/__tests__/DiagramResultPanel.test.js src/components/__tests__/EstimationResultPanel.test.js src/utils/__tests__/exporters.test.js`

Expected: PASS across input, shell, result-panel, and exporter regressions.

- [ ] **Step 2: Build the frontend for a production smoke test**

Run: `npm run build`

Expected: PASS with the localized shell, new components, and theme classes compiled successfully.

- [ ] **Step 3: Manually verify the acceptance checklist in the browser**

Check:

- 产品概览、代码度量、设计图度量、项目估算、报告导出五个导航项可点击
- 顶部导航在桌面端保持粘性，在移动端不遮挡内容
- Hero 区、能力概览、工作区引导形成明显的双层结构
- `InputWorkspace`、`LK` 面板、设计图结果、估算结果使用统一的视觉语言
- 代码分析、设计图分析、项目估算、报告导出四条关键路径仍可操作

- [ ] **Step 4: Commit the final polish**

```bash
git add metrics-frontend/src/App.vue metrics-frontend/src/styles/theme.css \
  metrics-frontend/src/components metrics-frontend/src/utils/__tests__/exporters.test.js
git commit -m "frontend: finalize Apple-inspired workbench redesign"
```
