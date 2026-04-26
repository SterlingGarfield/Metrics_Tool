<template>
  <section class="result-surface result-surface--diagram">
    <header class="result-heading">
      <p class="result-kicker">设计图结果</p>
      <h2>设计图识别结果</h2>
      <p class="result-lede">
        将图类型、来源、节点、关系和识别指标整理成中文摘要，方便先看结论，再按需展开原始数据。
      </p>
    </header>

    <div class="card-grid result-summary-grid">
      <article class="metric-card">
        <span>图类型</span>
        <strong>{{ diagramTypeLabel }}</strong>
      </article>
      <article class="metric-card">
        <span>来源</span>
        <strong>{{ sourceTypeLabel }}</strong>
      </article>
      <article class="metric-card">
        <span>识别置信度</span>
        <strong>{{ confidenceLabel }}</strong>
      </article>
      <article class="metric-card">
        <span>节点数</span>
        <strong>{{ elements.length }}</strong>
      </article>
      <article class="metric-card">
        <span>关系数</span>
        <strong>{{ relations.length }}</strong>
      </article>
    </div>

    <section v-if="metrics.length" class="result-block">
      <header class="result-block__header">
        <h3>识别指标</h3>
        <p>保留后端返回的关键图纸指标，便于快速核对识别结果的完整性。</p>
      </header>
      <div class="result-inline-grid">
        <article v-for="item in metrics" :key="item.name" class="result-inline-card">
          <span>{{ item.name }}</span>
          <strong>{{ formatMetricValue(item.value, item.unit) }}</strong>
          <p>{{ item.description || '无补充说明' }}</p>
        </article>
      </div>
    </section>

    <section class="result-block">
      <header class="result-block__header">
        <h3>节点概览</h3>
        <p>展示识别到的主要节点及其置信度，便于核对是否漏识别或错识别。</p>
      </header>
      <div v-if="elements.length" class="pill-list">
        <article v-for="item in elements" :key="`${item.type}-${item.name}`" class="pill-item">
          <strong>{{ item.name }}</strong>
          <span>{{ formatElementMeta(item) }}</span>
        </article>
      </div>
      <p v-else class="result-empty">未返回节点信息。</p>
    </section>

    <section class="result-block">
      <header class="result-block__header">
        <h3>关系概览</h3>
        <p>展示识别到的关系链路与类型，便于核对结构是否连贯。</p>
      </header>
      <div v-if="relations.length" class="pill-list">
        <article v-for="item in relations" :key="`${item.source}-${item.target}-${item.type}`" class="pill-item">
          <strong>{{ item.source }} -> {{ item.target }}</strong>
          <span>{{ formatRelationMeta(item) }}</span>
        </article>
      </div>
      <p v-else class="result-empty">未返回关系信息。</p>
    </section>

    <section v-if="issues.length" class="result-block">
      <header class="result-block__header">
        <h3>识别提醒</h3>
        <p>当前结果中可能存在歧义、缺失或低置信度项，建议优先复核。</p>
      </header>
      <ul class="result-issue-list">
        <li v-for="item in issues" :key="`${item.level}-${item.code}`">
          <strong>{{ formatIssueLevel(item.level) }}</strong>
          <span>{{ item.message }}</span>
        </li>
      </ul>
    </section>

    <details class="result-raw">
      <summary>原始结果</summary>
      <div class="result-raw__body" role="region" aria-label="原始结果">
        <pre>{{ rawResult }}</pre>
      </div>
    </details>
  </section>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  diagramResult: {
    type: Object,
    required: true
  }
})

const elements = computed(() => Array.isArray(props.diagramResult.elements) ? props.diagramResult.elements : [])
const relations = computed(() => Array.isArray(props.diagramResult.relations) ? props.diagramResult.relations : [])
const metrics = computed(() => Array.isArray(props.diagramResult.metrics) ? props.diagramResult.metrics : [])
const issues = computed(() => Array.isArray(props.diagramResult.issues) ? props.diagramResult.issues : [])
const rawResult = computed(() => JSON.stringify(props.diagramResult, null, 2))
const diagramTypeLabel = computed(() => formatDiagramType(props.diagramResult.diagramType))
const sourceTypeLabel = computed(() => formatSourceType(props.diagramResult.sourceType))
const confidenceLabel = computed(() => formatPercentage(props.diagramResult.confidence?.overall))

function formatDiagramType(value) {
  const labels = {
    class: '类图',
    flow: '流程图',
    usecase: '用例图'
  }
  return labels[value] || value || '暂无'
}

function formatSourceType(value) {
  const labels = {
    image: '图片输入',
    structured: '结构化文件'
  }
  return labels[value] || value || '暂无'
}

function formatPercentage(value) {
  if (value === null || value === undefined || Number.isNaN(Number(value))) {
    return '暂无'
  }
  return `${(Number(value) * 100).toFixed(0)}%`
}

function formatMetricValue(value, unit) {
  if (value === null || value === undefined) {
    return '暂无'
  }
  const numberText = typeof value === 'number' && !Number.isInteger(value) ? value.toFixed(2) : String(value)
  return unit ? `${numberText} ${unit}` : numberText
}

function formatElementMeta(item) {
  const parts = []
  if (item.type) {
    parts.push(item.type)
  }
  if (item.stereotype) {
    parts.push(item.stereotype)
  }
  if (item.confidence !== null && item.confidence !== undefined) {
    parts.push(formatPercentage(item.confidence))
  }
  return parts.length ? parts.join(' · ') : '暂无补充信息'
}

function formatRelationMeta(item) {
  const parts = []
  if (item.type) {
    parts.push(item.type)
  }
  if (item.confidence !== null && item.confidence !== undefined) {
    parts.push(formatPercentage(item.confidence))
  }
  return parts.length ? parts.join(' · ') : '暂无补充信息'
}

function formatIssueLevel(level) {
  const labels = {
    warning: '提醒',
    error: '错误',
    info: '信息'
  }
  return labels[level] || level || '提醒'
}
</script>
