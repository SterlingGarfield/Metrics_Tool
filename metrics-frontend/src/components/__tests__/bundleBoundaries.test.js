import fs from 'node:fs'
import path from 'node:path'

const appPath = path.resolve(process.cwd(), 'src/components/../App.vue')
const chartsPath = path.resolve(process.cwd(), 'src/components/MetricsCharts.vue')

describe('frontend bundle boundaries', () => {
  test('loads results-only panels through async component imports', () => {
    const source = fs.readFileSync(appPath, 'utf8')

    expect(source).toContain("defineAsyncComponent")
    expect(source).toContain("() => import('./components/MetricsCharts.vue')")
    expect(source).toContain("() => import('./components/MetricsTables.vue')")
    expect(source).toContain("() => import('./components/LkMetricsPanel.vue')")
    expect(source).toContain("() => import('./components/DesignMetricsPanel.vue')")
    expect(source).toContain("() => import('./components/EstimationPanel.vue')")
    expect(source).toContain("() => import('./components/MetricInfoDrawer.vue')")
  })

  test('imports only the ECharts modules needed by the complexity bar chart', () => {
    const source = fs.readFileSync(chartsPath, 'utf8')

    expect(source).toContain("from 'echarts/core'")
    expect(source).toContain("from 'echarts/charts'")
    expect(source).toContain("from 'echarts/components'")
    expect(source).toContain("from 'echarts/renderers'")
    expect(source).toContain('BarChart')
    expect(source).toContain('GridComponent')
    expect(source).toContain('TooltipComponent')
    expect(source).toContain('CanvasRenderer')
    expect(source).not.toContain("import * as echarts from 'echarts'")
  })
})
