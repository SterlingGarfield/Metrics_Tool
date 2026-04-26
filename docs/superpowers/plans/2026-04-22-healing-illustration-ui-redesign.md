# Healing Illustration UI Redesign Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Re-theme the full Metrics Tool into a healing illustration product, integrate pig-branded assets and IconPark-style action icons, and ship matching Windows executable/shortcut icons without breaking the current analysis workflow.

**Architecture:** Keep the existing Vue/Electron structure intact and layer the redesign through asset replacement, component restyling, a rewritten theme token system, and desktop packaging icon configuration. Functional interaction and analysis contracts stay unchanged; only the visual shell, branded assets, and icon chain are replaced.

**Tech Stack:** Vue 3, Vite, Vitest, Electron Builder, Node.js asset scripts, Windows `.ico` packaging

---

## File Structure

**Frontend**
- Modify: `metrics-frontend/package.json`
- Modify: `metrics-frontend/src/App.vue`
- Modify: `metrics-frontend/src/styles/theme.css`
- Modify: `metrics-frontend/src/components/AppHeader.vue`
- Modify: `metrics-frontend/src/components/HeroSection.vue`
- Modify: `metrics-frontend/src/components/EmptyStatePanel.vue`
- Modify: `metrics-frontend/src/components/InputWorkspace.vue`
- Modify: `metrics-frontend/src/components/OverviewCards.vue`
- Modify: `metrics-frontend/src/components/RiskPanel.vue`
- Modify: `metrics-frontend/src/components/MetricsCharts.vue`
- Modify: `metrics-frontend/src/components/MetricsTables.vue`
- Modify: `metrics-frontend/src/components/MetricInfoDrawer.vue`
- Create: `metrics-frontend/src/components/AppActionButton.vue`
- Create: `metrics-frontend/src/components/__tests__/HeroSection.test.js`
- Create: `metrics-frontend/src/components/__tests__/AppHeader.test.js`
- Modify: `metrics-frontend/src/components/__tests__/InputWorkspace.test.js`
- Modify: `metrics-frontend/src/components/__tests__/DashboardFlow.test.js`

**Brand assets**
- Create: `metrics-frontend/src/assets/branding/logo-healing.png`
- Create: `metrics-frontend/src/assets/branding/hero-sleepy-pig.png`
- Create: `metrics-frontend/src/assets/branding/empty-state-sleepy-pig.png`
- Create: `metrics-frontend/src/assets/branding/results-pig-accent.png`
- Create: `metrics-frontend/src/assets/branding/multicultural-companions.png`

**Desktop packaging**
- Modify: `metrics-desktop/electron-builder.json`
- Modify: `metrics-desktop/package.json`
- Create: `metrics-desktop/installer/metrics-tool.ico`
- Create: `metrics-desktop/scripts/generate-brand-icons.mjs`
- Modify: `metrics-desktop/test/installer-config.test.cjs`

---

### Task 1: Prepare Brand Assets and Desktop Icon Source

**Files:**
- Create: `metrics-frontend/src/assets/branding/logo-healing.png`
- Create: `metrics-frontend/src/assets/branding/hero-sleepy-pig.png`
- Create: `metrics-frontend/src/assets/branding/empty-state-sleepy-pig.png`
- Create: `metrics-frontend/src/assets/branding/results-pig-accent.png`
- Create: `metrics-frontend/src/assets/branding/multicultural-companions.png`
- Create: `metrics-desktop/scripts/generate-brand-icons.mjs`
- Create: `metrics-desktop/installer/metrics-tool.ico`
- Test: `metrics-desktop/test/installer-config.test.cjs`

- [ ] **Step 1: Write the failing desktop-icon test**

