<template>
  <main class="app-shell">
    <header class="topbar">
      <div class="brand">
        <span class="brand-kicker">本地化分析流程</span>
        <strong>软件度量工作台</strong>
      </div>
      <nav class="section-nav" aria-label="顶层导航">
        <a href="#overview">产品概览</a>
        <a href="#code-metrics">代码度量</a>
        <a href="#diagram-metrics">设计图度量</a>
        <a href="#estimation">项目估算</a>
        <a href="#export">报告导出</a>
      </nav>
    </header>

    <section id="overview" class="app-section app-section--overview">
      <ProductHero
        :backend-status="healthStatus"
        :recognition-status="recognitionStatus"
      />
      <MainlineOverview
        :code-status="codeTrackStatus"
        :diagram-status="diagramTrackStatus"
        :estimation-status="estimationTrackStatus"
      />
    </section>

    <section id="workbench" class="app-section app-section--workbench">
      <WorkbenchIntro @select="handleWorkbenchSelect" />
    </section>

    <div class="workbench-stack">
      <section id="code-metrics" class="panel">
        <div class="section-heading">
          <p class="section-kicker">主线一</p>
          <h2>代码度量</h2>
        </div>
        <InputWorkspace
          @submit-text="handleSubmitText"
          @submit-file="handleSubmitFile"
          @submit-files="handleSubmitFile"
          @submit-folder="handleSubmitFolder"
        />
        <p v-if="loading" class="status-banner">正在分析代码集...</p>
        <p v-if="error" class="status-banner error">{{ error }}</p>

        <template v-if="result">
          <OverviewCards :summary="result.projectSummary" />
          <LkMetricsPanel v-if="resolvedLkMetrics" :lk-metrics="resolvedLkMetrics" />
          <MetricsCharts :method-metrics="result.methodMetrics" />
          <MetricsTables :class-metrics="result.classMetrics" :method-metrics="result.methodMetrics" />
          <RiskPanel :risk-findings="result.riskFindings" />
          <MetricInfoDrawer />
        </template>
      </section>

      <section id="diagram-metrics" class="panel">
        <div class="section-heading">
          <p class="section-kicker">主线二</p>
          <h2>设计图度量</h2>
        </div>
        <div class="two-column-grid">
          <form class="pane" @submit.prevent="runStructured">
            <h3>结构化设计图输入</h3>
            <label for="structured-type">图类型</label>
            <select id="structured-type" v-model="structuredType">
              <option value="class">类图</option>
              <option value="flow">流程图</option>
              <option value="usecase">用例图</option>
            </select>
            <label for="structured-file">设计图文件（.puml / .mmd）</label>
            <input id="structured-file" type="file" accept=".puml,.mmd,.txt" @change="onStructuredFileChange" />
            <button type="submit" class="primary-button" :disabled="diagramLoading">分析结构化设计图</button>
          </form>

          <form class="pane" @submit.prevent="runImage">
            <h3>设计图图片输入</h3>
            <label for="image-type">图类型</label>
            <select id="image-type" v-model="imageType">
              <option value="class">类图</option>
              <option value="flow">流程图</option>
              <option value="usecase">用例图</option>
            </select>
            <label for="image-file">设计图图片（.png / .jpg）</label>
            <input id="image-file" type="file" accept=".png,.jpg,.jpeg" @change="onImageFileChange" />
            <button type="submit" class="primary-button" :disabled="diagramLoading">分析设计图图片</button>
          </form>
        </div>

        <p v-if="diagramLoading" class="status-banner">正在执行设计图识别...</p>
        <p v-if="diagramError" class="status-banner error">{{ diagramError }}</p>

        <DiagramResultPanel v-if="diagramResult" :diagram-result="diagramResult" />
      </section>

      <section id="estimation" class="panel">
        <div class="section-heading">
          <p class="section-kicker">主线三</p>
          <h2>项目估算</h2>
        </div>
        <form class="estimation-grid" @submit.prevent="runEstimation">
          <label for="estimate-method">估算方法</label>
          <select id="estimate-method" v-model="estimateForm.estimationMethod">
            <option value="ucp">用例点（UCP）</option>
            <option value="function_point">功能点（Function Point）</option>
          </select>

          <label for="estimate-diagram-type">设计图类型</label>
          <select id="estimate-diagram-type" v-model="estimateForm.diagramType">
            <option value="class">类图</option>
            <option value="flow">流程图</option>
            <option value="usecase">用例图</option>
          </select>

          <label for="estimate-loc">总代码行数</label>
          <input id="estimate-loc" type="number" min="0" v-model.number="estimateForm.totalLoc" />

          <label for="estimate-classes">类数量</label>
          <input id="estimate-classes" type="number" min="0" v-model.number="estimateForm.classCount" />

          <label for="estimate-relations">关系数量</label>
          <input id="estimate-relations" type="number" min="0" v-model.number="estimateForm.relationshipCount" />

          <label for="estimate-usecases">用例数量</label>
          <input id="estimate-usecases" type="number" min="0" v-model.number="estimateForm.useCaseCount" />

          <label for="estimate-decisions">判定节点数量</label>
          <input id="estimate-decisions" type="number" min="0" v-model.number="estimateForm.decisionNodeCount" />

          <template v-if="estimateForm.estimationMethod === 'ucp'">
            <label for="estimate-simple-actors">简单参与者数量（可选）</label>
            <input id="estimate-simple-actors" type="number" min="0" v-model.number="estimateForm.simpleActorCount" />

            <label for="estimate-average-actors">平均参与者数量（可选）</label>
            <input id="estimate-average-actors" type="number" min="0" v-model.number="estimateForm.averageActorCount" />

            <label for="estimate-complex-actors">复杂参与者数量（可选）</label>
            <input id="estimate-complex-actors" type="number" min="0" v-model.number="estimateForm.complexActorCount" />

            <label for="estimate-simple-usecases">简单用例数量（可选）</label>
            <input id="estimate-simple-usecases" type="number" min="0" v-model.number="estimateForm.simpleUseCaseCount" />

            <label for="estimate-average-usecases">平均用例数量（可选）</label>
            <input id="estimate-average-usecases" type="number" min="0" v-model.number="estimateForm.averageUseCaseCount" />

            <label for="estimate-complex-usecases">复杂用例数量（可选）</label>
            <input id="estimate-complex-usecases" type="number" min="0" v-model.number="estimateForm.complexUseCaseCount" />

            <label for="estimate-tcf">技术复杂度因子（可选）</label>
            <input id="estimate-tcf" type="number" min="0.6" max="1.4" step="0.01" v-model.number="estimateForm.technicalComplexityFactor" />

            <label for="estimate-ef">环境因子（可选）</label>
            <input id="estimate-ef" type="number" min="0.6" max="1.4" step="0.01" v-model.number="estimateForm.environmentalFactor" />
          </template>

          <template v-else>
            <p class="status-banner">
              如果希望后端根据当前项目指标自动推导简化的功能点画像，可以将功能点计数留空。
            </p>

            <label for="estimate-fp-ei">外部输入数量（可选）</label>
            <input id="estimate-fp-ei" type="number" min="0" v-model.number="estimateForm.externalInputCount" />

            <label for="estimate-fp-eo">外部输出数量（可选）</label>
            <input id="estimate-fp-eo" type="number" min="0" v-model.number="estimateForm.externalOutputCount" />

            <label for="estimate-fp-eq">外部查询数量（可选）</label>
            <input id="estimate-fp-eq" type="number" min="0" v-model.number="estimateForm.externalInquiryCount" />

            <label for="estimate-fp-ilf">内部逻辑文件数量（可选）</label>
            <input id="estimate-fp-ilf" type="number" min="0" v-model.number="estimateForm.internalLogicalFileCount" />

            <label for="estimate-fp-eif">外部接口文件数量（可选）</label>
            <input id="estimate-fp-eif" type="number" min="0" v-model.number="estimateForm.externalInterfaceFileCount" />

            <label for="estimate-fp-vaf">调整因子（可选）</label>
            <input id="estimate-fp-vaf" type="number" min="0.65" max="1.35" step="0.01" v-model.number="estimateForm.valueAdjustmentFactor" />
          </template>

          <label for="estimate-cost-rate">每人月成本</label>
          <input id="estimate-cost-rate" type="number" min="1" v-model.number="estimateForm.costRatePerPersonMonth" />

          <label for="estimate-schedule">目标工期（月）</label>
          <input id="estimate-schedule" type="number" min="0.1" step="0.1" v-model.number="estimateForm.targetScheduleMonths" />

          <button type="submit" class="primary-button" :disabled="estimationLoading">开始估算</button>
        </form>

        <p v-if="estimationLoading" class="status-banner">正在执行项目估算...</p>
        <p v-if="estimationError" class="status-banner error">{{ estimationError }}</p>

        <EstimationResultPanel v-if="estimationResult" :estimation-result="estimationResult" />
      </section>

      <section id="export" class="panel">
        <div class="section-heading">
          <p class="section-kicker">输出</p>
          <h2>报告导出</h2>
        </div>
        <p class="shell-lede">将当前可用的主线结果统一汇总为报告。</p>
        <div class="action-row">
          <button type="button" class="primary-button" :disabled="!hasAnyTrackData" @click="downloadCsv">导出 CSV</button>
          <button type="button" class="primary-button" :disabled="!hasAnyTrackData" @click="downloadMarkdown">导出 Markdown</button>
        </div>
        <p v-if="!hasAnyTrackData" class="status-banner">至少完成一条主线后才能导出报告。</p>
      </section>
    </div>
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
import LkMetricsPanel from './components/LkMetricsPanel.vue'
import MainlineOverview from './components/MainlineOverview.vue'
import MetricInfoDrawer from './components/MetricInfoDrawer.vue'
import MetricsCharts from './components/MetricsCharts.vue'
import MetricsTables from './components/MetricsTables.vue'
import OverviewCards from './components/OverviewCards.vue'
import DiagramResultPanel from './components/DiagramResultPanel.vue'
import EstimationResultPanel from './components/EstimationResultPanel.vue'
import ProductHero from './components/ProductHero.vue'
import RiskPanel from './components/RiskPanel.vue'
import WorkbenchIntro from './components/WorkbenchIntro.vue'
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
  estimationMethod: 'ucp',
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
  externalInputCount: null,
  externalOutputCount: null,
  externalInquiryCount: null,
  internalLogicalFileCount: null,
  externalInterfaceFileCount: null,
  valueAdjustmentFactor: null,
  costRatePerPersonMonth: 15000,
  targetScheduleMonths: 2
})

