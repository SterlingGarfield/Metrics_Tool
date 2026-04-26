import { fireEvent, render, screen, within } from '@testing-library/vue'
import App from '../../App.vue'

vi.mock('../../api/metrics', () => ({
  selectFiles: vi.fn().mockResolvedValue([]),
  selectFolder: vi.fn().mockResolvedValue(null),
  checkHealth: vi.fn().mockResolvedValue({ data: { status: 'UP' } }),
  analyzeText: vi.fn().mockResolvedValue({
    data: {
      codeMetrics: {
        available: true,
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
            addedMethodCount: 1,
            overriddenMethodCount: 0,
            specializationIndex: 0,
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
        lkSummary: {
          available: true,
          averageAddedMethodCount: 1,
          averageOverriddenMethodCount: 0,
          maxSpecializationIndex: 0,
          inheritanceClassCount: 1
        }
      },
      designMetrics: {
        available: false,
        diagramType: '',
        classCount: 0,
        relationshipCount: 0,
        useCaseCount: 0,
        actorCount: 0,
        flowNodeCount: 0,
        relationshipDensity: 0,
        useCasesPerActor: 0,
        imageProvided: false,
        notes: ''
      },
      estimationMetrics: {
        available: false,
        mode: '',
        loc: 0,
        staffCount: 0,
        devMonths: 0,
        cost: 0,
        workloadPersonMonths: 0,
        productivityPerPersonMonth: 0,
        costPerLoc: 0,
        useCasePoints: {
          uaw: 0,
          uucw: 0,
          uucp: 0,
          ucp: 0
        }
      },
      riskFindings: [
        {
          scope: 'CLASS',
          target: 'Demo',
          message: 'Class complexity or coupling is high'
        },
        {
          scope: 'METHOD',
          target: 'Demo#go',
          message: 'Cyclomatic complexity is high'
        }
      ],
      parseIssues: [],
      partial: false
    }
  }),
  analyzeDesign: vi.fn(),
  suggestDesignMetrics: vi.fn().mockResolvedValue({
    data: {
      available: true,
      diagramType: 'use-case',
      recognizedText: ['User', 'Register Account'],
      suggestedMetrics: {
        classCount: null,
        relationshipCount: 0,
        useCaseCount: 2,
        actorCount: 1,
        flowNodeCount: null
      },
      confidence: 0.84,
      warnings: ['以下建议基于 OCR 文本启发式推断，请在提交前确认。']
    }
  }),
  analyzeEstimation: vi.fn().mockResolvedValue({
    data: {
      codeMetrics: {
        available: false,
        projectSummary: null,
        classMetrics: [],
        methodMetrics: [],
        lkSummary: null
      },
      designMetrics: {
        available: false,
        diagramType: '',
        classCount: 0,
        relationshipCount: 0,
        useCaseCount: 0,
        actorCount: 0,
        flowNodeCount: 0,
        relationshipDensity: 0,
        useCasesPerActor: 0,
        imageProvided: false,
        notes: ''
      },
      estimationMetrics: {
        available: true,
        mode: 'manual',
        loc: 1200,
        staffCount: 6,
        devMonths: 2,
        cost: 12000,
        workloadPersonMonths: 12,
        productivityPerPersonMonth: 100,
        costPerLoc: 10,
        useCasePoints: {
          uaw: 0,
          uucw: 0,
          uucp: 0,
          ucp: 0
        }
      },
      riskFindings: [],
      parseIssues: [],
      partial: false
    }
  }),
  analyzeUseCasePoints: vi.fn().mockResolvedValue({
    data: {
      codeMetrics: {
        available: false,
        projectSummary: null,
        classMetrics: [],
        methodMetrics: [],
        lkSummary: null
      },
      designMetrics: {
        available: false,
        diagramType: '',
        classCount: 0,
        relationshipCount: 0,
        useCaseCount: 0,
        actorCount: 0,
        flowNodeCount: 0,
        relationshipDensity: 0,
        useCasesPerActor: 0,
        imageProvided: false,
        notes: ''
      },
      estimationMetrics: {
        available: true,
        mode: 'use-case-points',
        loc: 0,
        staffCount: 0,
        devMonths: 0,
        cost: 0,
        workloadPersonMonths: 0,
        productivityPerPersonMonth: 0,
        costPerLoc: 0,
        useCasePoints: {
          uaw: 8,
          uucw: 35,
          uucp: 43,
          ucp: 42.57
        }
      },
      riskFindings: [],
      parseIssues: [],
      partial: false
    }
  }),
  analyzeFiles: vi.fn(),
  analyzeFolder: vi.fn(),
  getDesktopWindowState: vi.fn().mockResolvedValue({ isMaximized: false }),
  minimizeDesktopWindow: vi.fn().mockResolvedValue(undefined),
  toggleDesktopMaximizeWindow: vi.fn().mockResolvedValue(undefined),
  closeDesktopWindow: vi.fn().mockResolvedValue(undefined),
  showDesktopAppMenu: vi.fn().mockResolvedValue(undefined),
  onDesktopWindowStateChanged: vi.fn(() => () => {}),
  exportCsv: vi.fn().mockResolvedValue({ canceled: false }),
  exportMarkdown: vi.fn().mockResolvedValue({ canceled: false })
}))

