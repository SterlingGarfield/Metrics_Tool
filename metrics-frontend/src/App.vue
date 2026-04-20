<template>
  <main class="app-shell">
    <section class="hero">
      <p class="eyebrow">Software Quality Assurance</p>
      <h1>Metrics Workbench</h1>
      <p class="lede">Run three core course mainlines in one local workflow: code metrics, design diagram metrics, and project estimation.</p>
      <p class="health-status">Code backend: {{ healthStatus }}</p>
      <p class="health-status">Recognition service: {{ recognitionStatus }}</p>
    </section>

    <section class="panel">
      <h2>Three Mainlines</h2>
      <div class="mainline-grid">
        <article class="mainline-card">
          <h3>Mainline 1: Code Metrics</h3>
          <p>AST-based software metrics from Java source (project/class/method/risk).</p>
          <p class="mainline-state">Status: {{ codeTrackStatus }}</p>
        </article>
        <article class="mainline-card">
          <h3>Mainline 2: Design Diagram Metrics</h3>
          <p>Structured + image diagram analysis with relation recovery and metric extraction.</p>
          <p class="mainline-state">Status: {{ diagramTrackStatus }}</p>
        </article>
        <article class="mainline-card">
          <h3>Mainline 3: Project Estimation</h3>
          <p>Heuristic + optional standard UCP estimation with workload, cost, schedule, and staffing.</p>
          <p class="mainline-state">Status: {{ estimationTrackStatus }}</p>
        </article>
      </div>
    </section>

    <section class="panel">
      <h2>Unified Export</h2>
      <p class="lede">Export a combined report across all available mainline results.</p>
      <div class="action-row">
        <button type="button" class="primary-button" :disabled="!hasAnyTrackData" @click="downloadCsv">Export CSV</button>
        <button type="button" class="primary-button" :disabled="!hasAnyTrackData" @click="downloadMarkdown">Export Markdown</button>
      </div>
      <p v-if="!hasAnyTrackData" class="status-banner">Run at least one mainline to enable export.</p>
    </section>

    <section class="panel">
      <h2>Mainline 1: Code Metrics</h2>
      <InputWorkspace
        @submit-text="handleSubmitText"
        @submit-file="handleSubmitFile"
        @submit-files="handleSubmitFile"
        @submit-folder="handleSubmitFolder"
      />
      <p v-if="loading" class="status-banner">Analyzing source set...</p>
      <p v-if="error" class="status-banner error">{{ error }}</p>
    </section>

    <template v-if="result">
      <OverviewCards :summary="result.projectSummary" />
      <MetricsCharts :method-metrics="result.methodMetrics" />
      <MetricsTables :class-metrics="result.classMetrics" :method-metrics="result.methodMetrics" />
      <RiskPanel :risk-findings="result.riskFindings" />
      <MetricInfoDrawer />
    </template>

    <section class="panel">
      <h2>Mainline 2: Design Diagram Metrics</h2>
      <div class="two-column-grid">
        <form class="pane" @submit.prevent="runStructured">
          <h3>Structured Input</h3>
          <label for="structured-type">Diagram Type</label>
          <select id="structured-type" v-model="structuredType">
            <option value="class">class</option>
            <option value="flow">flow</option>
            <option value="usecase">usecase</option>
          </select>
          <label for="structured-file">Diagram File (.puml/.mmd)</label>
          <input id="structured-file" type="file" accept=".puml,.mmd,.txt" @change="onStructuredFileChange" />
          <button type="submit" class="primary-button" :disabled="diagramLoading">Analyze Structured Diagram</button>
        </form>

        <form class="pane" @submit.prevent="runImage">
          <h3>Image Input</h3>
          <label for="image-type">Diagram Type</label>
          <select id="image-type" v-model="imageType">
            <option value="class">class</option>
            <option value="flow">flow</option>
            <option value="usecase">usecase</option>
          </select>
          <label for="image-file">Diagram Image (.png/.jpg)</label>
          <input id="image-file" type="file" accept=".png,.jpg,.jpeg" @change="onImageFileChange" />
          <button type="submit" class="primary-button" :disabled="diagramLoading">Analyze Image Diagram</button>
        </form>
      </div>

      <p v-if="diagramLoading" class="status-banner">Running diagram recognition...</p>
      <p v-if="diagramError" class="status-banner error">{{ diagramError }}</p>

      <div v-if="diagramResult" class="result-box">
        <h3>Diagram Result</h3>
        <p>Type: {{ diagramResult.diagramType }} | Source: {{ diagramResult.sourceType }}</p>
        <p>Confidence: {{ diagramResult.confidence?.overall ?? 'n/a' }}</p>
        <pre>{{ JSON.stringify(diagramResult, null, 2) }}</pre>
      </div>
    </section>

    <section class="panel">
      <h2>Mainline 3: Project Estimation</h2>
      <form class="estimation-grid" @submit.prevent="runEstimation">
        <label for="estimate-diagram-type">Diagram Type</label>
        <select id="estimate-diagram-type" v-model="estimateForm.diagramType">
          <option value="class">class</option>
          <option value="flow">flow</option>
          <option value="usecase">usecase</option>
        </select>

        <label for="estimate-loc">Total LoC</label>
        <input id="estimate-loc" type="number" min="0" v-model.number="estimateForm.totalLoc" />

        <label for="estimate-classes">Class Count</label>
        <input id="estimate-classes" type="number" min="0" v-model.number="estimateForm.classCount" />

        <label for="estimate-relations">Relationship Count</label>
        <input id="estimate-relations" type="number" min="0" v-model.number="estimateForm.relationshipCount" />

        <label for="estimate-usecases">Use Case Count</label>
        <input id="estimate-usecases" type="number" min="0" v-model.number="estimateForm.useCaseCount" />

        <label for="estimate-decisions">Decision Node Count</label>
        <input id="estimate-decisions" type="number" min="0" v-model.number="estimateForm.decisionNodeCount" />

        <label for="estimate-simple-actors">Simple Actor Count (Optional)</label>
        <input id="estimate-simple-actors" type="number" min="0" v-model.number="estimateForm.simpleActorCount" />

        <label for="estimate-average-actors">Average Actor Count (Optional)</label>
        <input id="estimate-average-actors" type="number" min="0" v-model.number="estimateForm.averageActorCount" />

        <label for="estimate-complex-actors">Complex Actor Count (Optional)</label>
        <input id="estimate-complex-actors" type="number" min="0" v-model.number="estimateForm.complexActorCount" />

        <label for="estimate-simple-usecases">Simple Use Case Count (Optional)</label>
        <input id="estimate-simple-usecases" type="number" min="0" v-model.number="estimateForm.simpleUseCaseCount" />

        <label for="estimate-average-usecases">Average Use Case Count (Optional)</label>
        <input id="estimate-average-usecases" type="number" min="0" v-model.number="estimateForm.averageUseCaseCount" />

        <label for="estimate-complex-usecases">Complex Use Case Count (Optional)</label>
        <input id="estimate-complex-usecases" type="number" min="0" v-model.number="estimateForm.complexUseCaseCount" />

        <label for="estimate-tcf">Technical Complexity Factor (Optional)</label>
        <input id="estimate-tcf" type="number" min="0.6" max="1.4" step="0.01" v-model.number="estimateForm.technicalComplexityFactor" />

        <label for="estimate-ef">Environmental Factor (Optional)</label>
        <input id="estimate-ef" type="number" min="0.6" max="1.4" step="0.01" v-model.number="estimateForm.environmentalFactor" />

        <label for="estimate-cost-rate">Cost Rate / Person-Month</label>
        <input id="estimate-cost-rate" type="number" min="1" v-model.number="estimateForm.costRatePerPersonMonth" />

        <label for="estimate-schedule">Target Schedule (Months)</label>
        <input id="estimate-schedule" type="number" min="0.1" step="0.1" v-model.number="estimateForm.targetScheduleMonths" />

        <button type="submit" class="primary-button" :disabled="estimationLoading">Run Estimation</button>
      </form>

      <p v-if="estimationLoading" class="status-banner">Estimating project workload...</p>
      <p v-if="estimationError" class="status-banner error">{{ estimationError }}</p>

      <div v-if="estimationResult" class="result-box">
        <h3>Estimation Result</h3>
        <pre>{{ JSON.stringify(estimationResult, null, 2) }}</pre>
      </div>
    </section>
  </main>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import {
  analyzeImageDiagram,
  analyzeStructuredDiagram,
  checkHealth,
  checkRecognitionHealth,
  estimateProject,
  fetchModelsStatus
} from './api/metrics'
import InputWorkspace from './components/InputWorkspace.vue'
import MetricInfoDrawer from './components/MetricInfoDrawer.vue'
import MetricsCharts from './components/MetricsCharts.vue'
import MetricsTables from './components/MetricsTables.vue'
import OverviewCards from './components/OverviewCards.vue'
import RiskPanel from './components/RiskPanel.vue'
import { useAnalysis } from './composables/useAnalysis'
import { buildCsv, buildMarkdownReport } from './utils/exporters'

