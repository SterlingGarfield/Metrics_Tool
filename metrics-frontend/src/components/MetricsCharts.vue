<template>
  <section class="panel results-section chart-panel">
    <div class="results-section-heading">
      <p class="results-section-kicker">趋势</p>
      <h2>复杂度趋势</h2>
      <p class="results-section-copy">
        按方法展示圈复杂度最高的条目，便于快速识别最需要优化的热点代码。
      </p>
    </div>
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
    backgroundColor: 'transparent',
    tooltip: {
      backgroundColor: 'rgba(7, 16, 29, 0.92)',
      borderColor: 'rgba(255, 255, 255, 0.16)',
      textStyle: {
        color: '#f5e8c7'
      }
    },
    grid: {
      left: 48,
      right: 24,
      top: 24,
      bottom: 64
    },
    xAxis: {
      type: 'category',
      data: topMethods.map((item) => item.methodName),
      axisLabel: {
        color: 'rgba(245, 232, 199, 0.82)',
        rotate: 24
      },
      axisLine: {
        lineStyle: {
          color: 'rgba(255, 255, 255, 0.2)'
        }
      }
    },
    yAxis: {
      type: 'value',
      axisLabel: {
        color: 'rgba(245, 232, 199, 0.82)'
      },
      splitLine: {
        lineStyle: {
          color: 'rgba(255, 255, 255, 0.08)'
        }
      }
    },
    series: [
      {
        type: 'bar',
        data: topMethods.map((item) => item.cyclomaticComplexity),
        itemStyle: { color: '#c45b2d', borderRadius: [8, 8, 0, 0] }
      }
    ]
  })
}

onMounted(renderChart)
watch(() => props.methodMetrics, renderChart, { deep: true })
</script>