beforeEach(() => {
  localStorage.clear()
  window.metricsDesktop = {
    getAppStatus: vi.fn()
  }
})

test('renders the desktop workspace shell immediately on launch', async () => {
  const { container } = render(App)
  const banner = screen.getByRole('banner')

  expect(banner).toHaveTextContent('Piggy Metrics')
  expect(banner.querySelector('.desktop-titlebar__center-title')).toHaveTextContent('Piggy Metrics')
  expect(within(banner).queryByText(/后端状态/)).not.toBeInTheDocument()
  expect(within(banner).queryByRole('button', { name: /切换到/ })).not.toBeInTheDocument()
  expect(screen.getByRole('button', { name: '文件' })).toBeInTheDocument()
  expect(screen.getByRole('button', { name: '代码度量' })).toBeInTheDocument()
  expect(screen.getByRole('button', { name: '设计度量' })).toBeInTheDocument()
  expect(screen.getByRole('button', { name: '项目估算' })).toBeInTheDocument()
  expect(screen.getByRole('button', { name: '切换到暗色主题' })).toBeInTheDocument()
  expect(await screen.findByText('后端状态：UP')).toBeInTheDocument()
  expect(screen.getByLabelText('Java 源码输入区')).toBeInTheDocument()
  expect(screen.queryByText('把 Java 度量带进一张更安静的分析画布')).not.toBeInTheDocument()
  expect(screen.queryByText('Java Metrics Analysis Studio')).not.toBeInTheDocument()
  expect(container.querySelector('.app-background-layer')).toBeInTheDocument()
  expect(container.querySelector('.desktop-titlebar__window-controls')).toBeInTheDocument()
  expect(container.querySelector('.desktop-sidebar-shell')).toHaveClass('desktop-sidebar-shell--fixed')
  expect(container.querySelector('.desktop-content-shell')).toHaveClass('desktop-content-shell--with-fixed-sidebar')
  expect(container.querySelector('.desktop-content-scroll')).toBeInTheDocument()

  const sidebarShell = container.querySelector('.desktop-sidebar-shell')
  const contentScroll = container.querySelector('.desktop-content-scroll')
  const toolbar = container.querySelector('.desktop-content-toolbar')
  expect(sidebarShell).toContainElement(toolbar)
  expect(contentScroll).not.toContainElement(toolbar)
  expect(toolbar).toHaveTextContent('后端状态：UP')

  const themeToggle = within(toolbar).getByRole('button', { name: '切换到暗色主题' })
  await fireEvent.click(themeToggle)
  expect(within(toolbar).getByRole('button', { name: '切换到亮色主题' })).toBeInTheDocument()
})

