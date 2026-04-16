import { fireEvent, render, screen } from '@testing-library/vue'
import InputWorkspace from '../InputWorkspace.vue'

describe('InputWorkspace', () => {
  test('renders all input modes and emits text analysis payload', async () => {
    const { emitted } = render(InputWorkspace)

    expect(screen.getByRole('button', { name: 'Code Input' })).toBeInTheDocument()
    expect(screen.getByRole('button', { name: 'Single File' })).toBeInTheDocument()
    expect(screen.getByRole('button', { name: 'Multiple Files' })).toBeInTheDocument()
    expect(screen.getByRole('button', { name: 'Folder Scan' })).toBeInTheDocument()

    await fireEvent.update(screen.getByLabelText('Java Source'), 'public class Demo {}')
    await fireEvent.click(screen.getByRole('button', { name: 'Analyze Text' }))

    expect(emitted()['submit-text'][0][0]).toEqual({
      fileName: 'Snippet.java',
      sourceCode: 'public class Demo {}'
    })
  })
})
