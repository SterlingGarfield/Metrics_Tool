<template>
  <div class="pane workspace-form design-workspace">
    <label for="diagram-type">设计图类型</label>
    <select id="diagram-type" v-model="diagramType" class="form-select">
      <option value="class">class</option>
      <option value="use-case">use-case</option>
      <option value="flow">flow</option>
    </select>

    <div class="form-grid">
      <label>
        <span>类数量</span>
        <input v-model="classCount" type="number" min="0" inputmode="numeric" />
      </label>
      <label>
        <span>关系数量</span>
        <input v-model="relationshipCount" type="number" min="0" inputmode="numeric" />
      </label>
      <label>
        <span>用例数量</span>
        <input v-model="useCaseCount" type="number" min="0" inputmode="numeric" />
      </label>
      <label>
        <span>参与者数量</span>
        <input v-model="actorCount" type="number" min="0" inputmode="numeric" />
      </label>
      <label>
        <span>流程节点数量</span>
        <input v-model="flowNodeCount" type="number" min="0" inputmode="numeric" />
      </label>
    </div>

    <label for="design-notes">设计说明</label>
    <textarea
      id="design-notes"
      ref="notesInput"
      v-model="notes"
      class="app-textarea app-textarea--autosize"
      rows="5"
      @input="syncNotesHeight"
    ></textarea>

    <label for="design-image">可选设计图图片</label>
    <input id="design-image" type="file" accept="image/*" @change="onImageChange" />

    <div class="action-row">
      <AppActionButton
        variant="secondary"
        :disabled="!selectedImage || suggesting"
        @click="requestSuggestion"
      >
        {{ suggestionActionLabel }}
      </AppActionButton>
      <AppActionButton @click="submitDesign">提交设计阶段度量</AppActionButton>
    </div>

    <p v-if="suggestionStatus" class="status-banner">{{ suggestionStatus }}</p>
    <p v-if="submitWarning" class="status-banner error">{{ submitWarning }}</p>

    <div v-if="suggestion" class="suggestion-card">
      <p class="workspace-kicker">OCR 建议</p>
      <p class="suggestion-meta">
        识别可用性：{{ suggestion.available ? '可用' : '不可用' }}
        <span v-if="suggestion.confidence"> · 置信度：{{ Math.round(suggestion.confidence * 100) }}%</span>
      </p>

      <div v-if="suggestion.recognizedText?.length" class="recognized-text-list">
        <strong>识别文本摘要</strong>
        <ul>
          <li v-for="line in suggestion.recognizedText" :key="line">{{ line }}</li>
        </ul>
      </div>

      <ul v-if="suggestion.warnings?.length" class="suggestion-warning-list">
        <li v-for="warning in suggestion.warnings" :key="warning">{{ warning }}</li>
      </ul>
    </div>
  </div>
</template>

<script setup>
import { computed, ref } from 'vue'
import { useAutosizeTextarea } from '../composables/useAutosizeTextarea'
import { suggestDesignMetrics } from '../api/metrics'
import AppActionButton from './AppActionButton.vue'

const emit = defineEmits(['submit-design'])
const OCR_AUTO_SUBMIT_THRESHOLD = 0.6

const diagramType = ref('class')
const classCount = ref('0')
const relationshipCount = ref('0')
const useCaseCount = ref('0')
const actorCount = ref('0')
const flowNodeCount = ref('0')
const notes = ref('')
const notesInput = ref(null)
const selectedImage = ref(null)
const suggestion = ref(null)
const suggesting = ref(false)
const suggestionStatus = ref('')
const submitWarning = ref('')
const latestSuggestionRequestId = ref(0)
const { syncHeight: syncNotesHeight } = useAutosizeTextarea(notesInput, [notes])
const suggestionActionLabel = computed(() => (
  selectedImage.value ? '重试识别' : '识别图片建议'
))

function parseMetric(value) {
  const parsed = Number.parseInt(value, 10)
  return Number.isFinite(parsed) && parsed >= 0 ? parsed : 0
}

function collectMetrics() {
  return {
    classCount: parseMetric(classCount.value),
    relationshipCount: parseMetric(relationshipCount.value),
    useCaseCount: parseMetric(useCaseCount.value),
    actorCount: parseMetric(actorCount.value),
    flowNodeCount: parseMetric(flowNodeCount.value)
  }
}