test('shows the new results hierarchy after text analysis completes', async () => {
  render(App)

  await fireEvent.update(await screen.findByLabelText('Java 源码输入区'), 'public class Demo { void go() {} }')
  await fireEvent.click(screen.getByRole('button', { name: '开始分析任务' }))

  expect(
    await screen.findByText('先看规模，再判断这次分析需要把注意力放在哪里。', {
      selector: '.results-section-copy'
    })
  ).toBeInTheDocument()
  expect(
    await screen.findByText(
      '把高风险项先摆到前面，方便决定下一步该修哪一段代码。',
      {
        selector: '.results-section-copy'
      }
    )
  ).toBeInTheDocument()
  const trendHeading = await screen.findByRole('heading', { name: '复杂度趋势' }, { timeout: 3000 })
  expect(trendHeading).toBeInTheDocument()
  expect(await screen.findByText('类级指标')).toBeInTheDocument()
  expect(await screen.findByText('指标说明')).toBeInTheDocument()
  expect(await screen.findByText('LK 指标视图')).toBeInTheDocument()
  expect(await screen.findByText('有继承关系的类数量')).toBeInTheDocument()
  expect(await screen.findByText('类级风险')).toBeInTheDocument()
  expect(await screen.findByText('类复杂度或耦合度偏高')).toBeInTheDocument()
  expect(await screen.findByText('圈复杂度偏高')).toBeInTheDocument()
  expect(await screen.findByRole('button', { name: '导出 CSV 报告' })).toBeInTheDocument()
  expect(await screen.findByRole('button', { name: '导出 Markdown 报告' })).toBeInTheDocument()

  const riskHeading = screen.getByText(
    '把高风险项先摆到前面，方便决定下一步该修哪一段代码。',
    { selector: '.results-section-copy' }
  )
  expect(riskHeading.compareDocumentPosition(trendHeading) & Node.DOCUMENT_POSITION_FOLLOWING).toBeTruthy()
})

test('renders derived design metrics and use case point estimation results', async () => {
  const metricsApi = await import('../../api/metrics')
  metricsApi.analyzeDesign.mockResolvedValueOnce({
    data: {
      codeMetrics: {
        available: false,
        projectSummary: null,
        classMetrics: [],
        methodMetrics: [],
        lkSummary: null
      },
      designMetrics: {
        available: true,
        diagramType: 'use-case',
        classCount: 0,
        relationshipCount: 5,
        useCaseCount: 6,
        actorCount: 4,
        flowNodeCount: 0,
        relationshipDensity: 0,
        useCasesPerActor: 1.5,
        imageProvided: false,
        notes: 'Registration flow'
      },
      estimationMetrics: {
        available: false,
        mode: '',
        loc: 0,
        staffCount: 0,
        devMonths: 0,
        cost: 0,
        workloadPersonMonths: 0,
        productivityPerPersonMonth: 0,
        costPerLoc: 0,
        useCasePoints: {
          uaw: 0,
          uucw: 0,
          uucp: 0,
          ucp: 0
        }
      },
      riskFindings: [],
      parseIssues: [],
      partial: false
    }
  })

  render(App)

  await fireEvent.click(screen.getByRole('button', { name: '设计度量' }))
  await fireEvent.update(screen.getByLabelText('设计图类型'), 'use-case')
  await fireEvent.update(screen.getByLabelText('关系数量'), '5')
  await fireEvent.update(screen.getByLabelText('用例数量'), '6')
  await fireEvent.update(screen.getByLabelText('参与者数量'), '4')
  await fireEvent.update(screen.getByLabelText('设计说明'), 'Registration flow')
  await fireEvent.click(screen.getByRole('button', { name: '提交设计阶段度量' }))

  expect(await screen.findByText('每个参与者对应的用例数')).toBeInTheDocument()
  expect(await screen.findByText('1.50')).toBeInTheDocument()

  await fireEvent.click(screen.getByRole('button', { name: '项目估算' }))
  await fireEvent.click(screen.getByRole('button', { name: '用例点估算' }))
  expect(screen.getByLabelText('一般参与者')).toHaveValue(4)
  expect(screen.getByLabelText('一般用例')).toHaveValue(6)

  await fireEvent.update(screen.getByLabelText('简单参与者'), '1')
  await fireEvent.update(screen.getByLabelText('一般参与者'), '2')
  await fireEvent.update(screen.getByLabelText('复杂参与者'), '1')
  await fireEvent.update(screen.getByLabelText('简单用例'), '2')
  await fireEvent.update(screen.getByLabelText('一般用例'), '1')
  await fireEvent.update(screen.getByLabelText('复杂用例'), '1')
  await fireEvent.update(screen.getByLabelText('技术复杂度因子'), '1.1')
  await fireEvent.update(screen.getByLabelText('环境复杂度因子'), '0.9')
  await fireEvent.click(screen.getByRole('button', { name: '提交用例点估算' }))

  expect(await screen.findByText('用例点估算结果')).toBeInTheDocument()
  expect(await screen.findByText('UAW')).toBeInTheDocument()
  expect(await screen.findByText('42.57')).toBeInTheDocument()
})

