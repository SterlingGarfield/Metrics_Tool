# Metrics Tool Aura Dark Redesign Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Rebuild the Metrics Tool frontend into a branded dark Aura-style experience with a homepage-first flow, a clearer analysis workspace, and a more professional results workspace without changing backend APIs.

**Architecture:** Keep the current Vue single-page app and existing analysis composable, but split the UI into focused presentation components: app shell, homepage hero, analysis workspace, empty state, and results sections. Centralize theme tokens in `theme.css`, move external image assets into repo-local frontend assets, and drive the redesign through small TDD-backed component changes.

**Tech Stack:** Vue 3, Vite, Vitest, Testing Library Vue, Axios, ECharts, CSS custom properties

---

## File Structure Map

### Existing files to modify

- `metrics-frontend/src/App.vue`
  Responsibility: compose the new app shell, hero, analysis workspace, status messaging, and results sections.
- `metrics-frontend/src/components/InputWorkspace.vue`
  Responsibility: become the redesigned analysis workspace with mode cards, Chinese-first labels, and richer mode guidance.
- `metrics-frontend/src/components/OverviewCards.vue`
  Responsibility: become the high-level summary card strip with Chinese copy and stronger grouping.
- `metrics-frontend/src/components/RiskPanel.vue`
  Responsibility: surface risk hotspots earlier with clearer empty-state handling.
- `metrics-frontend/src/components/MetricsCharts.vue`
  Responsibility: align chart palette and labels with the dark theme.
- `metrics-frontend/src/components/MetricsTables.vue`
  Responsibility: improve table layout, headings, and dark-theme readability.
- `metrics-frontend/src/components/MetricInfoDrawer.vue`
  Responsibility: become a quieter supporting metric reference panel in Chinese-first copy.
- `metrics-frontend/src/styles/theme.css`
  Responsibility: define the Aura-inspired dark theme, layout tokens, motion, and responsive behavior.
- `metrics-frontend/src/components/__tests__/DashboardFlow.test.js`
  Responsibility: cover branded landing view, entry CTA, results hierarchy, and export actions.
- `metrics-frontend/src/components/__tests__/InputWorkspace.test.js`
  Responsibility: cover the redesigned workspace cards, Chinese labels, and emitted payloads.

### New files to create

- `metrics-frontend/src/assets/branding/logo-minimal.png`
  Responsibility: checked-in copy of `简约LOGO.png` for app navigation and hero branding.
- `metrics-frontend/src/assets/branding/pig-hero.png`
  Responsibility: checked-in hero illustration derived from `BRO系列软件LOGO official Color.png` or `BRO系列软件LOGO official.png`.
- `metrics-frontend/src/assets/branding/pig-empty-state.png`
  Responsibility: checked-in empty-state illustration derived from `油画猪头2.png` or the softer pig image.
- `metrics-frontend/src/components/AppHeader.vue`
  Responsibility: top navigation with logo, product label, and backend status badge.
- `metrics-frontend/src/components/HeroSection.vue`
  Responsibility: homepage hero, product value statement, CTA, and capability cards.
- `metrics-frontend/src/components/EmptyStatePanel.vue`
  Responsibility: homepage-following empty state with pig illustration and next-step guidance.

## Task 1: Lock the app-shell behavior with failing tests

**Files:**
- Modify: `metrics-frontend/src/components/__tests__/DashboardFlow.test.js`
- Test: `metrics-frontend/src/components/__tests__/DashboardFlow.test.js`

- [ ] **Step 1: Write the failing branded-homepage test**

```js
import { fireEvent, render, screen } from '@testing-library/vue'
import App from '../../App.vue'

test('renders the branded homepage before analysis starts', async () => {
  render(App)

  expect(await screen.findByText('Java 度量分析平台')).toBeInTheDocument()
  expect(screen.getByText('暗色可视化代码度量与风险洞察')).toBeInTheDocument()
  expect(screen.getByRole('button', { name: '开始分析' })).toBeInTheDocument()
  expect(screen.getByText('支持代码输入、单文件、多文件与文件夹扫描')).toBeInTheDocument()
})
```

