import { fireEvent, render, screen } from '@testing-library/vue'
import InputWorkspace from '../InputWorkspace.vue'

describe('InputWorkspace', () => {
  test('renders controlled code mode content, auto-sizes textareas, and emits mode changes', async () => {
    const { emitted, rerender } = render(InputWorkspace, {
      props: {
        workspace: 'code',
        codeMode: 'text'
      }
    })

    expect(screen.getByRole('button', { name: '代码输入' })).toHaveAttribute('aria-pressed', 'true')
    expect(screen.getByRole('button', { name: '单文件分析' })).toHaveAttribute('aria-pressed', 'false')
    expect(screen.getByRole('button', { name: '多文件分析' })).toHaveAttribute('aria-pressed', 'false')
    expect(screen.getByRole('button', { name: '文件夹扫描' })).toHaveAttribute('aria-pressed', 'false')
    expect(screen.getByText('Measure. Analyze. Evolve.')).toBeInTheDocument()
    expect(screen.getByText('分析工作区')).toBeInTheDocument()
    expect(screen.queryByText('把输入组织成一张清晰的工作台')).not.toBeInTheDocument()
    expect(screen.getByText('请在左侧选择您要使用的模式')).toBeInTheDocument()
    expect(screen.queryByText('先选阶段，再选输入范围；所有入口共用统一的分析节奏。')).not.toBeInTheDocument()
    expect(screen.getByText('当前模式：代码输入')).toBeInTheDocument()
    expect(screen.getByText('适合快速验证某段 Java 代码的复杂度与结构指标。')).toBeInTheDocument()

    await fireEvent.click(screen.getByRole('button', { name: '单文件分析' }))
    expect(emitted()['update:codeMode'][0]).toEqual(['file'])

    await rerender({
      workspace: 'code',
      codeMode: 'file'
    })

    expect(screen.getByRole('button', { name: '单文件分析' })).toHaveAttribute('aria-pressed', 'true')
    expect(screen.getByText('当前模式：单文件分析')).toBeInTheDocument()
    expect(screen.getByText('用于聚焦某个类或单个源码文件的风险与指标。')).toBeInTheDocument()
    expect(screen.getByRole('button', { name: '从系统中选择 Java 文件' })).toBeInTheDocument()

    await fireEvent.click(screen.getByRole('button', { name: '文件夹扫描' }))
    expect(emitted()['update:codeMode'][1]).toEqual(['folder'])

    await rerender({
      workspace: 'code',
      codeMode: 'folder'
    })
    expect(screen.getByText('当前模式：文件夹扫描')).toBeInTheDocument()
    expect(screen.getByText('用于从目录级别整理整个 Java 模块的总体度量。')).toBeInTheDocument()

    await rerender({
      workspace: 'code',
      codeMode: 'text'
    })
    await fireEvent.click(screen.getByRole('button', { name: '代码输入' }))
    const sourceInput = screen.getByLabelText('Java 源码输入区')
    Object.defineProperty(sourceInput, 'scrollHeight', {
      configurable: true,
      value: 320
    })
    await fireEvent.update(sourceInput, 'public class Demo {}')
    expect(sourceInput).toHaveClass('app-textarea', 'app-textarea--autosize')
    expect(sourceInput.style.height).toBe('320px')
    await fireEvent.click(screen.getByRole('button', { name: '开始分析任务' }))

    expect(emitted()['submit-text'][0][0]).toEqual({
      fileName: 'Snippet.java',
      sourceCode: 'public class Demo {}'
    })
  })

  test('renders only the requested workspace when switchers are hidden', async () => {
    const { rerender } = render(InputWorkspace, {
      props: {
        workspace: 'design',
        codeMode: 'text',
        hideWorkspaceSwitcher: true
      }
    })

    expect(screen.queryByRole('button', { name: '代码度量' })).not.toBeInTheDocument()
    expect(screen.queryByRole('button', { name: '代码输入' })).not.toBeInTheDocument()
    expect(screen.getByLabelText('设计图类型')).toBeInTheDocument()

    await rerender({
      workspace: 'estimation',
      codeMode: 'text',
      hideWorkspaceSwitcher: true
    })

    expect(screen.getByRole('button', { name: '人工估算' })).toBeInTheDocument()
    expect(screen.getByLabelText('LoC')).toBeInTheDocument()
    expect(screen.queryByLabelText('设计图类型')).not.toBeInTheDocument()
  })

  test('emits desktop file selection actions from code file modes', async () => {
    const { emitted } = render(InputWorkspace, {
      props: {
        workspace: 'code',
        codeMode: 'file'
      }
    })

    await fireEvent.click(screen.getByRole('button', { name: '从系统中选择 Java 文件' }))

    await render(InputWorkspace, {
      props: {
        workspace: 'code',
        codeMode: 'files'
      }
    }).rerender({
      workspace: 'code',
      codeMode: 'files'
    })
    await fireEvent.click(screen.getByRole('button', { name: '从系统中选择多个 Java 文件' }))

    await render(InputWorkspace, {
      props: {
        workspace: 'code',
        codeMode: 'folder'
      }
    }).rerender({
      workspace: 'code',
      codeMode: 'folder'
    })
    await fireEvent.click(screen.getByRole('button', { name: '从系统中选择源码文件夹' }))

    expect(emitted()['submit-file'][0]).toEqual([])
  })

  test('emits workspace changes and supports design and estimation submissions', async () => {
    const { emitted, rerender } = render(InputWorkspace, {
      props: {
        workspace: 'code',
        codeMode: 'text',
        estimationDefaults: {
          actorCount: 4,
          useCaseCount: 6
        }
      }
    })

    await fireEvent.click(screen.getByRole('button', { name: '设计度量' }))
    expect(emitted()['update:workspace'][0]).toEqual(['design'])

    await rerender({
      workspace: 'design',
      codeMode: 'text'
    })
    expect(screen.getByLabelText('设计图类型')).toBeInTheDocument()
    const designNotes = screen.getByLabelText('设计说明')
    Object.defineProperty(designNotes, 'scrollHeight', {
      configurable: true,
      value: 180
    })
    await fireEvent.update(designNotes, '补充一些设计说明')
    expect(designNotes).toHaveClass('app-textarea', 'app-textarea--autosize')
    expect(designNotes.style.height).toBe('180px')

    await fireEvent.update(screen.getByLabelText('设计图类型'), 'class')
    await fireEvent.update(screen.getByLabelText('类数量'), '5')
    await fireEvent.click(screen.getByRole('button', { name: '提交设计阶段度量' }))

    expect(emitted()['submit-design'][0][0]).toEqual({
      diagramType: 'class',
      classCount: 5,
      relationshipCount: 0,
      useCaseCount: 0,
      actorCount: 0,
      flowNodeCount: 0,
      imageProvided: false,
      notes: '补充一些设计说明'
    })

    await fireEvent.click(screen.getByRole('button', { name: '项目估算' }))
    expect(emitted()['update:workspace'][1]).toEqual(['estimation'])

    await rerender({
      workspace: 'estimation',
      codeMode: 'text'
    })

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
