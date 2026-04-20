import { fireEvent, render, screen } from '@testing-library/vue'
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

test('renders overview cards after analysis completes', async () => {
  render(App)

  await fireEvent.update(screen.getByLabelText('Java Source'), 'public class Demo { void go() {} }')
  await fireEvent.click(screen.getByRole('button', { name: 'Analyze Text' }))

  expect(await screen.findByText('Files')).toBeInTheDocument()
  expect(await screen.findByText('Methods')).toBeInTheDocument()
  expect(await screen.findByText('Export CSV')).toBeInTheDocument()
})
