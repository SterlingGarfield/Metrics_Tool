import { fireEvent, render, screen, within } from '@testing-library/vue'
import EstimationResultPanel from '../EstimationResultPanel.vue'

describe('EstimationResultPanel', () => {
  test('renders estimation metrics, evidence, and raw payload', async () => {
    const estimationResult = {
      workloadPersonMonths: 12.5,
      cost: 187500,
      scheduleMonths: 2.5,
      suggestedStaffing: 5,
      basis: {
        summary: '基于 Function Point 直接输入与项目指标估算。',
        details: 'EI=3, EO=2, EQ=1, ILF=4, EIF=1, VAF=1.10。'
      },
      functionPointBreakdown: {
        directInputUsed: true,
        externalInputCount: 3,
        externalOutputCount: 2,
        externalInquiryCount: 1,
        internalLogicalFileCount: 4,
        externalInterfaceFileCount: 1,
        unadjustedFunctionPoints: 45,
        valueAdjustmentFactor: 1.1,
        adjustedFunctionPoints: 49.5,
        workloadPersonMonths: 4.13
      },
      ucpBreakdown: null
    }

    render(EstimationResultPanel, {
      props: {
        estimationResult
      }
    })

    expect(screen.getByRole('heading', { name: '项目估算结果' })).toBeInTheDocument()
    expect(screen.getByText('12.50 人月')).toBeInTheDocument()
    expect(screen.getByText('¥187,500.00')).toBeInTheDocument()
    expect(screen.getByText('2.50 个月')).toBeInTheDocument()
    expect(screen.getByText('5 人')).toBeInTheDocument()
    expect(screen.getByText('基于 Function Point 直接输入与项目指标估算。')).toBeInTheDocument()
    expect(screen.getByText('Function Point 细分')).toBeInTheDocument()
    expect(screen.getByText('外部输入')).toBeInTheDocument()
    expect(screen.getByText('3')).toBeInTheDocument()
    expect(screen.getByText('49.50')).toBeInTheDocument()

    await fireEvent.click(screen.getByText('原始结果'))
    const rawResult = screen.getByRole('region', { name: '原始结果' })
    expect(within(rawResult).getByText(/"workloadPersonMonths": 12.5/)).toBeInTheDocument()
  })

  test('renders the chinese UCP summary when only ucp breakdown is available', () => {
    const estimationResult = {
      workloadPersonMonths: 9.6,
      cost: 144000,
      scheduleMonths: 3.2,
      suggestedStaffing: 3,
      basis: {
        summary: '基于标准 UCP 输入进行估算。',
        details: 'UAW=6, UUCW=45, TCF=0.92, EF=1.03。'
      },
      functionPointBreakdown: null,
      ucpBreakdown: {
        standardInputUsed: true,
        simpleActorCount: 1,
        averageActorCount: 1,
        complexActorCount: 1,
        simpleUseCaseCount: 2,
        averageUseCaseCount: 2,
        complexUseCaseCount: 1,
        uaw: 6,
        uucw: 45,
        uucp: 51,
        technicalComplexityFactor: 0.92,
        environmentalFactor: 1.03,
        ucp: 48.35,
        workloadPersonMonths: 6.04
      }
    }

    render(EstimationResultPanel, {
      props: {
        estimationResult
      }
    })

    expect(screen.getByText('基于标准 UCP 输入进行估算。')).toBeInTheDocument()
    expect(screen.getByText('UCP 细分')).toBeInTheDocument()
    expect(screen.getByText('标准输入')).toBeInTheDocument()
    expect(screen.getByText('是')).toBeInTheDocument()
    expect(screen.getByText('UAW')).toBeInTheDocument()
    expect(screen.getByText('6.00')).toBeInTheDocument()
    expect(screen.getByText('UUCW')).toBeInTheDocument()
    expect(screen.getByText('45.00')).toBeInTheDocument()
    expect(screen.getByText('TCF')).toBeInTheDocument()
    expect(screen.getByText('0.92')).toBeInTheDocument()
    expect(screen.getByText('EF')).toBeInTheDocument()
    expect(screen.getByText('1.03')).toBeInTheDocument()
    expect(screen.queryByText('Function Point 细分')).not.toBeInTheDocument()
  })
})
