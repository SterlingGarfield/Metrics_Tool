import { fireEvent, render, screen } from '@testing-library/vue'
import DesktopSidebar from '../DesktopSidebar.vue'

describe('DesktopSidebar', () => {
  test('renders top-level workspaces and exposes a stronger active state for the selected modes', async () => {
    const { emitted, rerender } = render(DesktopSidebar, {
      props: {
        workspace: 'code',
        codeMode: 'text'
      }
    })

    expect(screen.getByRole('navigation', { name: '桌面工作区导航' })).toBeInTheDocument()
    expect(screen.getByRole('button', { name: '代码度量' })).toHaveAttribute('aria-pressed', 'true')
    expect(screen.getByRole('button', { name: '代码度量' })).toHaveAttribute('aria-current', 'page')
    expect(screen.getByRole('button', { name: '代码输入' })).toHaveAttribute('aria-pressed', 'true')
    expect(screen.getByRole('button', { name: '代码输入' })).toHaveAttribute('aria-current', 'page')
    expect(screen.getByRole('button', { name: '单文件分析' })).toHaveAttribute('aria-label', '单文件分析')
    expect(document.querySelectorAll('.desktop-sidebar__item-icon svg')).toHaveLength(3)
    expect(document.querySelectorAll('.desktop-sidebar__subitem-icon svg')).toHaveLength(4)

    await fireEvent.click(screen.getByRole('button', { name: '设计度量' }))
    expect(emitted()['update:workspace'][0]).toEqual(['design'])

    await rerender({
      workspace: 'design',
      codeMode: 'text'
    })

    expect(screen.getByRole('button', { name: '设计度量' })).toHaveAttribute('aria-current', 'page')
    expect(screen.queryByRole('button', { name: '代码输入' })).not.toBeInTheDocument()
    expect(document.querySelectorAll('.desktop-sidebar__item-icon svg')).toHaveLength(3)

    await fireEvent.click(screen.getByRole('button', { name: '代码度量' }))
    expect(emitted()['update:workspace'][1]).toEqual(['code'])

    await rerender({
      workspace: 'code',
      codeMode: 'files'
    })

    expect(screen.getByRole('button', { name: '多文件分析' })).toHaveAttribute('aria-current', 'page')
    await fireEvent.click(screen.getByRole('button', { name: '文件夹扫描' }))
    expect(emitted()['update:codeMode'][0]).toEqual(['folder'])
  })
})