- [ ] **Step 2: Run the dashboard test to verify it fails**

Run: `npm run test -- --run src/components/__tests__/DashboardFlow.test.js`

Expected: FAIL because the current `App.vue` still renders the old English hero and does not expose the new Chinese copy or CTA.

- [ ] **Step 3: Extend the same test file with a results-hierarchy assertion**

```js
test('shows the new results hierarchy after text analysis completes', async () => {
  render(App)

  await fireEvent.click(screen.getByRole('button', { name: '开始分析' }))
  await fireEvent.update(screen.getByLabelText('Java 源码输入区'), 'public class Demo { void go() {} }')
  await fireEvent.click(screen.getByRole('button', { name: '开始分析任务' }))

  expect(await screen.findByText('本次分析概览')).toBeInTheDocument()
  expect(await screen.findByText('风险焦点')).toBeInTheDocument()
  expect(await screen.findByText('复杂度趋势')).toBeInTheDocument()
  expect(await screen.findByRole('button', { name: '导出 CSV' })).toBeInTheDocument()
})
```

- [ ] **Step 4: Re-run the dashboard test to confirm both cases fail for the right reason**

Run: `npm run test -- --run src/components/__tests__/DashboardFlow.test.js`

Expected: FAIL with missing text or role assertions tied to the new homepage and results hierarchy.

- [ ] **Step 5: Commit the red test baseline**

```bash
git add metrics-frontend/src/components/__tests__/DashboardFlow.test.js
git commit -m "test: define branded metrics dashboard flow"
```

## Task 2: Implement the app shell, hero, and empty state

**Files:**
- Create: `metrics-frontend/src/components/AppHeader.vue`
- Create: `metrics-frontend/src/components/HeroSection.vue`
- Create: `metrics-frontend/src/components/EmptyStatePanel.vue`
- Create: `metrics-frontend/src/assets/branding/logo-minimal.png`
- Create: `metrics-frontend/src/assets/branding/pig-hero.png`
- Create: `metrics-frontend/src/assets/branding/pig-empty-state.png`
- Modify: `metrics-frontend/src/App.vue`
- Modify: `metrics-frontend/src/styles/theme.css`
- Test: `metrics-frontend/src/components/__tests__/DashboardFlow.test.js`

- [ ] **Step 1: Copy the approved image sources into repo-local branding assets**

```text
D:\Photos\LOGO 设计\theme\简约LOGO.png
-> metrics-frontend/src/assets/branding/logo-minimal.png

D:\Photos\LOGO 设计\theme\BRO系列软件LOGO official Color.png
-> metrics-frontend/src/assets/branding/pig-hero.png

D:\Photos\LOGO 设计\theme\油画猪头2.png
-> metrics-frontend/src/assets/branding/pig-empty-state.png
```

- [ ] **Step 2: Create `AppHeader.vue` with logo and backend status badge**

```vue
<script setup>
defineProps({
  healthStatus: {
    type: String,
    required: true
  }
})

import logoUrl from '../assets/branding/logo-minimal.png'
</script>

<template>
  <header class="app-header panel-shell">
    <div class="brand-lockup">
      <img :src="logoUrl" alt="BRO Metrics logo" class="brand-logo" />
      <div>
        <p class="brand-kicker">BRO Metrics</p>
        <h1>Java 度量分析平台</h1>
      </div>
    </div>
    <p class="status-chip" :data-state="healthStatus === 'UP' ? 'ok' : 'warn'">
      后端状态：{{ healthStatus }}
    </p>
  </header>
</template>
```

- [ ] **Step 3: Create `HeroSection.vue` and `EmptyStatePanel.vue` for the homepage-first flow**