```js
const assert = require('node:assert/strict')
const fs = require('node:fs')
const path = require('node:path')

function run() {
  const desktopRoot = path.resolve(__dirname, '..')
  const builder = JSON.parse(
    fs.readFileSync(path.join(desktopRoot, 'electron-builder.json'), 'utf8')
  )

  assert.equal(fs.existsSync(path.join(desktopRoot, 'installer', 'metrics-tool.ico')), true)
  assert.equal(fs.existsSync(path.join(desktopRoot, '..', 'metrics-frontend', 'src', 'assets', 'branding', 'logo-healing.png')), true)
  assert.equal(builder.win.icon, 'installer/metrics-tool.ico')
  assert.equal(builder.nsis.installerIcon, 'installer/metrics-tool.ico')
  assert.equal(builder.nsis.uninstallerIcon, 'installer/metrics-tool.ico')
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `node test/installer-config.test.cjs`
Expected: FAIL because the healing logo asset and `.ico` file do not exist yet, and the Electron Builder config has no icon fields.

- [ ] **Step 3: Create the asset foundation and icon generator**

Create repo-local copies derived from [简约LOGO.png](</D:/Photos/LOGO 设计/theme/简约LOGO.png>) and the approved art direction:

- `logo-healing.png`: cleaned flat pig-face app logo
- `hero-sleepy-pig.png`: seated pig holding flowers, sleepy
- `empty-state-sleepy-pig.png`: softer empty-state illustration
- `results-pig-accent.png`: low-contrast background pig accent
- `multicultural-companions.png`: simplified supporting human figures

Create the desktop icon generator:

```js
import fs from 'node:fs/promises'
import path from 'node:path'
import pngToIco from 'png-to-ico'

const desktopRoot = new URL('..', import.meta.url).pathname
const sourcePng = path.resolve(desktopRoot, '..', '..', 'metrics-frontend', 'src', 'assets', 'branding', 'logo-healing.png')
const targetIco = path.resolve(desktopRoot, '..', 'installer', 'metrics-tool.ico')

const buffer = await pngToIco(sourcePng)
await fs.writeFile(targetIco, buffer)
console.log(`Generated ${targetIco}`)
```

- [ ] **Step 4: Run the icon test again**

Run: `node test/installer-config.test.cjs`
Expected: PASS

- [ ] **Step 5: Commit**

```bash
git add metrics-frontend/src/assets/branding metrics-desktop/scripts/generate-brand-icons.mjs metrics-desktop/installer/metrics-tool.ico metrics-desktop/test/installer-config.test.cjs
git commit -m "feat: add healing brand assets and desktop icon source"
```

### Task 2: Add Shared Action Buttons and Header/Hero Branding

**Files:**
- Modify: `metrics-frontend/package.json`
- Create: `metrics-frontend/src/components/AppActionButton.vue`
- Modify: `metrics-frontend/src/components/AppHeader.vue`
- Modify: `metrics-frontend/src/components/HeroSection.vue`
- Create: `metrics-frontend/src/components/__tests__/AppHeader.test.js`
- Create: `metrics-frontend/src/components/__tests__/HeroSection.test.js`

- [ ] **Step 1: Write the failing hero/header tests**

```js
test('renders the healing hero and mascot art direction', () => {
  render(HeroSection)

  expect(screen.getByRole('heading', { name: '让 Java 度量分析像一次温柔整理' })).toBeInTheDocument()
  expect(screen.getByText('用更轻松的方式查看复杂度、风险与结构信号')).toBeInTheDocument()
  expect(screen.getByAltText('捧着鲜花打瞌睡的小猪主插画')).toBeInTheDocument()
})
```

```js
test('renders the healing brand lockup and live status', () => {
  render(AppHeader, { props: { healthStatus: 'UP' } })

  expect(screen.getByText('Piggy Metrics')).toBeInTheDocument()
  expect(screen.getByText('Java 度量分析平台')).toBeInTheDocument()
  expect(screen.getByAltText('Piggy Metrics 应用标志')).toBeInTheDocument()
  expect(screen.getByText('后端状态：UP')).toBeInTheDocument()
})
```

- [ ] **Step 2: Run tests to verify they fail**

Run:
- `npm run test -- --run src/components/__tests__/HeroSection.test.js`
- `npm run test -- --run src/components/__tests__/AppHeader.test.js`

Expected: FAIL because the components still use the old dark-brand copy and old assets.

- [ ] **Step 3: Add the shared icon button and rewrite header/hero**

Install the icon package:

```json
"dependencies": {
  "@icon-park/vue-next": "^1.4.2",
  "axios": "^1.7.2",
  "echarts": "^5.5.1",
  "vue": "^3.5.13"
}
```

Create a reusable flat action button:

```vue
<template>
  <button type="button" class="app-action-button">
    <span v-if="$slots.icon" class="app-action-button__icon"><slot name="icon" /></span>
    <span class="app-action-button__label"><slot /></span>
  </button>