function buildSubmissionPayload(metrics = collectMetrics()) {
  return {
    diagramType: diagramType.value,
    ...metrics,
    imageProvided: Boolean(selectedImage.value),
    notes: notes.value
  }
}

function invalidateSuggestionRequests() {
  latestSuggestionRequestId.value += 1
  suggesting.value = false
}

function applySuggestions(metrics = {}) {
  if (metrics.classCount != null) {
    classCount.value = String(metrics.classCount)
  }
  if (metrics.relationshipCount != null) {
    relationshipCount.value = String(metrics.relationshipCount)
  }
  if (metrics.useCaseCount != null) {
    useCaseCount.value = String(metrics.useCaseCount)
  }
  if (metrics.actorCount != null) {
    actorCount.value = String(metrics.actorCount)
  }
  if (metrics.flowNodeCount != null) {
    flowNodeCount.value = String(metrics.flowNodeCount)
  }
}

function hasCompleteMetrics(metrics, targetDiagramType = diagramType.value) {
  if (targetDiagramType === 'use-case') {
    return metrics.actorCount > 0 && metrics.useCaseCount > 0
  }

  if (targetDiagramType === 'flow') {
    return metrics.flowNodeCount > 0
  }

  return metrics.classCount > 0
}

function shouldAutoSubmit(nextSuggestion) {
  const normalizedConfidence = Number(nextSuggestion?.confidence ?? 0)
  const targetDiagramType = nextSuggestion?.diagramType || diagramType.value
  const metrics = {
    classCount: Number(nextSuggestion?.suggestedMetrics?.classCount ?? 0),
    relationshipCount: Number(nextSuggestion?.suggestedMetrics?.relationshipCount ?? 0),
    useCaseCount: Number(nextSuggestion?.suggestedMetrics?.useCaseCount ?? 0),
    actorCount: Number(nextSuggestion?.suggestedMetrics?.actorCount ?? 0),
    flowNodeCount: Number(nextSuggestion?.suggestedMetrics?.flowNodeCount ?? 0)
  }

  return Boolean(nextSuggestion?.available) &&
    normalizedConfidence >= OCR_AUTO_SUBMIT_THRESHOLD &&
    hasCompleteMetrics(metrics, targetDiagramType)
}

function submitDesign(options = {}) {
  const metrics = options.metrics || collectMetrics()
  const hasManualMetrics = Object.values(metrics).some((value) => value > 0)
  if (selectedImage.value && !hasManualMetrics) {
    submitWarning.value = '已选择设计图图片，但当前提交不会自动识别图片。请先点击“识别图片建议”，或手动填写用例/参与者等指标。'
    return
  }

  if (!options.preserveSuggestionRequest) {
    invalidateSuggestionRequests()
  }

  submitWarning.value = ''
  emit('submit-design', buildSubmissionPayload(metrics))
}

function onImageChange(event) {
  selectedImage.value = event.target.files?.[0] ?? null
  submitWarning.value = ''
  suggestion.value = null
  suggestionStatus.value = ''

  if (!selectedImage.value) {
    invalidateSuggestionRequests()
    return
  }

  requestSuggestion()
}

async function requestSuggestion() {
  if (!selectedImage.value) {
    return
  }

  submitWarning.value = ''
  suggestion.value = null
  suggestionStatus.value = '正在识别设计图...'
  suggesting.value = true
  const requestId = latestSuggestionRequestId.value + 1
  latestSuggestionRequestId.value = requestId

  try {
    const response = await suggestDesignMetrics({
      diagramType: diagramType.value,
      image: selectedImage.value
    })

    if (requestId !== latestSuggestionRequestId.value) {
      return
    }

    suggestion.value = response.data
    applySuggestions(response.data?.suggestedMetrics)

    if (shouldAutoSubmit(response.data)) {
      suggestionStatus.value = '正在生成设计指标...'
      submitDesign({
        metrics: collectMetrics(),
        preserveSuggestionRequest: true
      })
      return
    }

    suggestionStatus.value = '识别不完整，请补全后提交。'
  } catch (error) {
    if (requestId !== latestSuggestionRequestId.value) {
      return
    }

    suggestion.value = {
      available: false,
      recognizedText: [],
      suggestedMetrics: {},
      confidence: 0,
      warnings: [error?.message || 'OCR 建议获取失败，请改用手工录入。']
    }
    suggestionStatus.value = '识别不完整，请补全后提交。'
  } finally {
    if (requestId === latestSuggestionRequestId.value) {
      suggesting.value = false
    }
  }
}
</script>
