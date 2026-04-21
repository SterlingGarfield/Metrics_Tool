import { fireEvent, render, screen, within } from '@testing-library/vue'
import App from '../../App.vue'

vi.mock('../../api/metrics', () => ({
  checkHealth: vi.fn().mockResolvedValue({ data: { status: 'UP' } }),
  checkRecognitionHealth: vi.fn().mockResolvedValue({ data: { status: 'UP' } }),
  fetchModelsStatus: vi.fn().mockResolvedValue({ data: { ready: true } }),
  analyzeStructuredDiagram: vi.fn(),
  analyzeImageDiagram: vi.fn(),
  estimateProject: vi.fn(),
  analyzeText: vi.fn().mockResolvedValue({
    data: {
      projectSummary: {
        totalFiles: 1,
        totalClasses: 2,
        totalMethods: 2,
        totalLoc: 18,
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
        },
        {
          fileName: 'Snippet.java',
          className: 'Helper',
          loc: 8,
          wmc: 1,
          cbo: 0,
          rfc: 1,
          lcom: 0,
          dit: 1,
          noc: 0,
          nom: 1,
          noa: 1,
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
        },
        {
          fileName: 'Snippet.java',
          className: 'Helper',
          methodName: 'assist',
          loc: 3,
          cyclomaticComplexity: 1,
          parameterCount: 0,
          maxNestingDepth: 0,
          branchCount: 0
        }
      ],
      codeMetrics: {
        lkMetrics: {
          classCount: 7,
          methodCount: 8,
          attributeCount: 5,
          relationshipCount: 4,
          averageMethodsPerClass: 3.5,
          averageAttributesPerClass: 2.5,
          relationDensity: 9.99,
          inheritanceDepthDistribution: [9, 8]
        },
        lkPresentation: {
          classCount: 7,
          methodCount: 8,
          attributeCount: 5,
          relationshipCount: 4,
          averageMethodsPerClass: 3.5,
          averageAttributesPerClass: 2.5,
          relationDensity: 9.99,
          inheritanceDepthDistribution: [9, 8]
        }
      },
      riskFindings: [],
      parseIssues: [],
      partial: false
    }
  }),
  analyzeFiles: vi.fn(),
  analyzeFolder: vi.fn()
}))

test('renders overview cards after analysis completes', async () => {
  const scrollIntoView = vi.fn()
  vi.spyOn(document, 'getElementById').mockImplementation((id) => {
    if (id === 'code-metrics' || id === 'diagram-metrics' || id === 'estimation' || id === 'export') {
      return { scrollIntoView }
    }
    return null
  })

  render(App)

  expect(screen.getByRole('link', { name: '产品概览' })).toHaveAttribute('href', '#overview')
  expect(screen.getByRole('link', { name: '代码度量' })).toHaveAttribute('href', '#code-metrics')
  expect(screen.getByRole('link', { name: '设计图度量' })).toHaveAttribute('href', '#diagram-metrics')
  expect(screen.getByRole('link', { name: '项目估算' })).toHaveAttribute('href', '#estimation')
  expect(screen.getByRole('link', { name: '报告导出' })).toHaveAttribute('href', '#export')
  expect(screen.getByRole('heading', { name: '面向课程项目的软件度量工作台' })).toBeInTheDocument()
  expect(screen.getByRole('link', { name: '进入代码度量' })).toHaveAttribute('href', '#code-metrics')
  expect(screen.getByRole('link', { name: '进入分析工作区' })).toHaveAttribute('href', '#workbench')
  expect(screen.getByRole('button', { name: '直达代码度量' })).toBeInTheDocument()
  expect(screen.getByRole('button', { name: '直达设计图度量' })).toBeInTheDocument()
  expect(screen.getByRole('button', { name: '直达项目估算' })).toBeInTheDocument()
  expect(screen.getByRole('button', { name: '直达报告导出' })).toBeInTheDocument()

  await fireEvent.click(screen.getByRole('button', { name: '直达代码度量' }))
  expect(scrollIntoView).toHaveBeenCalledWith({ behavior: 'smooth', block: 'start' })

  await screen.findByText('UP / READY')
  const backendLabel = screen.getByText('代码后端')
  expect(backendLabel.parentElement).toHaveTextContent('UP')
  const recognitionLabel = screen.getByText('识别服务')
  expect(recognitionLabel.parentElement).toHaveTextContent('UP / READY')

  await fireEvent.update(screen.getByLabelText('Java 源码'), 'public class Demo { void go() {} }')
  await fireEvent.click(screen.getByRole('button', { name: '开始分析' }))

  expect(await screen.findByText('状态：ready')).toBeInTheDocument()
  expect(await screen.findByRole('heading', { name: '项目概览' })).toBeInTheDocument()
  const overviewSurface = screen.getByRole('heading', { name: '项目概览' }).closest('section')
  expect(overviewSurface).not.toBeNull()
  const overview = within(overviewSurface)
  expect(await overview.findByText('文件总数')).toBeInTheDocument()
  expect(overview.getByText('类总数')).toBeInTheDocument()
  expect(overview.getByText('方法总数')).toBeInTheDocument()
  expect(overview.getByText('总代码行数')).toBeInTheDocument()
  expect(screen.getByRole('heading', { name: 'LK 课程对齐概览' })).toBeInTheDocument()
  expect(screen.getByRole('heading', { name: '方法复杂度排行' })).toBeInTheDocument()
  expect(screen.getByRole('heading', { name: '类与方法指标明细' })).toBeInTheDocument()
  expect(screen.getByRole('heading', { name: '风险提示' })).toBeInTheDocument()
  expect(screen.getByText('本次分析未发现关键风险。')).toBeInTheDocument()
  expect(screen.getByRole('heading', { name: '指标说明' })).toBeInTheDocument()
  const lkSurface = screen.getByRole('heading', { name: 'LK 课程对齐概览' }).closest('section')
  expect(lkSurface).not.toBeNull()
  const lkPanel = within(lkSurface)
  expect(lkPanel.getByText('类总数')).toBeInTheDocument()
  expect(lkPanel.getByText('方法总数')).toBeInTheDocument()
  expect(lkPanel.getByText('属性总数')).toBeInTheDocument()
  expect(lkPanel.getByText('关系密度')).toBeInTheDocument()
  expect(lkPanel.getByText('9.99')).toBeInTheDocument()
  expect(screen.getAllByLabelText('图类型')).toHaveLength(2)
  expect(screen.getByRole('button', { name: '分析结构化设计图' })).toBeInTheDocument()
  expect(screen.getByRole('button', { name: '分析设计图图片' })).toBeInTheDocument()
  expect(screen.getByRole('button', { name: '开始估算' })).toBeInTheDocument()
  expect(screen.getByRole('button', { name: '导出 CSV' })).not.toBeDisabled()
  expect(screen.getByRole('button', { name: '导出 Markdown' })).not.toBeDisabled()
})
