<template>
  <section class="panel">
    <header class="result-heading">
      <p class="result-kicker">复杂度图</p>
      <h2>方法复杂度排行</h2>
      <p class="result-lede">
        按圈复杂度从高到低展示前 10 个方法，帮助快速定位热点。
      </p>
    </header>
    <div ref="complexityChart" class="chart-surface"></div>
  </section>
</template>

<script setup>
import * as echarts from 'echarts'
import { nextTick, onMounted, ref, watch } from 'vue'

const props = defineProps({
  methodMetrics: {
    type: Array,
    required: true
  }
})

const complexityChart = ref(null)
let chart

async function renderChart() {
  await nextTick()
  if (!complexityChart.value || complexityChart.value.offsetWidth === 0) {
    return
  }
  chart ??= echarts.init(complexityChart.value)
  const topMethods = [...props.methodMetrics]
    .sort((a, b) => b.cyclomaticComplexity - a.cyclomaticComplexity)
    .slice(0, 10)
  chart.setOption({
    title: {
      text: '方法复杂度前 10 项',
      left: 'center'
    },
    grid: {
      top: 56,
      left: 24,
      right: 20,
      bottom: 48,
      containLabel: true
    },
    tooltip: {},
    xAxis: { type: 'category', data: topMethods.map((item) => item.methodName), axisLabel: { rotate: 20 } },
    yAxis: { type: 'value' },
    series: [
      {
        type: 'bar',
        data: topMethods.map((item) => item.cyclomaticComplexity),
        itemStyle: { color: '#c45b2d' }
      }
    ]
  })
}

onMounted(renderChart)
watch(() => props.methodMetrics, renderChart, { deep: true })
</script>
