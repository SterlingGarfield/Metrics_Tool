import { fireEvent, render, screen } from '@testing-library/vue'
import EstimationInputPanel from '../EstimationInputPanel.vue'

describe('EstimationInputPanel', () => {
  test('collects manual effort inputs and emits an estimation payload', async () => {
    const { emitted } = render(EstimationInputPanel)

    expect(screen.getByRole('button', { name: '人工估算' })).toHaveAttribute('aria-pressed', 'true')
    await fireEvent.update(screen.getByLabelText('LoC'), '1200')
    await fireEvent.update(screen.getByLabelText('人员数量'), '6')
    await fireEvent.update(screen.getByLabelText('工期（月）'), '2')
    await fireEvent.update(screen.getByLabelText('成本'), '12000')
    await fireEvent.click(screen.getByRole('button', { name: '提交项目估算' }))

    expect(emitted()['submit-estimation'][0][0]).toEqual({
      loc: 1200,
      staffCount: 6,
      devMonths: 2,
      cost: 12000
    })
  })

  test('supports use case point estimation and seeds neutral defaults from design metrics', async () => {
    const { emitted } = render(EstimationInputPanel, {
      props: {
        useCaseDefaults: {
          actorCount: 4,
          useCaseCount: 6
        }
      }
    })

    await fireEvent.click(screen.getByRole('button', { name: '用例点估算' }))

    expect(screen.getByRole('button', { name: '用例点估算' })).toHaveAttribute('aria-pressed', 'true')
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

    expect(emitted()['submit-use-case-points'][0][0]).toEqual({
      simpleActors: 1,
      averageActors: 2,
      complexActors: 1,
      simpleUseCases: 2,
      averageUseCases: 1,
      complexUseCases: 1,
      technicalComplexityFactor: 1.1,
      environmentalComplexityFactor: 0.9
    })
  })
})