</template>
```

Rewrite `HeroSection.vue` to use the new copy and new mascot asset:

```vue
<h1>让 Java 度量分析像一次温柔整理</h1>
<p class="hero-lede">用更轻松的方式查看复杂度、风险与结构信号</p>
<img class="hero-illustration" :src="heroSleepyPig" alt="捧着鲜花打瞌睡的小猪主插画" />
```

- [ ] **Step 4: Run the tests again**

Run:
- `npm run test -- --run src/components/__tests__/HeroSection.test.js`
- `npm run test -- --run src/components/__tests__/AppHeader.test.js`

Expected: PASS

- [ ] **Step 5: Commit**

```bash
git add metrics-frontend/package.json metrics-frontend/src/components/AppActionButton.vue metrics-frontend/src/components/AppHeader.vue metrics-frontend/src/components/HeroSection.vue metrics-frontend/src/components/__tests__/HeroSection.test.js metrics-frontend/src/components/__tests__/AppHeader.test.js
git commit -m "feat: add healing header hero and shared action buttons"
```

### Task 3: Rewrite the Global Theme and Input Workspace

**Files:**
- Modify: `metrics-frontend/src/styles/theme.css`
- Modify: `metrics-frontend/src/components/EmptyStatePanel.vue`
- Modify: `metrics-frontend/src/components/InputWorkspace.vue`
- Modify: `metrics-frontend/src/components/__tests__/InputWorkspace.test.js`

- [ ] **Step 1: Write the failing input-workspace test**

```js
test('shows the healing workspace guidance and icon-led file actions', async () => {
  render(InputWorkspace, { props: { desktopEnabled: true } })

  expect(screen.getByText('用一组清晰、温柔的模式卡片开始分析')).toBeInTheDocument()
  expect(screen.getByRole('button', { name: '从系统中选择 Java 文件' })).toBeInTheDocument()
  expect(screen.getByRole('button', { name: '从系统中选择多个 Java 文件' })).toBeInTheDocument()
  expect(screen.getByRole('button', { name: '从系统中选择源码文件夹' })).toBeInTheDocument()
})
```

- [ ] **Step 2: Run test to verify it fails**

Run: `npm run test -- --run src/components/__tests__/InputWorkspace.test.js`
Expected: FAIL because the old copy and old visual shell are still present.

- [ ] **Step 3: Rewrite the theme tokens and workspace surfaces**

Replace the top-level theme with healing tokens:

```css
:root {
  --ink: #4f4037;
  --paper: #fff8f1;
  --paper-soft: #fff2ea;
  --panel: rgba(255, 255, 255, 0.86);
  --panel-strong: #fffdf9;
  --line: rgba(144, 119, 108, 0.16);
  --accent: #f2a67e;
  --accent-strong: #ea8962;
  --success: #85b79d;
  --danger: #df8d85;
  --shadow: 0 20px 44px rgba(171, 134, 119, 0.14);
  font-family: "Segoe UI", "Microsoft YaHei", sans-serif;
}
```

Update the workspace copy and keep the same interaction model:

```vue
<p class="workspace-summary">
  用一组清晰、温柔的模式卡片开始分析，让输入选择和结果整理都更轻松。
