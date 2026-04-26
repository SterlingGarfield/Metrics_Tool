<template>
  <div class="app-frame" :data-theme="theme">
    <div class="app-background-layer" aria-hidden="true"></div>

    <div v-if="!hostUnavailable" class="desktop-titlebar-shell">
      <DesktopTitleBar
        :is-maximized="isMaximized"
        @show-app-menu="showAppMenu"
        @minimize-window="minimizeWindow"
        @toggle-maximize-window="toggleMaximizeWindow"
        @close-window="closeWindow"
      />
    </div>

    <main v-if="!hostUnavailable" class="desktop-workbench">
      <aside class="desktop-sidebar-shell desktop-sidebar-shell--fixed">
        <div class="desktop-content-toolbar" aria-label="工作台状态与显示设置">
          <span class="status-badge" :class="statusClass">
            后端状态：{{ healthStatus }}
          </span>

          <button
            type="button"
            class="theme-toggle"
            :aria-label="toggleLabel"
            @click="toggleTheme"
          >
            <span class="theme-toggle__label">Theme</span>
            <span class="theme-toggle__value">{{ theme === 'dark' ? 'Dark' : 'Light' }}</span>
          </button>
        </div>

        <DesktopSidebar
          v-model:workspace="workspace"
          v-model:codeMode="codeMode"
        />
      </aside>

      <section class="desktop-content-shell desktop-content-shell--with-fixed-sidebar">
        <div class="desktop-content-scroll">
          <section class="workspace-stack">
            <div class="workspace-column">
              <InputWorkspace
                v-model:workspace="workspace"
                v-model:codeMode="codeMode"
                :hide-workspace-switcher="true"
                :use-case-defaults="useCaseDefaults"
                @submit-text="runTextAnalysis"
                @submit-file="runSingleFileAnalysis"
                @submit-files="runMultiFileAnalysis"
                @submit-folder="runFolderAnalysis"
                @submit-design="runDesignAnalysis"
                @submit-estimation="runEstimationAnalysis"
                @submit-use-case-points="runUseCasePointAnalysis"
              />

              <p v-if="loading" class="status-banner">正在整理源码与度量结果...</p>
              <p v-if="error" class="status-banner error">分析失败：{{ error }}</p>
            </div>
          </section>

          <section v-if="result" class="results-shell">
            <div class="action-row">
              <AppActionButton @click="downloadCsv">
                <template #icon>
                  <svg viewBox="0 0 24 24" fill="none">
                    <path d="M12 5V15" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" />
                    <path d="M8.5 11.5L12 15L15.5 11.5" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round" />
                    <path d="M6 18.5H18" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" />
                  </svg>
                </template>
                导出 CSV 报告
              </AppActionButton>
              <AppActionButton variant="secondary" @click="downloadMarkdown">
                <template #icon>
                  <svg viewBox="0 0 24 24" fill="none">
                    <path d="M6 5.75H14L18 9.75V18.25H6V5.75Z" stroke="currentColor" stroke-width="1.8" stroke-linejoin="round" />
                    <path d="M14 5.75V9.75H18" stroke="currentColor" stroke-width="1.8" stroke-linejoin="round" />
                    <path d="M9 13.25H15" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" />
                  </svg>
                </template>
                导出 Markdown 报告
              </AppActionButton>
            </div>
            <OverviewCards v-if="codeMetrics?.available" :summary="codeMetrics.projectSummary" />
            <LkMetricsPanel v-if="lkSummary?.available" :summary="lkSummary" />
            <RiskPanel
              v-if="codeMetrics?.available || riskFindings.length"
              :risk-findings="riskFindings"
            />
            <MetricsCharts
              v-if="methodMetrics.length"
              :method-metrics="methodMetrics"
              :theme="theme"
            />
            <MetricsTables
              v-if="classMetrics.length || methodMetrics.length"
              :class-metrics="classMetrics"
              :method-metrics="methodMetrics"
            />
            <DesignMetricsPanel v-if="designMetrics?.available" :summary="designMetrics" />
            <EstimationPanel v-if="estimationMetrics?.available" :summary="estimationMetrics" />
            <MetricInfoDrawer v-if="codeMetrics?.available" />
          </section>
        </div>
      </section>
    </main>

    <main v-else class="desktop-host-shell">
      <DesktopHostUnavailablePanel />
    </main>
  </div>
