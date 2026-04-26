<template>
  <section class="panel results-section chart-panel">
    <div class="results-section-heading">
      <p class="results-section-kicker">趋势</p>
      <h2>复杂度趋势</h2>
      <p class="results-section-copy">
        先看方法层面的复杂度高点，图表区域保持清爽，方便快速识别需要优先处理的热点。
      </p>
    </div>
    <div ref="complexityChart" class="chart-surface"></div>
  </section>
</template>

<script setup>
import * as echarts from 'echarts/core'
import { BarChart } from 'echarts/charts'
import { GridComponent, TooltipComponent } from 'echarts/components'
import { CanvasRenderer } from 'echarts/renderers'
import { nextTick, onBeforeUnmount, onMounted, ref, watch } from 'vue'

echarts.use([BarChart, GridComponent, TooltipComponent, CanvasRenderer])

const props = defineProps({
  methodMetrics: {
    type: Array,
    required: true
  },
  theme: {
    type: String,
    default: 'light'
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
  const themeStyles = getComputedStyle(document.documentElement)
  const topMethods = [...props.methodMetrics]
    .sort((a, b) => b.cyclomaticComplexity - a.cyclomaticComplexity)
    .slice(0, 10)

  chart.setOption({
    backgroundColor: 'transparent',
    tooltip: {
      backgroundColor: themeStyles.getPropertyValue('--chart-tooltip-bg').trim(),
      borderColor: themeStyles.getPropertyValue('--chart-tooltip-border').trim(),
      textStyle: {
        color: themeStyles.getPropertyValue('--chart-text').trim()
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
        color: themeStyles.getPropertyValue('--chart-text-soft').trim(),
        rotate: 24
      },
      axisLine: {
        lineStyle: {
          color: themeStyles.getPropertyValue('--chart-axis').trim()
        }
      }
    },
    yAxis: {
      type: 'value',
      axisLabel: {
        color: themeStyles.getPropertyValue('--chart-text-soft').trim()
      },
      splitLine: {
        lineStyle: {
          color: themeStyles.getPropertyValue('--chart-grid').trim()
        }
      }
    },
    series: [
      {
        type: 'bar',
        data: topMethods.map((item) => item.cyclomaticComplexity),
        itemStyle: {
          color: themeStyles.getPropertyValue('--chart-bar').trim(),
          borderRadius: [10, 10, 0, 0]
        }
      }
    ]
  })
}

onMounted(renderChart)
onBeforeUnmount(() => {
  chart?.dispose()
  chart = undefined
})
watch(() => props.methodMetrics, renderChart, { deep: true })
watch(() => props.theme, renderChart)
</script>