</p>
```

Use `AppActionButton` for desktop selection actions so the icon rhythm is shared with the rest of the product.

- [ ] **Step 4: Run the input test again**

Run: `npm run test -- --run src/components/__tests__/InputWorkspace.test.js`
Expected: PASS

- [ ] **Step 5: Commit**

```bash
git add metrics-frontend/src/styles/theme.css metrics-frontend/src/components/EmptyStatePanel.vue metrics-frontend/src/components/InputWorkspace.vue metrics-frontend/src/components/__tests__/InputWorkspace.test.js
git commit -m "feat: apply healing theme tokens to workspace and empty states"
```

### Task 4: Redesign the Results Workspace Without Losing Readability

**Files:**
- Modify: `metrics-frontend/src/App.vue`
- Modify: `metrics-frontend/src/components/OverviewCards.vue`
- Modify: `metrics-frontend/src/components/RiskPanel.vue`
- Modify: `metrics-frontend/src/components/MetricsCharts.vue`
- Modify: `metrics-frontend/src/components/MetricsTables.vue`
- Modify: `metrics-frontend/src/components/MetricInfoDrawer.vue`
- Modify: `metrics-frontend/src/components/__tests__/DashboardFlow.test.js`

- [ ] **Step 1: Write the failing dashboard-flow test**

```js
expect(await screen.findByText('在温柔的阅读节奏里查看这次分析概览')).toBeInTheDocument()
expect(await screen.findByText('优先留意这些需要温柔修整的风险信号')).toBeInTheDocument()
expect(await screen.findByRole('button', { name: '导出 CSV 报告' })).toBeInTheDocument()
expect(await screen.findByRole('button', { name: '导出 Markdown 报告' })).toBeInTheDocument()
```

- [ ] **Step 2: Run test to verify it fails**

Run: `npm run test -- --run src/components/__tests__/DashboardFlow.test.js`
Expected: FAIL because the results page still uses the previous dark analytical copy and button set.

- [ ] **Step 3: Restyle result modules while keeping table/chart clarity**

Update overview copy:

```vue
<p class="results-section-copy">
  在温柔的阅读节奏里查看这次分析概览，快速判断代码规模与结构体量。
</p>
```

Update risk copy:

```vue
<p class="results-section-copy">
  优先留意这些需要温柔修整的风险信号，再决定接下来最值得处理的代码区域。