```vue
<!-- HeroSection.vue -->
<script setup>
import pigHeroUrl from '../assets/branding/pig-hero.png'

defineEmits(['start'])
</script>

<template>
  <section class="hero-shell">
    <div class="hero-copy">
      <p class="eyebrow">Software Quality Assurance</p>
      <h2>暗色可视化代码度量与风险洞察</h2>
      <p class="lede">
        面向 Java 项目的度量分析、复杂度观察与课程汇报输出。
      </p>
      <button type="button" class="primary-button" @click="$emit('start')">开始分析</button>
      <div class="capability-row">
        <article class="capability-card">支持代码输入、单文件、多文件与文件夹扫描</article>
      </div>
    </div>
    <div class="hero-art">
      <img :src="pigHeroUrl" alt="Pig-themed brand illustration" class="hero-image" />
    </div>
  </section>
</template>
```

```vue
<!-- EmptyStatePanel.vue -->
<script setup>
import pigEmptyStateUrl from '../assets/branding/pig-empty-state.png'
</script>

<template>
  <section class="empty-state panel-shell">
    <img :src="pigEmptyStateUrl" alt="Empty state pig artwork" class="empty-state-image" />
    <div>
      <h3>从任意一种输入方式开始</h3>
      <p>选择源码粘贴、文件上传或文件夹扫描后，即可进入结果工作区。</p>
    </div>
  </section>
</template>
```

- [ ] **Step 4: Rewrite `App.vue` to use the new shell components while keeping existing analysis hooks**

```vue
<script setup>
import { onMounted, ref } from 'vue'
import { checkHealth } from './api/metrics'
import AppHeader from './components/AppHeader.vue'
import EmptyStatePanel from './components/EmptyStatePanel.vue'
import HeroSection from './components/HeroSection.vue'
import InputWorkspace from './components/InputWorkspace.vue'
import MetricInfoDrawer from './components/MetricInfoDrawer.vue'
import MetricsCharts from './components/MetricsCharts.vue'
import MetricsTables from './components/MetricsTables.vue'
import OverviewCards from './components/OverviewCards.vue'
import RiskPanel from './components/RiskPanel.vue'
import { useAnalysis } from './composables/useAnalysis'
import { buildCsv, buildMarkdownReport } from './utils/exporters'

const healthStatus = ref('checking')
const analysisAnchor = ref(null)
const { loading, result, error, runTextAnalysis, runFileAnalysis, runFolderAnalysis } = useAnalysis()

function focusAnalysisWorkspace() {
  analysisAnchor.value?.scrollIntoView({ behavior: 'smooth', block: 'start' })
}
</script>

<template>
  <main class="app-shell">
    <AppHeader :health-status="healthStatus" />
    <HeroSection @start="focusAnalysisWorkspace" />
    <section ref="analysisAnchor" class="analysis-zone">
      <InputWorkspace
        @submit-text="runTextAnalysis"
        @submit-file="runFileAnalysis"
        @submit-files="runFileAnalysis"
        @submit-folder="runFolderAnalysis"
      />
    </section>
    <EmptyStatePanel v-if="!loading && !error && !result" />
    <p v-if="loading" class="status-banner">正在分析源码与度量数据...</p>
    <p v-if="error" class="status-banner error">{{ error }}</p>
    <template v-if="result">
      <OverviewCards :summary="result.projectSummary" />
      <RiskPanel :risk-findings="result.riskFindings" />
      <MetricsCharts :method-metrics="result.methodMetrics" />
      <MetricsTables :class-metrics="result.classMetrics" :method-metrics="result.methodMetrics" />
      <MetricInfoDrawer />
    </template>
  </main>
</template>
```

- [ ] **Step 5: Add only the shell-level CSS needed to make the new test pass**

```css
.app-shell {
  max-width: 1280px;
  margin: 0 auto;
  padding: 24px 20px 96px;
}

.hero-shell {
  display: grid;
  grid-template-columns: 1.1fr 0.9fr;
  gap: 24px;
}

.capability-row {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 12px;
}
```

- [ ] **Step 6: Run the dashboard test to verify the app shell passes**

Run: `npm run test -- --run src/components/__tests__/DashboardFlow.test.js`

Expected: PASS for the homepage test, with the results-hierarchy test still failing until the results components are updated.

