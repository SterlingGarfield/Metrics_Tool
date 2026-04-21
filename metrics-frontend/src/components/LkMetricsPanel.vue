<template>
  <section class="panel">
    <header class="result-heading">
      <p class="result-kicker">LK 指标</p>
      <h2>LK 课程对齐概览</h2>
      <p class="result-lede">
        将当前结果按课程防守材料里的 LK 口径重新组织，便于和导出报告统一查看。
      </p>
    </header>

    <section class="lk-grid">
      <article v-for="item in metricCards" :key="item.label" class="lk-card">
        <span>{{ item.label }}</span>
        <strong>{{ item.value }}</strong>
      </article>
    </section>

    <div class="lk-evidence">
      <p class="lk-evidence__title"><strong>继承深度分布</strong></p>
      <p>{{ formatDistribution(lkMetrics.inheritanceDepthDistribution) }}</p>
      <p class="lk-note">
        结合 CK 明细表以及导出的 Markdown / CSV 报告，一起呈现课程对齐结果。
      </p>
    </div>
  </section>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  lkMetrics: {
    type: Object,
    required: true
  }
})

const metricCards = computed(() => [
  { label: '类总数', value: formatValue(props.lkMetrics.classCount) },
  { label: '方法总数', value: formatValue(props.lkMetrics.methodCount) },
  { label: '属性总数', value: formatValue(props.lkMetrics.attributeCount) },
  { label: '关系总数', value: formatValue(props.lkMetrics.relationshipCount) },
  { label: '平均每类方法数', value: formatValue(props.lkMetrics.averageMethodsPerClass) },
  { label: '平均每类属性数', value: formatValue(props.lkMetrics.averageAttributesPerClass) },
  { label: '关系密度', value: formatValue(props.lkMetrics.relationDensity) }
])

function formatValue(value) {
  if (value === null || value === undefined) {
    return '暂无数据'
  }
  return typeof value === 'number' && !Number.isInteger(value)
    ? value.toFixed(2)
    : String(value)
}

function formatDistribution(values) {
  if (!Array.isArray(values) || values.length === 0) {
    return '[]'
  }
  return `[${values.join(', ')}]`
}
</script>