const healthStatus = ref('checking')
const recognitionStatus = ref('checking')
const { loading, result, error, runTextAnalysis, runFileAnalysis, runFolderAnalysis } = useAnalysis()

const structuredType = ref('class')
const imageType = ref('class')
const structuredFile = ref(null)
const imageFile = ref(null)
const diagramLoading = ref(false)
const diagramError = ref('')
const diagramResult = ref(null)

const estimationLoading = ref(false)
const estimationError = ref('')
const estimationResult = ref(null)
const estimateForm = ref({
  diagramType: 'class',
  totalLoc: null,
  classCount: null,
  relationshipCount: null,
  useCaseCount: null,
  decisionNodeCount: null,
  simpleActorCount: null,
  averageActorCount: null,
  complexActorCount: null,
  simpleUseCaseCount: null,
  averageUseCaseCount: null,
  complexUseCaseCount: null,
  technicalComplexityFactor: null,
  environmentalFactor: null,
  costRatePerPersonMonth: 15000,
  targetScheduleMonths: 2
})

const hasAnyTrackData = computed(() => !!(result.value || diagramResult.value || estimationResult.value))
const codeTrackStatus = computed(() => {
  if (loading.value) {
    return 'running'
  }
  return result.value ? 'ready' : 'idle'
})
const diagramTrackStatus = computed(() => {
  if (diagramLoading.value) {
    return 'running'
  }
  return diagramResult.value ? 'ready' : 'idle'
})
const estimationTrackStatus = computed(() => {
  if (estimationLoading.value) {
    return 'running'
  }
  return estimationResult.value ? 'ready' : 'idle'
})