</template>

<script setup>
import { computed, defineAsyncComponent, onMounted, ref } from 'vue'
import { checkHealth, exportCsv, exportMarkdown } from './api/metrics'
import AppActionButton from './components/AppActionButton.vue'
import DesktopHostUnavailablePanel from './components/DesktopHostUnavailablePanel.vue'
import DesktopSidebar from './components/DesktopSidebar.vue'
import DesktopTitleBar from './components/DesktopTitleBar.vue'
import InputWorkspace from './components/InputWorkspace.vue'
import OverviewCards from './components/OverviewCards.vue'
import RiskPanel from './components/RiskPanel.vue'
import { useAnalysis } from './composables/useAnalysis'
import { useDesktopWindow } from './composables/useDesktopWindow'
import { useTheme } from './composables/useTheme'

const MetricsCharts = defineAsyncComponent(() => import('./components/MetricsCharts.vue'))
const MetricsTables = defineAsyncComponent(() => import('./components/MetricsTables.vue'))
const LkMetricsPanel = defineAsyncComponent(() => import('./components/LkMetricsPanel.vue'))
const DesignMetricsPanel = defineAsyncComponent(() => import('./components/DesignMetricsPanel.vue'))
const EstimationPanel = defineAsyncComponent(() => import('./components/EstimationPanel.vue'))
const MetricInfoDrawer = defineAsyncComponent(() => import('./components/MetricInfoDrawer.vue'))

const healthStatus = ref('checking')
const workspace = ref('code')
const codeMode = ref('text')
const hostUnavailable = ref(!hasDesktopHost())
const { theme, toggleTheme } = useTheme()
const {
  loading,
  result,
  error,
  runTextAnalysis,
  runSingleFileAnalysis,
  runMultiFileAnalysis,
  runFolderAnalysis,
  runDesignAnalysis,
  runEstimationAnalysis,
  runUseCasePointAnalysis
} = useAnalysis()
const {
  isMaximized,
  minimizeWindow,
  toggleMaximizeWindow,
  closeWindow,
  showAppMenu
} = useDesktopWindow(computed(() => !hostUnavailable.value))

const codeMetrics = computed(() => result.value?.codeMetrics ?? null)
const designMetrics = computed(() => result.value?.designMetrics ?? null)
const estimationMetrics = computed(() => result.value?.estimationMetrics ?? null)
const classMetrics = computed(() => codeMetrics.value?.classMetrics ?? [])
const methodMetrics = computed(() => codeMetrics.value?.methodMetrics ?? [])
const lkSummary = computed(() => codeMetrics.value?.lkSummary ?? null)
const riskFindings = computed(() => result.value?.riskFindings ?? [])
const useCaseDefaults = computed(() => {
  if (!designMetrics.value?.available || !String(designMetrics.value.diagramType || '').includes('use-case')) {
    return null
  }

  return {
    actorCount: designMetrics.value.actorCount ?? 0,
    useCaseCount: designMetrics.value.useCaseCount ?? 0
  }
})
const statusClass = computed(() => {
  if (healthStatus.value === 'UP') {
    return 'is-up'
  }

  if (healthStatus.value === 'checking') {
    return 'is-checking'
  }

  return 'is-down'
})
const toggleLabel = computed(() => (
  theme.value === 'dark' ? '切换到亮色主题' : '切换到暗色主题'
))

onMounted(async () => {
  if (hostUnavailable.value) {
    healthStatus.value = 'UNAVAILABLE'
    return
  }

  try {
    const response = await checkHealth()
    healthStatus.value = response.data.status
  } catch (runtimeError) {
    if (!hasDesktopHost()) {
      hostUnavailable.value = true
    }
    healthStatus.value = 'UNAVAILABLE'
  }
})

function hasDesktopHost() {
  if (typeof window === 'undefined') {
    return false
  }

  return Boolean(window.metricsDesktop && typeof window.metricsDesktop.getAppStatus === 'function')
}

async function downloadCsv() {
  if (!result.value) {
    return
  }

  await exportCsv(result.value)
}

async function downloadMarkdown() {
  if (!result.value) {
    return
  }

  await exportMarkdown(result.value)
}
</script>