const resolvedLkMetrics = computed(() => result.value?.codeMetrics?.lkMetrics || result.value?.codeMetrics?.lkPresentation || null)
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

function handleWorkbenchSelect(targetId) {
  const target = document.getElementById(targetId)
  target?.scrollIntoView({ behavior: 'smooth', block: 'start' })
}

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
    diagramError.value = '请选择结构化设计图文件。'
    return
  }
  diagramLoading.value = true
  diagramError.value = ''
  try {
    const response = await analyzeStructuredDiagram(structuredFile.value, structuredType.value)
    diagramResult.value = response.data.diagramAnalysis || response.data
  } catch (err) {
    diagramError.value = err?.response?.data?.message || err?.message || '结构化设计图分析失败'
  } finally {
    diagramLoading.value = false
  }
}

async function runImage() {
  if (!imageFile.value) {
    diagramError.value = '请选择设计图图片文件。'
    return
  }
  diagramLoading.value = true
  diagramError.value = ''
  try {
    const response = await analyzeImageDiagram(imageFile.value, imageType.value)
    diagramResult.value = response.data.diagramAnalysis || response.data
  } catch (err) {
    diagramError.value = err?.response?.data?.message || err?.message || '设计图图片分析失败'
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
    estimationError.value = err?.response?.data?.message || err?.message || '项目估算失败'
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
