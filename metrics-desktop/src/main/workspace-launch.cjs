async function launchWorkspace(options = {}) {
  const analyzerState = options.analyzerState || null
  const logger = options.logger

  if (!analyzerState?.analyzerReady) {
    logger?.warn('Desktop analyzer not ready for direct launch', {
      analyzerJarPath: analyzerState?.analyzerJarPath || null
    })
  }

  const mainWindow = await options.createMainWindow()
  if (mainWindow && typeof mainWindow.show === 'function') {
    const destroyed = typeof mainWindow.isDestroyed === 'function' && mainWindow.isDestroyed()
    if (!destroyed) {
      mainWindow.show()
    }
  }

  return {
    analyzerReady: Boolean(analyzerState?.analyzerReady),
    mainWindow
  }
}

module.exports = {
  launchWorkspace
}