- [ ] **Step 7: Commit the shell and asset baseline**

```bash
git add metrics-frontend/src/App.vue metrics-frontend/src/components/AppHeader.vue metrics-frontend/src/components/HeroSection.vue metrics-frontend/src/components/EmptyStatePanel.vue metrics-frontend/src/styles/theme.css metrics-frontend/src/assets/branding
git commit -m "feat: add branded app shell for metrics tool"
```

## Task 3: Redesign the analysis workspace and mode cards

**Files:**
- Modify: `metrics-frontend/src/components/InputWorkspace.vue`
- Modify: `metrics-frontend/src/components/__tests__/InputWorkspace.test.js`
- Modify: `metrics-frontend/src/styles/theme.css`
- Test: `metrics-frontend/src/components/__tests__/InputWorkspace.test.js`

- [ ] **Step 1: Rewrite the input-workspace test around Chinese mode cards and richer labels**

```js
import { fireEvent, render, screen } from '@testing-library/vue'
import InputWorkspace from '../InputWorkspace.vue'

test('renders analysis mode cards and emits code-input payloads', async () => {
  const { emitted } = render(InputWorkspace)

  expect(screen.getByRole('button', { name: '代码输入' })).toBeInTheDocument()
  expect(screen.getByRole('button', { name: '单文件分析' })).toBeInTheDocument()
  expect(screen.getByRole('button', { name: '多文件分析' })).toBeInTheDocument()
  expect(screen.getByRole('button', { name: '文件夹扫描' })).toBeInTheDocument()

  await fireEvent.update(screen.getByLabelText('Java 源码输入区'), 'public class Demo {}')
  await fireEvent.click(screen.getByRole('button', { name: '开始分析任务' }))

  expect(emitted()['submit-text'][0][0]).toEqual({
    fileName: 'Snippet.java',
    sourceCode: 'public class Demo {}'
  })
})
```

- [ ] **Step 2: Run the workspace test to verify it fails**

Run: `npm run test -- --run src/components/__tests__/InputWorkspace.test.js`

Expected: FAIL because the current component still renders English mode pills and old labels.

- [ ] **Step 3: Rewrite `InputWorkspace.vue` as a card-based analysis workspace**

```vue
<script setup>
import { computed, ref } from 'vue'

const emit = defineEmits(['submit-text', 'submit-file', 'submit-files', 'submit-folder'])
const mode = ref('text')
const textSource = ref('')
const modes = [
  { key: 'text', label: '代码输入', description: '直接粘贴 Java 源码片段进行快速分析。' },
  { key: 'file', label: '单文件分析', description: '上传一个 .java 文件查看单体结果。' },
  { key: 'files', label: '多文件分析', description: '上传多个 .java 文件建立更完整的上下文。' },
  { key: 'folder', label: '文件夹扫描', description: '选择源码目录获取项目级度量结果。' }
]

const activeMode = computed(() => modes.find((item) => item.key === mode.value))
</script>

<template>
  <section class="workspace-shell panel-shell">
    <div class="workspace-intro">
      <p class="section-kicker">Analysis Workspace</p>
      <h3>选择分析方式</h3>
      <p>{{ activeMode.description }}</p>
    </div>

    <div class="mode-card-grid">
      <button
        v-for="item in modes"
        :key="item.key"
        type="button"
        class="mode-card"
        :class="{ active: mode === item.key }"
        @click="mode = item.key"
      >
        <strong>{{ item.label }}</strong>
        <span>{{ item.description }}</span>
      </button>
    </div>

    <div v-if="mode === 'text'" class="pane">
      <label for="source-input">Java 源码输入区</label>
      <textarea id="source-input" v-model="textSource" rows="14"></textarea>
      <button type="button" class="primary-button" @click="emit('submit-text', { fileName: 'Snippet.java', sourceCode: textSource })">开始分析任务</button>
    </div>
  </section>
</template>
```

- [ ] **Step 4: Add the supporting workspace styles**

