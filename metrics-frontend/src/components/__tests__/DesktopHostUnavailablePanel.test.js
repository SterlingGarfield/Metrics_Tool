import { render, screen } from '@testing-library/vue'
import DesktopHostUnavailablePanel from '../DesktopHostUnavailablePanel.vue'

describe('DesktopHostUnavailablePanel', () => {
  test('explains that the renderer must be launched from the desktop host', () => {
    render(DesktopHostUnavailablePanel)

    expect(screen.getByText('请从 Metrics Desktop 启动当前工作台')).toBeInTheDocument()
    expect(screen.getByText(/当前界面已经切换为纯桌面端产品路径/)).toBeInTheDocument()
    expect(screen.getByText(/Electron 宿主或安装包启动/)).toBeInTheDocument()
  })
})