test('auto-submits design analysis after a high-confidence OCR suggestion', async () => {
  const metricsApi = await import('../../api/metrics')
  metricsApi.analyzeDesign.mockResolvedValueOnce({
    data: {
      codeMetrics: {
        available: false,
        projectSummary: null,
        classMetrics: [],
        methodMetrics: [],
        lkSummary: null
      },
      designMetrics: {
        available: true,
        diagramType: 'use-case',
        classCount: 0,
        relationshipCount: 0,
        useCaseCount: 2,
        actorCount: 1,
        flowNodeCount: 0,
        relationshipDensity: 0,
        useCasesPerActor: 2,
        imageProvided: true,
        notes: ''
      },
      estimationMetrics: {
        available: false,
        mode: '',
        loc: 0,
        staffCount: 0,
        devMonths: 0,
        cost: 0,
        workloadPersonMonths: 0,
        productivityPerPersonMonth: 0,
        costPerLoc: 0,
        useCasePoints: {
          uaw: 0,
          uucw: 0,
          uucp: 0,
          ucp: 0
        }
      },
      riskFindings: [],
      parseIssues: [],
      partial: false
    }
  })

  render(App)

  await fireEvent.click(screen.getByRole('button', { name: '设计度量' }))
  await fireEvent.update(screen.getByLabelText('设计图类型'), 'use-case')
  await fireEvent.change(screen.getByLabelText('可选设计图图片'), {
    target: {
      files: [new File(['diagram'], 'design.png', { type: 'image/png' })]
    }
  })

  expect(await screen.findByText('User')).toBeInTheDocument()
  expect(await screen.findByText('每个参与者对应的用例数')).toBeInTheDocument()
  expect(await screen.findByText('2.00')).toBeInTheDocument()
  expect(metricsApi.analyzeDesign).toHaveBeenCalledWith({
    diagramType: 'use-case',
    classCount: 0,
    relationshipCount: 0,
    useCaseCount: 2,
    actorCount: 1,
    flowNodeCount: 0,
    imageProvided: true,
    notes: ''
  })
})

test('uses the sidebar to switch between code, design and estimation workspaces', async () => {
  const { container } = render(App)

  await fireEvent.click(screen.getByRole('button', { name: '设计度量' }))
  expect(screen.getByLabelText('设计图类型')).toBeInTheDocument()

  await fireEvent.click(screen.getByRole('button', { name: '项目估算' }))
  expect(screen.getByLabelText('LoC')).toBeInTheDocument()

  await fireEvent.click(screen.getByRole('button', { name: '代码度量' }))
  await fireEvent.click(screen.getByRole('button', { name: '文件夹扫描' }))
  expect(screen.getByRole('button', { name: '从系统中选择源码文件夹' })).toBeInTheDocument()
  expect(container.querySelector('.desktop-titlebar-shell')).toBeInTheDocument()
})

test('shows a blocking host-unavailable state outside Electron', async () => {
  delete window.metricsDesktop

  render(App)

  expect(screen.getByText('请从 Metrics Desktop 启动当前工作台')).toBeInTheDocument()
  expect(screen.queryByRole('button', { name: '代码度量' })).not.toBeInTheDocument()
  expect(screen.queryByLabelText('Java 源码输入区')).not.toBeInTheDocument()
})