```css
.workspace-shell {
  display: grid;
  gap: 20px;
}

.mode-card-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 14px;
}

.mode-card {
  padding: 18px;
  border-radius: 22px;
  text-align: left;
}
```

- [ ] **Step 5: Run the workspace test to verify it passes**

Run: `npm run test -- --run src/components/__tests__/InputWorkspace.test.js`

Expected: PASS.

- [ ] **Step 6: Commit the redesigned workspace**

```bash
git add metrics-frontend/src/components/InputWorkspace.vue metrics-frontend/src/components/__tests__/InputWorkspace.test.js metrics-frontend/src/styles/theme.css
git commit -m "feat: redesign metrics analysis workspace"
```

## Task 4: Rebuild the results hierarchy around summary, risk, and detail

**Files:**
- Modify: `metrics-frontend/src/components/OverviewCards.vue`
- Modify: `metrics-frontend/src/components/RiskPanel.vue`
- Modify: `metrics-frontend/src/components/MetricsCharts.vue`
- Modify: `metrics-frontend/src/components/MetricsTables.vue`
- Modify: `metrics-frontend/src/components/MetricInfoDrawer.vue`
- Modify: `metrics-frontend/src/components/__tests__/DashboardFlow.test.js`
- Modify: `metrics-frontend/src/styles/theme.css`
- Test: `metrics-frontend/src/components/__tests__/DashboardFlow.test.js`

- [ ] **Step 1: Update the dashboard test to assert the new Chinese results structure**

```js
expect(await screen.findByText('本次分析概览')).toBeInTheDocument()
expect(await screen.findByText('风险焦点')).toBeInTheDocument()
expect(await screen.findByText('复杂度趋势')).toBeInTheDocument()
expect(await screen.findByText('类级指标')).toBeInTheDocument()
expect(await screen.findByText('方法级指标')).toBeInTheDocument()
expect(await screen.findByText('指标说明')).toBeInTheDocument()
```

- [ ] **Step 2: Run the dashboard test to verify the results assertions still fail**

Run: `npm run test -- --run src/components/__tests__/DashboardFlow.test.js`

Expected: FAIL because the old results components still render English headings and flat structure.

- [ ] **Step 3: Rewrite `OverviewCards.vue` and `RiskPanel.vue` around a summary-first reading path**

```vue
<!-- OverviewCards.vue -->
<template>
  <section class="results-section">
    <div class="section-heading">
      <p class="section-kicker">Summary</p>
      <h2>本次分析概览</h2>
      <p>快速确认本次扫描覆盖范围与核心规模指标。</p>
    </div>
    <div class="summary-grid">
      <article class="metric-card">
        <span>文件数</span>
        <strong>{{ summary.totalFiles }}</strong>
      </article>
      <article class="metric-card">
        <span>类数量</span>
        <strong>{{ summary.totalClasses }}</strong>
      </article>
      <article class="metric-card">
        <span>方法数</span>
        <strong>{{ summary.totalMethods }}</strong>
      </article>
      <article class="metric-card">
        <span>总 LOC</span>
        <strong>{{ summary.totalLoc }}</strong>
      </article>
    </div>
  </section>
</template>
```

```vue
<!-- RiskPanel.vue -->
<template>
  <section class="results-section">
    <div class="section-heading">
      <p class="section-kicker">Risks</p>
      <h2>风险焦点</h2>
    </div>
    <ul v-if="riskFindings.length" class="risk-list">
      <li v-for="item in riskFindings" :key="item.scope + item.target" class="risk-item">
        <strong>{{ item.target }}</strong>
        <span>{{ item.scope }} · {{ item.message }}</span>
      </li>
    </ul>
    <p v-else class="quiet-note">本次分析未发现关键风险项。</p>
  </section>
</template>
```

- [ ] **Step 4: Rewrite the chart, table, and metric-guide headings for the new hierarchy**

```vue
<!-- MetricsCharts.vue -->
<template>
  <section class="results-section">
    <div class="section-heading">
      <p class="section-kicker">Charts</p>
      <h2>复杂度趋势</h2>
    </div>
    <div ref="complexityChart" class="chart-surface"></div>
  </section>
</template>
```

