import { fireEvent, render, screen } from '@testing-library/vue'
import App from '../../App.vue'

vi.mock('../../api/metrics', () => ({
  checkHealth: vi.fn().mockResolvedValue({ data: { status: 'UP' } }),
  analyzeText: vi.fn().mockResolvedValue({
    data: {
      projectSummary: {
        totalFiles: 1,
        totalClasses: 1,
        totalMethods: 1,
        totalLoc: 10,
        blankLines: 1,
        commentLines: 0,
        commentRatio: 0,
        highRiskClasses: 0,
        highRiskMethods: 0
      },
      classMetrics: [
        {
          fileName: 'Snippet.java',
          className: 'Demo',
          loc: 10,
          wmc: 2,
          cbo: 1,
          rfc: 2,
          lcom: 0,
          dit: 0,
          noc: 0,
          nom: 1,
          noa: 0,
          publicMethodCount: 1,
          commentRatio: 0,
          partial: false
        }
      ],
      methodMetrics: [
        {
          fileName: 'Snippet.java',
          className: 'Demo',
          methodName: 'go',
          loc: 4,
          cyclomaticComplexity: 2,
          parameterCount: 0,
          maxNestingDepth: 1,
          branchCount: 1
        }
      ],
      riskFindings: [],
      parseIssues: [],
      partial: false
    }
  }),
  analyzeFiles: vi.fn(),
  analyzeFolder: vi.fn()
}))

test('renders the branded homepage before analysis starts', async () => {
  render(App)

  expect(screen.getByText('Java 度量分析平台')).toBeInTheDocument()
  expect(screen.getByText('暗色可视化代码度量与风险洞察')).toBeInTheDocument()
  expect(screen.getByRole('button', { name: '开始分析' })).toBeInTheDocument()
  expect(screen.getByText('支持代码输入、单文件、多文件与文件夹扫描')).toBeInTheDocument()
})

test('shows the new results hierarchy after text analysis completes', async () => {
  render(App)

  await fireEvent.click(screen.getByRole('button', { name: '开始分析' }))
  await fireEvent.update(await screen.findByLabelText('Java 源码输入区'), 'public class Demo { void go() {} }')
  await fireEvent.click(screen.getByRole('button', { name: '开始分析任务' }))

  expect(await screen.findByText('本次分析概览')).toBeInTheDocument()
  expect(await screen.findByText('风险焦点')).toBeInTheDocument()
  expect(await screen.findByText('复杂度趋势')).toBeInTheDocument()
  expect(await screen.findByRole('button', { name: '导出 CSV' })).toBeInTheDocument()
})
