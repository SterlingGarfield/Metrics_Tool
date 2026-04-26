import { fireEvent, render, screen, waitFor } from '@testing-library/vue'
import DesignInputPanel from '../DesignInputPanel.vue'
import { suggestDesignMetrics } from '../../api/metrics'

function createSuggestionResponse(overrides = {}) {
  return {
    data: {
      available: true,
      diagramType: 'use-case',
      recognizedText: ['User', 'Register Account', 'Reset Password'],
      suggestedMetrics: {
        classCount: null,
        relationshipCount: null,
        useCaseCount: 2,
        actorCount: 1,
        flowNodeCount: null
      },
      confidence: 0.84,
      warnings: ['以下建议基于 OCR 文本启发式推断，请在提交前确认。'],
      ...overrides
    }
  }
}

function createDeferred() {
  let resolve
  let reject
  const promise = new Promise((res, rej) => {
    resolve = res
    reject = rej
  })

  return { promise, resolve, reject }
}

vi.mock('../../api/metrics', () => ({
  suggestDesignMetrics: vi.fn()
}))

describe('DesignInputPanel', () => {
  beforeEach(() => {
    suggestDesignMetrics.mockReset()
    suggestDesignMetrics.mockResolvedValue(createSuggestionResponse())
  })

  test('collects manual design metrics and emits a design analysis payload', async () => {
    const { emitted } = render(DesignInputPanel)

    await fireEvent.update(screen.getByLabelText('设计图类型'), 'use-case')
    await fireEvent.update(screen.getByLabelText('类数量'), '8')
    await fireEvent.update(screen.getByLabelText('关系数量'), '12')
    await fireEvent.update(screen.getByLabelText('用例数量'), '3')
    await fireEvent.update(screen.getByLabelText('参与者数量'), '6')
    await fireEvent.update(screen.getByLabelText('流程节点数量'), '4')
    await fireEvent.update(screen.getByLabelText('设计说明'), 'Registration flow')
    await fireEvent.click(screen.getByRole('button', { name: '提交设计阶段度量' }))

    expect(emitted()['submit-design'][0][0]).toEqual({
      diagramType: 'use-case',
      classCount: 8,
      relationshipCount: 12,
      useCaseCount: 3,
      actorCount: 6,
      flowNodeCount: 4,
      imageProvided: false,
      notes: 'Registration flow'
    })
  })

  test('auto-runs OCR after image selection and auto-submits when confidence and metrics are complete', async () => {
    const { emitted } = render(DesignInputPanel)

    await fireEvent.update(screen.getByLabelText('设计图类型'), 'use-case')
    await fireEvent.change(screen.getByLabelText('可选设计图图片'), {
      target: {
        files: [new File(['diagram'], 'design.png', { type: 'image/png' })]
      }
    })

    expect(suggestDesignMetrics).toHaveBeenCalledTimes(1)
    expect(
      await screen.findByText((content) => (
        content === '正在识别设计图...' || content === '正在生成设计指标...'
      ))
    ).toBeInTheDocument()
    expect(await screen.findByText('User')).toBeInTheDocument()
    expect(await screen.findByText('Register Account')).toBeInTheDocument()
    await waitFor(() => {
      expect(emitted()['submit-design'][0][0]).toEqual({
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
    expect(screen.getByRole('button', { name: '重试识别' })).toBeInTheDocument()
  })

  test('applies OCR suggestions and still lets users edit the final payload when confidence is too low for auto-submit', async () => {
    suggestDesignMetrics.mockResolvedValueOnce(createSuggestionResponse({
      confidence: 0.55,
      warnings: ['当前识别结果未达到自动提交阈值，请确认后补全。']
    }))

    const { emitted } = render(DesignInputPanel)

    await fireEvent.update(screen.getByLabelText('设计图类型'), 'use-case')
    await fireEvent.change(screen.getByLabelText('可选设计图图片'), {
      target: {
        files: [new File(['diagram'], 'design.png', { type: 'image/png' })]
      }
    })

    expect(await screen.findByText('当前识别结果未达到自动提交阈值，请确认后补全。')).toBeInTheDocument()
    expect(await screen.findByText('识别不完整，请补全后提交。')).toBeInTheDocument()
    expect(emitted()['submit-design']).toBeUndefined()
    await fireEvent.update(screen.getByLabelText('用例数量'), '3')
    await fireEvent.update(screen.getByLabelText('参与者数量'), '2')
    await fireEvent.click(screen.getByRole('button', { name: '提交设计阶段度量' }))

    expect(emitted()['submit-design'][0][0]).toEqual({
      diagramType: 'use-case',
      classCount: 0,
      relationshipCount: 0,
      useCaseCount: 3,
      actorCount: 2,
      flowNodeCount: 0,
      imageProvided: true,
      notes: ''
    })
  })

  test('blocks a silent all-zero submit when an image is selected but no metrics were filled', async () => {
    suggestDesignMetrics.mockResolvedValueOnce(createSuggestionResponse({
      available: false,
      recognizedText: [],
      suggestedMetrics: {},
      confidence: 0,
      warnings: ['当前图片未识别出可直接使用的设计实体。']
    }))

    const { emitted } = render(DesignInputPanel)

    await fireEvent.update(screen.getByLabelText('设计图类型'), 'use-case')
    await fireEvent.change(screen.getByLabelText('可选设计图图片'), {
      target: {
        files: [new File(['diagram'], 'design.png', { type: 'image/png' })]
      }
    })

    await fireEvent.click(screen.getByRole('button', { name: '提交设计阶段度量' }))

    expect(emitted()['submit-design']).toBeUndefined()
    expect(
      await screen.findByText('已选择设计图图片，但当前提交不会自动识别图片。请先点击“识别图片建议”，或手动填写用例/参与者等指标。')
    ).toBeInTheDocument()
  })

  test('keeps only the newest OCR response when users replace the image mid-request', async () => {
    const firstSuggestion = createDeferred()
    const secondSuggestion = createDeferred()
    suggestDesignMetrics
      .mockImplementationOnce(() => firstSuggestion.promise)
      .mockImplementationOnce(() => secondSuggestion.promise)

    const { emitted } = render(DesignInputPanel)

    await fireEvent.update(screen.getByLabelText('设计图类型'), 'use-case')
    const imageInput = screen.getByLabelText('可选设计图图片')

    await fireEvent.change(imageInput, {
      target: {
        files: [new File(['first'], 'first.png', { type: 'image/png' })]
      }
    })
    await fireEvent.change(imageInput, {
      target: {
        files: [new File(['second'], 'second.png', { type: 'image/png' })]
      }
    })

    secondSuggestion.resolve(createSuggestionResponse({
      recognizedText: ['Admin', 'Approve Request', 'View Dashboard'],
      suggestedMetrics: {
        classCount: null,
        relationshipCount: 2,
        useCaseCount: 3,
        actorCount: 2,
        flowNodeCount: null
      }
    }))

    await waitFor(() => {
      expect(emitted()['submit-design'][0][0]).toEqual({
        diagramType: 'use-case',
        classCount: 0,
        relationshipCount: 2,
        useCaseCount: 3,
        actorCount: 2,
        flowNodeCount: 0,
        imageProvided: true,
        notes: ''
      })
    })

    firstSuggestion.resolve(createSuggestionResponse({
      recognizedText: ['User', 'Register Account'],
      suggestedMetrics: {
        classCount: null,
        relationshipCount: 0,
        useCaseCount: 1,
        actorCount: 1,
        flowNodeCount: null
      }
    }))

    await waitFor(() => {
      expect(screen.getByLabelText('用例数量')).toHaveValue(3)
      expect(screen.getByLabelText('参与者数量')).toHaveValue(2)
      expect(screen.getByLabelText('关系数量')).toHaveValue(2)
    })
    expect(screen.getByText('Admin')).toBeInTheDocument()
    expect(screen.getByText('Approve Request')).toBeInTheDocument()
    expect(screen.queryByText('Register Account')).not.toBeInTheDocument()
    expect(emitted()['submit-design']).toHaveLength(1)
  })
})