```vue
<!-- MetricsTables.vue -->
<template>
  <section class="results-section table-stack">
    <h2>类级指标</h2>
    <!-- class metrics table -->
    <h2>方法级指标</h2>
    <!-- method metrics table -->
  </section>
</template>
```

```vue
<!-- MetricInfoDrawer.vue -->
<template>
  <aside class="results-section metric-guide">
    <h2>指标说明</h2>
    <ul>
      <li>WMC：类中方法圈复杂度总和。</li>
      <li>CBO：类与外部类型之间的耦合数量。</li>
      <li>RFC：类可响应的方法集合规模。</li>
      <li>LCOM：类内部职责分散程度的估计值。</li>
    </ul>
  </aside>
</template>
```

- [ ] **Step 5: Add results-section CSS for hierarchy, spacing, and dark panel grouping**

```css
.results-section {
  margin-top: 28px;
  padding: 24px;
  border-radius: 28px;
  background: rgba(9, 14, 25, 0.78);
  border: 1px solid rgba(255, 255, 255, 0.08);
}

.section-heading {
  margin-bottom: 18px;
}

.risk-list {
  display: grid;
  gap: 12px;
}
```

- [ ] **Step 6: Run the dashboard test to verify the new results hierarchy passes**

Run: `npm run test -- --run src/components/__tests__/DashboardFlow.test.js`

Expected: PASS.

- [ ] **Step 7: Commit the results hierarchy update**

```bash
git add metrics-frontend/src/components/OverviewCards.vue metrics-frontend/src/components/RiskPanel.vue metrics-frontend/src/components/MetricsCharts.vue metrics-frontend/src/components/MetricsTables.vue metrics-frontend/src/components/MetricInfoDrawer.vue metrics-frontend/src/components/__tests__/DashboardFlow.test.js metrics-frontend/src/styles/theme.css
git commit -m "feat: reorganize metrics results workspace"
```

## Task 5: Apply the full dark Aura theme and responsive polish

**Files:**
- Modify: `metrics-frontend/src/styles/theme.css`
- Modify: `metrics-frontend/src/components/AppHeader.vue`
- Modify: `metrics-frontend/src/components/HeroSection.vue`
- Modify: `metrics-frontend/src/components/InputWorkspace.vue`
- Modify: `metrics-frontend/src/components/MetricsCharts.vue`
- Test: `metrics-frontend/src/components/__tests__/DashboardFlow.test.js`
- Test: `metrics-frontend/src/components/__tests__/InputWorkspace.test.js`

- [ ] **Step 1: Replace the old light theme tokens with Aura-inspired dark variables**

```css
:root {
  --bg: #070b13;
  --bg-elevated: rgba(14, 20, 33, 0.82);
  --bg-soft: rgba(20, 28, 46, 0.72);
  --text: #f5f7fb;
  --text-muted: #a8b3c7;
  --brand: #ff8b8b;
  --brand-strong: #ff6f91;
  --line: rgba(255, 255, 255, 0.08);
  --glow-cyan: rgba(101, 214, 255, 0.22);
  --shadow-lg: 0 32px 80px rgba(0, 0, 0, 0.38);
}

body {
  margin: 0;
  color: var(--text);
  background:
    radial-gradient(circle at 15% 20%, rgba(255, 111, 145, 0.22), transparent 28%),
    radial-gradient(circle at 80% 10%, rgba(101, 214, 255, 0.18), transparent 24%),
    linear-gradient(180deg, #060912 0%, #0d1321 42%, #101826 100%);
}
```

- [ ] **Step 2: Add shell, hero, button, and panel polish that matches the new palette**

```css
.panel-shell,
.workspace-shell,
.results-section {
  box-shadow: var(--shadow-lg);
  backdrop-filter: blur(18px);
}

.primary-button {
  background: linear-gradient(135deg, var(--brand-strong), #ffb36b);
  color: #09101b;
}

.hero-image {
  filter: drop-shadow(0 20px 40px rgba(0, 0, 0, 0.4));
}
```

