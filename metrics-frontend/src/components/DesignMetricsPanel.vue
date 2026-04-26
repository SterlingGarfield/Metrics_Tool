<template>
  <section class="panel results-section">
    <div class="results-section-heading">
      <p class="results-section-kicker">Design</p>
      <h2>设计度量结果</h2>
      <p class="results-section-copy">
        把原始计数、派生指标与 OCR 参与情况拆开呈现，便于直接对应课程要求。
      </p>
    </div>

    <h3 class="results-subheading">原始计数</h3>
    <section class="card-grid">
      <article class="metric-card">
        <span>设计图类型</span>
        <strong>{{ summary.diagramType || '未指定' }}</strong>
      </article>
      <article class="metric-card">
        <span>类数量</span>
        <strong>{{ summary.classCount }}</strong>
      </article>
      <article class="metric-card">
        <span>关系数量</span>
        <strong>{{ summary.relationshipCount }}</strong>
      </article>
      <article class="metric-card">
        <span>用例数量</span>
        <strong>{{ summary.useCaseCount }}</strong>
      </article>
      <article class="metric-card">
        <span>参与者数量</span>
        <strong>{{ summary.actorCount }}</strong>
      </article>
      <article class="metric-card">
        <span>流程节点数量</span>
        <strong>{{ summary.flowNodeCount }}</strong>
      </article>
    </section>

    <h3 class="results-subheading">派生指标</h3>
    <section class="card-grid">
      <article v-if="showRelationshipDensity" class="metric-card">
        <span>关系密度</span>
        <strong>{{ formatNumber(summary.relationshipDensity) }}</strong>
      </article>
      <article v-if="showUseCasesPerActor" class="metric-card">
        <span>每个参与者对应的用例数</span>
        <strong>{{ formatNumber(summary.useCasesPerActor) }}</strong>
      </article>
      <article v-if="!showRelationshipDensity && !showUseCasesPerActor" class="metric-card">
        <span>派生指标</span>
        <strong>0.00</strong>
      </article>
    </section>

    <p v-if="summary.notes" class="results-empty-state">设计说明：{{ summary.notes }}</p>
    <p class="results-empty-state">
      OCR 参与：{{ summary.imageProvided ? '已提供设计图图片并允许 OCR 辅助' : '本次仅使用手工录入' }}
    </p>
  </section>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  summary: {
    type: Object,
    required: true
  }
})

const showRelationshipDensity = computed(() => (
  String(props.summary.diagramType || '').includes('class')
))
const showUseCasesPerActor = computed(() => (
  String(props.summary.diagramType || '').includes('use-case')
))

function formatNumber(value) {
  return Number(value ?? 0).toFixed(2)
}
</script>
