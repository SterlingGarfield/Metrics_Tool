<template>
  <main class="app-shell">
    <section class="hero">
      <p class="eyebrow">Software Quality Assurance</p>
      <h1>Java Metrics Tool</h1>
      <p class="lede">Analyze Java code, inspect CK metrics, and export report-ready results for your course project.</p>
      <p class="health-status">Backend status: {{ healthStatus }}</p>
    </section>

    <InputWorkspace
      @submit-text="runTextAnalysis"
      @submit-file="runFileAnalysis"
      @submit-files="runFileAnalysis"
      @submit-folder="runFolderAnalysis"
    />

    <p v-if="loading" class="status-banner">Analyzing source set...</p>
    <p v-if="error" class="status-banner error">{{ error }}</p>

    <template v-if="result">
      <OverviewCards :summary="result.projectSummary" />
      <div class="action-row">
        <button type="button" class="primary-button" @click="downloadCsv">Export CSV</button>
        <button type="button" class="primary-button" @click="downloadMarkdown">Export Markdown</button>
      </div>
      <MetricsCharts :method-metrics="result.methodMetrics" />
      <MetricsTables :class-metrics="result.classMetrics" :method-metrics="result.methodMetrics" />
      <RiskPanel :risk-findings="result.riskFindings" />
      <MetricInfoDrawer />
    </template>
  </main>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { checkHealth } from './api/metrics'
import InputWorkspace from './components/InputWorkspace.vue'
import MetricInfoDrawer from './components/MetricInfoDrawer.vue'
import MetricsCharts from './components/MetricsCharts.vue'
import MetricsTables from './components/MetricsTables.vue'
import OverviewCards from './components/OverviewCards.vue'
import RiskPanel from './components/RiskPanel.vue'
import { useAnalysis } from './composables/useAnalysis'
import { buildCsv, buildMarkdownReport } from './utils/exporters'

const healthStatus = ref('checking')
const { loading, result, error, runTextAnalysis, runFileAnalysis, runFolderAnalysis } = useAnalysis()

onMounted(async () => {
  try {
    const response = await checkHealth()
    healthStatus.value = response.data.status
  } catch {
    healthStatus.value = 'UNAVAILABLE'
  }
})

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