- [ ] **Step 3: Add responsive breakpoints so homepage, workspace, and tables remain usable on mobile**

```css
@media (max-width: 960px) {
  .hero-shell,
  .mode-card-grid {
    grid-template-columns: 1fr;
  }

  .capability-row {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 640px) {
  .app-header {
    gap: 12px;
    align-items: flex-start;
  }

  .table-stack {
    overflow-x: auto;
  }
}
```

- [ ] **Step 4: Tune the ECharts option for dark labels and restrained highlight colors**

```js
chart.setOption({
  backgroundColor: 'transparent',
  textStyle: { color: '#f5f7fb' },
  tooltip: { trigger: 'axis' },
  xAxis: {
    type: 'category',
    axisLabel: { color: '#cdd6e3' },
    axisLine: { lineStyle: { color: 'rgba(255,255,255,0.16)' } }
  },
  yAxis: {
    type: 'value',
    axisLabel: { color: '#cdd6e3' },
    splitLine: { lineStyle: { color: 'rgba(255,255,255,0.08)' } }
  },
  series: [{ type: 'bar', itemStyle: { color: '#ff8b8b' } }]
})
```

- [ ] **Step 5: Run the focused frontend tests**

Run: `npm run test -- --run src/components/__tests__/InputWorkspace.test.js src/components/__tests__/DashboardFlow.test.js`

Expected: PASS.

- [ ] **Step 6: Commit the theme and responsive polish**

```bash
git add metrics-frontend/src/styles/theme.css metrics-frontend/src/components/AppHeader.vue metrics-frontend/src/components/HeroSection.vue metrics-frontend/src/components/InputWorkspace.vue metrics-frontend/src/components/MetricsCharts.vue
git commit -m "feat: apply aura dark visual system to metrics frontend"
```

## Task 6: Run regression verification and prepare the handoff

**Files:**
- Modify: `metrics-frontend/src/components/__tests__/DashboardFlow.test.js` (only if a brittle assertion needs cleanup)
- Modify: `metrics-frontend/src/components/__tests__/InputWorkspace.test.js` (only if a brittle assertion needs cleanup)
- Test: `metrics-frontend/package.json`

- [ ] **Step 1: Run the full frontend test suite**

Run: `npm run test -- --run`

Expected: PASS with all Vitest checks green.

- [ ] **Step 2: Run a production build to verify asset imports and CSS compile**

Run: `npm run build`

Expected: PASS with Vite emitting the production bundle and bundled branding assets.

- [ ] **Step 3: Review the final diff for scope drift**

Run: `git status --short`

Expected: only the intended frontend files and branding assets changed.

- [ ] **Step 4: Capture the final implementation commit**

```bash
git add metrics-frontend
git commit -m "feat: ship aura dark redesign for metrics tool"
```

- [ ] **Step 5: Prepare a short validation note for the user**

```text
Validated homepage-first flow, four analysis modes, dark-theme chart and table readability, export actions, and mobile-safe layout collapse points.
```

## Self-Review

### Spec coverage

- Homepage-first branded entry: covered by Tasks 1 and 2.
- Analysis workspace redesign with four retained modes: covered by Task 3.
- Results hierarchy and professional dark data reading: covered by Task 4.
- Aura-inspired visual system, controlled pig imagery, and responsive polish: covered by Task 5.
- Test and build verification without backend changes: covered by Task 6.

### Placeholder scan

- No placeholder markers or shortcut instructions are left in the plan.
- Each implementation task includes exact file paths, test commands, and concrete code snippets.

### Type consistency

- The plan consistently uses `healthStatus`, `runTextAnalysis`, `runFileAnalysis`, and `runFolderAnalysis` from the existing code.
- UI labels stay consistent across tests and component snippets: `开始分析`, `开始分析任务`, `本次分析概览`, `风险焦点`, and `复杂度趋势`.
