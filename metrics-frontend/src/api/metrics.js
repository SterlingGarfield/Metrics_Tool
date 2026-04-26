import { buildCsv, buildMarkdownReport } from '../utils/exporters'

const DESKTOP_HOST_UNAVAILABLE_MESSAGE = '桌面宿主不可用，请从 Metrics Desktop 启动应用。'

function getDesktopBridge() {
  if (typeof window === 'undefined') {
    return null
  }

  const bridge = window.metricsDesktop
  if (!bridge || typeof bridge.getAppStatus !== 'function') {
    return null
  }

  return bridge
}

function requireDesktopBridge(...requiredMethods) {
  const bridge = getDesktopBridge()
  if (!bridge) {
    throw new Error(DESKTOP_HOST_UNAVAILABLE_MESSAGE)
  }

  for (const methodName of requiredMethods) {
    if (typeof bridge[methodName] !== 'function') {
      throw new Error(DESKTOP_HOST_UNAVAILABLE_MESSAGE)
    }
  }

  return bridge
}

function callDesktopBridge(methodName, ...args) {
  return Promise.resolve().then(() => requireDesktopBridge(methodName)[methodName](...args))
}

export function getAppStatus() {
  return callDesktopBridge('getAppStatus')
}

export function checkHealth() {
  return getAppStatus()
}

export function analyzeText(payload) {
  return callDesktopBridge('analyzeText', payload)
}

export function analyzeFiles(files) {
  return callDesktopBridge('analyzeFiles', files || [])
}

export function analyzeFolder(files) {
  return callDesktopBridge('analyzeFolder', files)
}

export function analyzeDesign(payload) {
  return callDesktopBridge('analyzeDesign', payload)
}

export function analyzeEstimation(payload) {
  return callDesktopBridge('analyzeEstimation', payload)
}

export function analyzeUseCasePoints(payload) {
  return callDesktopBridge('analyzeUseCasePoints', payload)
}

export function getDesktopWindowState() {
  return callDesktopBridge('getWindowState')
}

export function minimizeDesktopWindow() {
  return callDesktopBridge('minimizeWindow')
}

export function toggleDesktopMaximizeWindow() {
  return callDesktopBridge('toggleMaximizeWindow')
}

export function closeDesktopWindow() {
  return callDesktopBridge('closeWindow')
}

export function showDesktopAppMenu() {
  return callDesktopBridge('showAppMenu')
}

export function onDesktopWindowStateChanged(listener) {
  return requireDesktopBridge('onWindowStateChanged').onWindowStateChanged(listener)
}

async function serializeImageForDesktop(image) {
  if (!image) {
    return {
      imageBytes: [],
      imageName: '',
      imageType: ''
    }
  }

  let imageBuffer = new ArrayBuffer(0)
  if (typeof image.arrayBuffer === 'function') {
    imageBuffer = await image.arrayBuffer()
  } else if (typeof Response !== 'undefined') {
    imageBuffer = await new Response(image).arrayBuffer()
  }

  return {
    imageBytes: Array.from(new Uint8Array(imageBuffer)),
    imageName: image.name || '',
    imageType: image.type || ''
  }
}

export async function suggestDesignMetrics({ diagramType, image }) {
  const desktopPayload = await serializeImageForDesktop(image)
  return callDesktopBridge('suggestDesignMetrics', {
    diagramType,
    ...desktopPayload
  })
}

export function selectFiles(options = {}) {
  return callDesktopBridge('selectFiles', options)
}

export function selectFolder() {
  return callDesktopBridge('selectFolder')
}

export function exportCsv(result, filename = 'metrics-report.csv') {
  const content = buildCsv(result)
  return callDesktopBridge('exportCsv', { filename, content })
}

export function exportMarkdown(result, filename = 'metrics-report.md') {
  const content = buildMarkdownReport(result)
  return callDesktopBridge('exportMarkdown', { filename, content })
}