</p>
```

Keep chart plot backgrounds transparent but change shell colors:

```js
tooltip: {
  backgroundColor: 'rgba(255, 250, 245, 0.96)',
  borderColor: 'rgba(210, 176, 160, 0.32)',
  textStyle: { color: '#5e4b42' }
}
```

Keep tables clean:

```css
.table-stack table {
  background: rgba(255, 255, 255, 0.92);
  color: #4f4037;
}
```

- [ ] **Step 4: Run dashboard-flow and full frontend tests**

Run:
- `npm run test -- --run src/components/__tests__/DashboardFlow.test.js`
- `npm run test -- --run`

Expected: PASS

- [ ] **Step 5: Commit**

```bash
git add metrics-frontend/src/App.vue metrics-frontend/src/components/OverviewCards.vue metrics-frontend/src/components/RiskPanel.vue metrics-frontend/src/components/MetricsCharts.vue metrics-frontend/src/components/MetricsTables.vue metrics-frontend/src/components/MetricInfoDrawer.vue metrics-frontend/src/components/__tests__/DashboardFlow.test.js
git commit -m "feat: redesign the healing results workspace"
```

### Task 5: Wire the Windows App Icon Chain and Verify Desktop Packaging

**Files:**
- Modify: `metrics-desktop/electron-builder.json`
- Modify: `metrics-desktop/package.json`
- Modify: `metrics-desktop/test/installer-config.test.cjs`
- Create: `metrics-desktop/installer/metrics-tool.ico`

- [ ] **Step 1: Expand the failing installer-config test**

```js
assert.equal(builder.win.icon, 'installer/metrics-tool.ico')
assert.equal(builder.nsis.installerIcon, 'installer/metrics-tool.ico')
assert.equal(builder.nsis.uninstallerIcon, 'installer/metrics-tool.ico')
assert.equal(builder.nsis.installerHeaderIcon, 'installer/metrics-tool.ico')
```

- [ ] **Step 2: Run test to verify it fails**

Run: `npm test`
Expected: FAIL because the builder config and icon asset chain are still incomplete.

- [ ] **Step 3: Configure the packaging chain**

Update `electron-builder.json`:

```json
"win": {
  "target": ["nsis"],
  "artifactName": "MetricsToolDesktop-${version}.${ext}",
  "icon": "installer/metrics-tool.ico"
},
"nsis": {
  "oneClick": false,
  "allowToChangeInstallationDirectory": true,
  "installerIcon": "installer/metrics-tool.ico",
  "uninstallerIcon": "installer/metrics-tool.ico",
  "installerHeaderIcon": "installer/metrics-tool.ico",
  "include": "installer/custom-installer.nsh"
}
```

Add an icon-generation script entry:

```json
"scripts": {
  "generate-icons": "node scripts/generate-brand-icons.mjs",
  "test": "node test/runtime-paths.test.cjs && node test/boot-state.test.cjs && node test/java-runtime.test.cjs && node test/window-lifecycle.test.cjs && node test/workspace-launch.test.cjs && node test/installer-config.test.cjs && node test/frontend-build-paths.test.cjs"
}
```

- [ ] **Step 4: Run packaging verification**

Run:
- `npm test`
- `powershell -ExecutionPolicy Bypass -File .\metrics-desktop\scripts\build-installer.ps1`

Expected:
- desktop tests PASS
- installer rebuilds successfully
- `metrics-desktop/dist/MetricsToolDesktop-0.0.1.exe` is regenerated with the new icon chain configured

- [ ] **Step 5: Commit**

```bash
git add metrics-desktop/electron-builder.json metrics-desktop/package.json metrics-desktop/test/installer-config.test.cjs metrics-desktop/installer/metrics-tool.ico metrics-desktop/scripts/generate-brand-icons.mjs
git commit -m "feat: wire healing brand icons into desktop packaging"
```

### Task 6: Final Visual QA and Regression Sweep

**Files:**
- Modify: `README.md`
- Modify: `metrics-frontend/src/components/__tests__/DashboardFlow.test.js`
- Modify: `metrics-frontend/src/components/__tests__/InputWorkspace.test.js`

- [ ] **Step 1: Add the final regression expectations**

```js
expect(screen.getByAltText('Piggy Metrics 应用标志')).toBeInTheDocument()
expect(screen.getByAltText('捧着鲜花打瞌睡的小猪主插画')).toBeInTheDocument()
```

- [ ] **Step 2: Run the regression suite to verify failures or missing assertions**

Run:
- `npm run test -- --run`
- `npm test`

Expected: Any missing copy, asset, or icon integration should be exposed before final polish.

- [ ] **Step 3: Update the user-facing runbook**

Add a short branding note in `README.md`:

```md
## Desktop Experience

- healing illustration UI across homepage, workspace, and result views
- pig-branded Windows executable and shortcut icon
- four existing analysis modes preserved
```

- [ ] **Step 4: Run the full verification set**

Run:
- `npm run test -- --run`
- `npm run build`
- `npm test`
- `npm run check`
- `powershell -ExecutionPolicy Bypass -File .\metrics-desktop\scripts\build-installer.ps1`

Expected: PASS across frontend, desktop validation, and packaging.

- [ ] **Step 5: Commit**

```bash
git add README.md metrics-frontend/src/components/__tests__/DashboardFlow.test.js metrics-frontend/src/components/__tests__/InputWorkspace.test.js
git commit -m "docs: finalize healing illustration ui verification and runbook"
```

---

## Self-Review

**Spec coverage:** The plan covers the approved design areas: full-product healing style, medium-illustration density in results, background-only illustration rules, pig-based brand assets, IconPark-style action icons, and Windows executable/shortcut icon replacement.

**Placeholder scan:** There are no `TBD` or `TODO` placeholders. Asset creation is specified as exact repo-local filenames and desktop packaging outputs rather than an abstract “add art later” instruction.

**Type consistency:** The plan consistently uses the current component and packaging file structure. Icon packaging always points at `metrics-desktop/installer/metrics-tool.ico`, and all frontend brand assets live under `metrics-frontend/src/assets/branding/`.

