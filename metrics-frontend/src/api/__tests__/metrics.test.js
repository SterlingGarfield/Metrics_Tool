import { beforeEach, describe, expect, test, vi } from 'vitest'

describe('metrics API desktop bridge', () => {
  beforeEach(() => {
    vi.resetModules()
    delete window.metricsDesktop
  })

  test('throws a desktop-host error when the renderer is opened without Electron', async () => {
    const api = await import('../metrics.js')

    await expect(api.checkHealth()).rejects.toThrow(/桌面宿主不可用/)
    await expect(
      api.analyzeText({ fileName: 'Demo.java', sourceCode: 'public class Demo {}' })
    ).rejects.toThrow(/桌面宿主不可用/)
    await expect(api.getDesktopWindowState()).rejects.toThrow(/桌面宿主不可用/)
    await expect(api.exportCsv({ codeMetrics: { available: true } })).rejects.toThrow(/桌面宿主不可用/)
    expect(() => api.onDesktopWindowStateChanged(() => {})).toThrow(/桌面宿主不可用/)
  })

  test('uses the desktop bridge for analysis, export and window actions', async () => {
    const bridge = {
      getAppStatus: vi.fn().mockResolvedValue({ data: { status: 'UP', mode: 'desktop' } }),
      analyzeText: vi.fn().mockResolvedValue({ data: { codeMetrics: { available: true } } }),
      analyzeFiles: vi.fn().mockResolvedValue({ data: { codeMetrics: { available: true } } }),
      analyzeFolder: vi.fn().mockResolvedValue({ data: { codeMetrics: { available: true } } }),
      analyzeDesign: vi.fn().mockResolvedValue({ data: { designMetrics: { available: true } } }),
      analyzeEstimation: vi.fn().mockResolvedValue({ data: { estimationMetrics: { available: true } } }),
      analyzeUseCasePoints: vi.fn().mockResolvedValue({ data: { estimationMetrics: { available: true } } }),
      suggestDesignMetrics: vi.fn().mockResolvedValue({ data: { available: true } }),
      selectFiles: vi.fn().mockResolvedValue(['A.java']),
      selectFolder: vi.fn().mockResolvedValue({ files: ['Folder.java'] }),
      exportCsv: vi.fn().mockResolvedValue({ canceled: false }),
      exportMarkdown: vi.fn().mockResolvedValue({ canceled: false }),
      getWindowState: vi.fn().mockResolvedValue({ isMaximized: true }),
      minimizeWindow: vi.fn().mockResolvedValue({ ok: true }),
      toggleMaximizeWindow: vi.fn().mockResolvedValue({ isMaximized: false }),
      closeWindow: vi.fn().mockResolvedValue({ ok: true }),
      showAppMenu: vi.fn().mockResolvedValue({ ok: true }),
      onWindowStateChanged: vi.fn(() => () => {})
    }
    window.metricsDesktop = bridge

    const api = await import('../metrics.js')

    await api.checkHealth()
    await api.analyzeText({ fileName: 'DesktopDemo.java', sourceCode: 'public class DesktopDemo {}' })
    await api.analyzeFiles(['A.java'])
    await api.analyzeFolder({ files: ['Folder.java'] })
    await api.analyzeDesign({ diagramType: 'class', classCount: 8 })
    await api.analyzeEstimation({ loc: 800, staffCount: 4, devMonths: 2, cost: 6000 })
    await api.analyzeUseCasePoints({
      simpleActors: 1,
      averageActors: 2,
      complexActors: 1,
      simpleUseCases: 2,
      averageUseCases: 1,
      complexUseCases: 1,
      technicalComplexityFactor: 1.1,
      environmentalComplexityFactor: 0.9
    })
    await api.selectFiles({ multiple: true })
    await api.selectFolder()
    await api.getDesktopWindowState()
    await api.minimizeDesktopWindow()
    await api.toggleDesktopMaximizeWindow()
    await api.closeDesktopWindow()
    await api.showDesktopAppMenu()
    await api.exportCsv({ codeMetrics: { available: true, classMetrics: [], methodMetrics: [] } })
    await api.exportMarkdown({ codeMetrics: { available: true, classMetrics: [], methodMetrics: [] } })
    await api.suggestDesignMetrics({
      diagramType: 'class',
      image: new File(['diagram'], 'desktop-design.png', { type: 'image/png' })
    })

    expect(bridge.getAppStatus).toHaveBeenCalledTimes(1)
    expect(bridge.analyzeText).toHaveBeenCalledWith({
      fileName: 'DesktopDemo.java',
      sourceCode: 'public class DesktopDemo {}'
    })
    expect(bridge.analyzeFiles).toHaveBeenCalledWith(['A.java'])
    expect(bridge.analyzeFolder).toHaveBeenCalledWith({ files: ['Folder.java'] })
    expect(bridge.analyzeDesign).toHaveBeenCalledWith({
      diagramType: 'class',
      classCount: 8
    })
    expect(bridge.analyzeEstimation).toHaveBeenCalledWith({
      loc: 800,
      staffCount: 4,
      devMonths: 2,
      cost: 6000
    })
    expect(bridge.analyzeUseCasePoints).toHaveBeenCalledWith({
      simpleActors: 1,
      averageActors: 2,
      complexActors: 1,
      simpleUseCases: 2,
      averageUseCases: 1,
      complexUseCases: 1,
      technicalComplexityFactor: 1.1,
      environmentalComplexityFactor: 0.9
    })
    expect(bridge.selectFiles).toHaveBeenCalledWith({ multiple: true })
    expect(bridge.selectFolder).toHaveBeenCalledTimes(1)
    expect(bridge.exportCsv).toHaveBeenCalledTimes(1)
    expect(bridge.exportMarkdown).toHaveBeenCalledTimes(1)
    expect(bridge.getWindowState).toHaveBeenCalledTimes(1)
    expect(bridge.minimizeWindow).toHaveBeenCalledTimes(1)
    expect(bridge.toggleMaximizeWindow).toHaveBeenCalledTimes(1)
    expect(bridge.closeWindow).toHaveBeenCalledTimes(1)
    expect(bridge.showAppMenu).toHaveBeenCalledTimes(1)
    expect(bridge.suggestDesignMetrics).toHaveBeenCalledWith(
      expect.objectContaining({
        diagramType: 'class',
        imageName: 'desktop-design.png',
        imageType: 'image/png'
      })
    )
    expect(api.onDesktopWindowStateChanged(() => {})).toEqual(expect.any(Function))
  })
})
