import { fireEvent, render, screen } from '@testing-library/vue'
import InputWorkspace from '../InputWorkspace.vue'

describe('InputWorkspace', () => {
  test('renders chinese-first workspace modes and emits text analysis payload', async () => {
    const { emitted } = render(InputWorkspace)

    expect(screen.getByRole('button', { name: '代码输入' })).toHaveAttribute('aria-pressed', 'true')
    expect(screen.getByRole('button', { name: '单文件上传' })).toHaveAttribute('aria-pressed', 'false')
    expect(screen.getByRole('button', { name: '多文件上传' })).toHaveAttribute('aria-pressed', 'false')
    expect(screen.getByRole('button', { name: '文件夹扫描' })).toHaveAttribute('aria-pressed', 'false')

    expect(screen.getByText('Java 源码')).toBeInTheDocument()
    expect(screen.getByRole('button', { name: '开始分析' })).toBeInTheDocument()

    const singleFile = new File(['public class Single {}'], 'Single.java', { type: 'text/x-java-source' })
    const fileA = new File(['public class A {}'], 'A.java', { type: 'text/x-java-source' })
    const fileB = new File(['public class B {}'], 'B.java', { type: 'text/x-java-source' })
    const folderFile = new File(['public class Folder {}'], 'Folder/Folder.java', { type: 'text/x-java-source' })

    await fireEvent.click(screen.getByRole('button', { name: '单文件上传' }))
    expect(screen.queryByLabelText('Java 源码')).not.toBeInTheDocument()
    expect(screen.getByLabelText('Java 源文件')).toBeInTheDocument()
    await fireEvent.change(screen.getByLabelText('Java 源文件'), { target: { files: [singleFile] } })

    await fireEvent.click(screen.getByRole('button', { name: '多文件上传' }))
    expect(screen.getByLabelText('Java 文件')).toBeInTheDocument()
    await fireEvent.change(screen.getByLabelText('Java 文件'), { target: { files: [fileA, fileB] } })

    await fireEvent.click(screen.getByRole('button', { name: '文件夹扫描' }))
    expect(screen.getByLabelText('Java 源码目录')).toBeInTheDocument()
    await fireEvent.change(screen.getByLabelText('Java 源码目录'), { target: { files: [folderFile] } })

    await fireEvent.click(screen.getByRole('button', { name: '代码输入' }))
    await fireEvent.update(screen.getByLabelText('Java 源码'), 'public class Demo {}')
    await fireEvent.click(screen.getByRole('button', { name: '开始分析' }))

    expect(emitted()['submit-text'][0][0]).toEqual({
      fileName: 'Snippet.java',
      sourceCode: 'public class Demo {}'
    })
    expect(emitted()['submit-file'][0][0]).toEqual([singleFile])
    expect(emitted()['submit-files'][0][0]).toEqual([fileA, fileB])
    expect(emitted()['submit-folder'][0][0]).toEqual([folderFile])
  })
})
