<template>
  <section class="panel">
    <h2>Complexity View</h2>
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
    tooltip: {},
    xAxis: { type: 'category', data: topMethods.map((item) => item.methodName) },
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
