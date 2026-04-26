const fs = require('fs')
const path = require('path')
const { app, BrowserWindow, ipcMain } = require('electron')
const { installApplicationMenu, showAppMenu } = require('./app-menu.cjs')
const { createAnalyzerState, disposeAnalyzerState } = require('./analyzer.cjs')
const { createBootState } = require('./boot-state.cjs')
const {
  bindWindowStateEvents,
  buildMainWindowOptions
} = require('./desktop-window.cjs')
const { createLogger } = require('./logger.cjs')
const { registerIpcHandlers } = require('./ipc.cjs')
const { ensureDesktopDirectories } = require('./runtime-paths.cjs')
const { loadWindowUntilReady } = require('./window-lifecycle.cjs')
const { launchWorkspace } = require('./workspace-launch.cjs')

let desktopContext = null
let mainWindow = null
let releaseWindowStateEvents = () => {}

function createMainWindow() {
  if (!mainWindow || mainWindow.isDestroyed()) {
    mainWindow = new BrowserWindow(buildMainWindowOptions({
      preloadPath: path.resolve(__dirname, '../preload/index.cjs')
    }))
    mainWindow.setMenuBarVisibility(false)
    releaseWindowStateEvents = bindWindowStateEvents(mainWindow, (state) => {
      if (!mainWindow || mainWindow.isDestroyed()) {
        return
      }

      mainWindow.webContents.send('desktop:windowStateChanged', state)
    })

    mainWindow.on('closed', () => {
      releaseWindowStateEvents()
      releaseWindowStateEvents = () => {}
      mainWindow = null
    })
  }

  const devServerUrl = process.env.METRICS_DESKTOP_DEV_SERVER_URL
  const packagedFrontendEntry = path.join(process.resourcesPath || '', 'payload', 'frontend', 'index.html')
  const localFrontendEntry = path.resolve(__dirname, '../../../metrics-frontend/dist/index.html')

  return loadWindowUntilReady(mainWindow, () => {
    if (devServerUrl) {
      desktopContext?.logger?.info('Loading desktop UI from dev server', { devServerUrl })
      return mainWindow.loadURL(devServerUrl)
    }

    if (fs.existsSync(packagedFrontendEntry)) {
      desktopContext?.logger?.info('Loading desktop UI from packaged frontend', { packagedFrontendEntry })
      return mainWindow.loadFile(packagedFrontendEntry)
    }

    desktopContext?.logger?.info('Loading desktop UI from local frontend dist', { localFrontendEntry })
    return mainWindow.loadFile(localFrontendEntry)
  }, {
    logger: desktopContext?.logger
  }).then(() => mainWindow)
}

async function bootDesktopShell(options = {}) {
  desktopContext.bootState.reset()
  desktopContext.bootState.update('workspace', '正在直接装载桌面工作区。')

  if (options.forceRetry) {
    desktopContext.logger.info('Retrying desktop workspace launch')
  }

  disposeAnalyzerState(desktopContext.analyzerState)
  desktopContext.analyzerState = createAnalyzerState({
    runtimePaths: desktopContext.runtimePaths,
    logger: desktopContext.logger
  })

  const launchResult = await launchWorkspace({
    analyzerState: desktopContext.analyzerState,
    createMainWindow,
    logger: desktopContext.logger
  })

  desktopContext.bootState.ready(
    launchResult.analyzerReady
      ? '桌面工作区已准备完成。'
      : '桌面工作区已打开，分析运行时尚未就绪。'
  )
}

app.whenReady().then(async () => {
  const runtimePaths = await ensureDesktopDirectories({
    appDataRoot: path.join(app.getPath('appData'), 'Metrics Tool Desktop')
  })
  const logger = createLogger(runtimePaths)
  logger.info('Desktop shell starting', { appDataRoot: runtimePaths.appDataRoot })

  desktopContext = {
    runtimePaths,
    logger,
    bootState: createBootState(),
    analyzerState: createAnalyzerState({ runtimePaths, logger })
  }
  desktopContext.appMenu = installApplicationMenu(desktopContext)

  registerIpcHandlers(ipcMain, desktopContext, {
    retryBoot: async () => bootDesktopShell({ forceRetry: true }),
    getMainWindow: () => mainWindow,
    showAppMenu: (browserWindow) => showAppMenu(desktopContext, browserWindow)
  })
  await bootDesktopShell()
})

app.on('activate', async () => {
  if (BrowserWindow.getAllWindows().length === 0) {
    await bootDesktopShell()
  }
})

app.on('before-quit', () => {
  disposeAnalyzerState(desktopContext?.analyzerState)
})

app.on('window-all-closed', () => {
  if (process.platform !== 'darwin') {
    app.quit()
  }
})
