import { fireEvent, render, screen, within } from '@testing-library/vue'
import DesktopTitleBar from '../DesktopTitleBar.vue'

describe('DesktopTitleBar', () => {
  test('renders the slim Codex-style menu bar with a centered product title', async () => {
    const { emitted } = render(DesktopTitleBar, {
      props: {
        isMaximized: false
      }
    })

    const banner = screen.getByRole('banner')
    const controls = banner.querySelector('.desktop-titlebar__window-controls')

    expect(banner).toHaveTextContent('Piggy Metrics')
    expect(banner.querySelector('.desktop-titlebar__center-title')).toHaveTextContent('Piggy Metrics')
    expect(banner.querySelector('.desktop-titlebar__center-title')).toHaveClass('desktop-titlebar__center-title--bold')
    expect(banner.querySelector('.desktop-titlebar__brand-mark')).not.toBeInTheDocument()
    expect(screen.getByRole('button', { name: '后退' })).toBeDisabled()
    expect(screen.getByRole('button', { name: '前进' })).toBeDisabled()
    expect(screen.getByRole('button', { name: '文件' })).toBeInTheDocument()
    expect(screen.getByRole('button', { name: '编辑' })).toBeInTheDocument()
    expect(screen.getByRole('button', { name: '查看' })).toBeInTheDocument()
    expect(screen.getByRole('button', { name: '窗口' })).toBeInTheDocument()
    expect(screen.getByRole('button', { name: '帮助' })).toBeInTheDocument()
    expect(banner.querySelector('.desktop-titlebar__drag-region')).toBeInTheDocument()
    expect(banner.querySelector('.desktop-titlebar__utility')).not.toBeInTheDocument()
    expect(screen.queryByText('后端状态：UP')).not.toBeInTheDocument()
    expect(screen.queryByRole('button', { name: '切换到亮色主题' })).not.toBeInTheDocument()
    expect(screen.getByRole('button', { name: '关闭窗口' })).toBeInTheDocument()
    expect(screen.getByRole('button', { name: '最小化窗口' })).toBeInTheDocument()
    expect(screen.getByRole('button', { name: '最大化窗口' })).toBeInTheDocument()
    expect(controls).toBeInTheDocument()
    expect(within(controls).getAllByRole('button').map((button) => button.getAttribute('aria-label'))).toEqual([
      '最小化窗口',
      '最大化窗口',
      '关闭窗口'
    ])

    await fireEvent.click(screen.getByRole('button', { name: '文件' }))
    await fireEvent.click(screen.getByRole('button', { name: '帮助' }))
    await fireEvent.click(screen.getByRole('button', { name: '最小化窗口' }))
    await fireEvent.click(screen.getByRole('button', { name: '最大化窗口' }))
    await fireEvent.click(screen.getByRole('button', { name: '关闭窗口' }))

    expect(emitted()['show-app-menu']).toHaveLength(2)
    expect(emitted()['toggle-theme']).toBeUndefined()
    expect(emitted()['minimize-window']).toHaveLength(1)
    expect(emitted()['toggle-maximize-window']).toHaveLength(1)
    expect(emitted()['close-window']).toHaveLength(1)
  })
})
