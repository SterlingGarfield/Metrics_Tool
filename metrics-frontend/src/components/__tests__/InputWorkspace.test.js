import { fireEvent, render, screen } from '@testing-library/vue'
import InputWorkspace from '../InputWorkspace.vue'

describe('InputWorkspace', () => {
  test('renders the Chinese workspace modes and reflects the active mode', async () => {
    const { emitted } = render(InputWorkspace)

    expect(screen.getByRole('button', { name: '代码输入' })).toHaveAttribute('aria-pressed', 'true')
    expect(screen.getByRole('button', { name: '单文件分析' })).toHaveAttribute('aria-pressed', 'false')
    expect(screen.getByRole('button', { name: '多文件分析' })).toHaveAttribute('aria-pressed', 'false')
    expect(screen.getByRole('button', { name: '文件夹扫描' })).toHaveAttribute('aria-pressed', 'false')
    expect(screen.getByText('选择你的分析方式')).toBeInTheDocument()
    expect(screen.getByText('当前模式：代码输入')).toBeInTheDocument()
    expect(screen.getByText('适合快速粘贴 Java 片段并立即启动度量分析。')).toBeInTheDocument()

    await fireEvent.click(screen.getByRole('button', { name: '单文件分析' }))

    expect(screen.getByRole('button', { name: '单文件分析' })).toHaveAttribute('aria-pressed', 'true')
    expect(screen.getByText('当前模式：单文件分析')).toBeInTheDocument()
    expect(screen.getByText('用于对单个 Java 源文件进行快速检查与风险洞察。')).toBeInTheDocument()
    expect(screen.getByLabelText('选择一个 Java 文件')).toBeInTheDocument()

    await fireEvent.click(screen.getByRole('button', { name: '文件夹扫描' }))

    expect(screen.getByText('当前模式：文件夹扫描')).toBeInTheDocument()
    expect(screen.getByText('用于按目录扫描 Java 项目源码并收集整体度量结果。')).toBeInTheDocument()

    await fireEvent.click(screen.getByRole('button', { name: '代码输入' }))
    await fireEvent.update(screen.getByLabelText('Java 源码输入区'), 'public class Demo {}')
    await fireEvent.click(screen.getByRole('button', { name: '开始分析任务' }))

    expect(emitted()['submit-text'][0][0]).toEqual({
      fileName: 'Snippet.java',
      sourceCode: 'public class Demo {}'
    })
  })

  test('emits file, files and folder submissions from the non-text modes', async () => {
    const { emitted } = render(InputWorkspace)

    await fireEvent.click(screen.getByRole('button', { name: '单文件分析' }))
    const singleFile = new File(['class Demo {}'], 'Demo.java', { type: 'text/x-java-source' })
    await fireEvent.change(screen.getByLabelText('选择一个 Java 文件'), { target: { files: [singleFile] } })

    await fireEvent.click(screen.getByRole('button', { name: '多文件分析' }))
    const fileA = new File(['class A {}'], 'A.java', { type: 'text/x-java-source' })
    const fileB = new File(['class B {}'], 'B.java', { type: 'text/x-java-source' })
    await fireEvent.change(screen.getByLabelText('选择多个 Java 文件'), { target: { files: [fileA, fileB] } })

    await fireEvent.click(screen.getByRole('button', { name: '文件夹扫描' }))
    const folderFile = new File(['class Folder {}'], 'Folder.java', { type: 'text/x-java-source' })
    await fireEvent.change(screen.getByLabelText('扫描 Java 源码文件夹'), { target: { files: [folderFile] } })

    expect(emitted()['submit-file'][0][0]).toEqual([singleFile])
    expect(emitted()['submit-files'][0][0]).toEqual([fileA, fileB])
    expect(emitted()['submit-folder'][0][0]).toEqual([folderFile])
  })
})