onMounted(async () => {
  await Promise.all([loadBackendHealth(), loadRecognitionStatus()])
})

async function loadBackendHealth() {
  try {
    const response = await checkHealth()
    healthStatus.value = response.data.status
  } catch {
    healthStatus.value = 'UNAVAILABLE'
  }
}

async function loadRecognitionStatus() {
  try {
    const [healthResponse, modelsResponse] = await Promise.all([checkRecognitionHealth(), fetchModelsStatus()])
    const base = healthResponse.data?.status ?? 'UNKNOWN'
    const ready = modelsResponse.data?.ready === true ? 'READY' : 'NOT_READY'
    recognitionStatus.value = `${base} / ${ready}`
  } catch {
    recognitionStatus.value = 'UNAVAILABLE'
  }
}

async function handleSubmitText(payload) {
  await runTextAnalysis(payload)
  hydrateEstimateDefaults()
}

async function handleSubmitFile(files) {
  await runFileAnalysis(files)
  hydrateEstimateDefaults()
}

async function handleSubmitFolder(files) {
  await runFolderAnalysis(files)
  hydrateEstimateDefaults()
}

function hydrateEstimateDefaults() {
  const summary = result.value?.projectSummary
  const classes = result.value?.classMetrics
  if (!summary) {
    return
  }
  estimateForm.value.totalLoc = summary.totalLoc
  estimateForm.value.classCount = summary.totalClasses
  if (Array.isArray(classes)) {
    const relationshipCount = classes.reduce((acc, item) => acc + (item.cbo || 0), 0)
    estimateForm.value.relationshipCount = relationshipCount
  }
}

function onStructuredFileChange(event) {
  structuredFile.value = event.target.files?.[0] || null
}

function onImageFileChange(event) {
  imageFile.value = event.target.files?.[0] || null
}

async function runStructured() {
  if (!structuredFile.value) {
    diagramError.value = 'Please choose a structured diagram file.'
    return
  }
  diagramLoading.value = true
  diagramError.value = ''
  try {
    const response = await analyzeStructuredDiagram(structuredFile.value, structuredType.value)
    diagramResult.value = response.data.diagramAnalysis || response.data
  } catch (err) {
    diagramError.value = err?.response?.data?.message || err?.message || 'Structured analysis failed'
  } finally {
    diagramLoading.value = false
  }
}

async function runImage() {
  if (!imageFile.value) {
    diagramError.value = 'Please choose an image diagram file.'
    return
  }
  diagramLoading.value = true
  diagramError.value = ''
  try {
    const response = await analyzeImageDiagram(imageFile.value, imageType.value)
    diagramResult.value = response.data.diagramAnalysis || response.data
  } catch (err) {
    diagramError.value = err?.response?.data?.message || err?.message || 'Image analysis failed'
  } finally {
    diagramLoading.value = false
  }
}

async function runEstimation() {
  estimationLoading.value = true
  estimationError.value = ''
  try {
    const payload = {
      ...estimateForm.value,
      relationshipCount: estimateForm.value.relationshipCount ?? diagramResult.value?.relations?.length ?? 0,
      useCaseCount: estimateForm.value.useCaseCount ?? 0,
      decisionNodeCount: estimateForm.value.decisionNodeCount ?? 0
    }
    const response = await estimateProject(payload)
    estimationResult.value = response.data.projectEstimation || response.data
  } catch (err) {
    estimationError.value = err?.response?.data?.message || err?.message || 'Estimation failed'
  } finally {
    estimationLoading.value = false
  }
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
  if (!hasAnyTrackData.value) {
    return
  }
  downloadBlob(
    'metrics-report.csv',
    buildCsv({
      codeResult: result.value,
      diagramResult: diagramResult.value,
      estimationResult: estimationResult.value
    }),
    'text/csv'
  )
}

function downloadMarkdown() {
  if (!hasAnyTrackData.value) {
    return
  }
  downloadBlob(
    'metrics-report.md',
    buildMarkdownReport({
      codeResult: result.value,
      diagramResult: diagramResult.value,
      estimationResult: estimationResult.value
    }),
    'text/markdown'
  )
}
</script>
