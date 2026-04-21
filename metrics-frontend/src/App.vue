<template>
  <main class="app-shell">
    <AppHeader :health-status="healthStatus" />
    <HeroSection @start="focusAnalysisWorkspace" />

    <section :class="result ? 'workspace-stack' : 'workspace-layout'">
      <div ref="workspaceRegion" class="workspace-column" tabindex="-1">
        <InputWorkspace
          @submit-text="runTextAnalysis"
          @submit-file="runFileAnalysis"
          @submit-files="runFileAnalysis"
          @submit-folder="runFolderAnalysis"
        />

        <p v-if="loading" class="status-banner">正在分析源码与度量数据...</p>
        <p v-if="error" class="status-banner error">分析失败：{{ error }}</p>
      </div>

      <EmptyStatePanel v-if="!result" />
    </section>

    <section v-if="result" class="results-shell">
      <OverviewCards :summary="result.projectSummary" />
      <div class="action-row">
        <button type="button" class="primary-button" @click="downloadCsv">导出 CSV</button>
        <button type="button" class="primary-button" @click="downloadMarkdown">导出 Markdown</button>
      </div>
      <RiskPanel :risk-findings="result.riskFindings" />
      <MetricsCharts :method-metrics="result.methodMetrics" />
      <MetricsTables :class-metrics="result.classMetrics" :method-metrics="result.methodMetrics" />
      <MetricInfoDrawer />
    </section>
  </main>
</template>

<script setup>
import { nextTick, onMounted, ref } from 'vue'
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
const workspaceRegion = ref(null)
const { loading, result, error, runTextAnalysis, runFileAnalysis, runFolderAnalysis } = useAnalysis()

onMounted(async () => {
  try {
    const response = await checkHealth()
    healthStatus.value = response.data.status
  } catch {
    healthStatus.value = 'UNAVAILABLE'
  }
})

async function focusAnalysisWorkspace() {
  await nextTick()

  workspaceRegion.value?.focus()
  if (typeof workspaceRegion.value?.scrollIntoView === 'function') {
    workspaceRegion.value.scrollIntoView({ behavior: 'smooth', block: 'start' })
  }
  workspaceRegion.value?.querySelector('textarea, input[type="file"]')?.focus()
}

function downloadBlob(filename, content, type) {
  const blob = new Blob([content], { type })
  const link = document.createElement('a')
  link.href = URL.createObjectURL(blob)
  link.download = filename
  link.click()
  URL.revokeObjectURL(link.href)
}

function downloadCsv() {
  downloadBlob('metrics-report.csv', buildCsv(result.value), 'text/csv')
}

function downloadMarkdown() {
  downloadBlob('metrics-report.md', buildMarkdownReport(result.value), 'text/markdown')
}
</script>
