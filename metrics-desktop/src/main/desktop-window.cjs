function buildMainWindowOptions({ preloadPath }) {
  return {
    width: 1440,
    height: 920,
    minWidth: 1100,
    minHeight: 760,
    show: false,
    frame: false,
    autoHideMenuBar: true,
    backgroundColor: '#141413',
    webPreferences: {
      preload: preloadPath,
      contextIsolation: true,
      nodeIntegration: false
    }
  }
}

function getWindowStateSnapshot(browserWindow) {
  return {
    isMaximized: Boolean(browserWindow?.isMaximized?.())
  }
}

function toggleWindowMaximize(browserWindow) {
  if (!browserWindow || typeof browserWindow.isMaximized !== 'function') {
    return getWindowStateSnapshot(browserWindow)
  }

  if (browserWindow.isMaximized()) {
    browserWindow.unmaximize?.()
  } else {
    browserWindow.maximize?.()
  }

  return getWindowStateSnapshot(browserWindow)
}

function bindWindowStateEvents(browserWindow, notify) {
  if (!browserWindow || typeof notify !== 'function') {
    return () => {}
  }

  const emitState = () => notify(getWindowStateSnapshot(browserWindow))
  browserWindow.on('maximize', emitState)
  browserWindow.on('unmaximize', emitState)

  return () => {
    browserWindow.removeListener('maximize', emitState)
    browserWindow.removeListener('unmaximize', emitState)
  }
}

function registerDesktopWindowHandlers(ipcMain, actions = {}) {
  const getMainWindow = typeof actions.getMainWindow === 'function'
    ? actions.getMainWindow
    : () => null

  ipcMain.handle('desktop:getWindowState', async () => {
    return getWindowStateSnapshot(getMainWindow())
  })

  ipcMain.handle('desktop:minimizeWindow', async () => {
    getMainWindow()?.minimize?.()
    return { ok: true }
  })

  ipcMain.handle('desktop:toggleMaximizeWindow', async () => {
    return toggleWindowMaximize(getMainWindow())
  })

  ipcMain.handle('desktop:closeWindow', async () => {
    getMainWindow()?.close?.()
    return { ok: true }
  })

  ipcMain.handle('desktop:showAppMenu', async () => {
    actions.showAppMenu?.(getMainWindow())
    return { ok: true }
  })
}

module.exports = {
  bindWindowStateEvents,
  buildMainWindowOptions,
  getWindowStateSnapshot,
  registerDesktopWindowHandlers,
  toggleWindowMaximize
}
