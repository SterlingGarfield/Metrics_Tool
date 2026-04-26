function loadWindowUntilReady(browserWindow, loadAction, options = {}) {
  const timeoutMs = options.timeoutMs || 15000
  const logger = options.logger

  return new Promise((resolve, reject) => {
    let settled = false
    let timer = null

    const cleanup = () => {
      if (timer) {
        clearTimeout(timer)
        timer = null
      }

      browserWindow.removeListener('ready-to-show', onReadyToShow)
      browserWindow.webContents.removeListener('did-finish-load', onDidFinishLoad)
      browserWindow.webContents.removeListener('did-fail-load', onDidFailLoad)
    }

    const finish = (eventName) => {
      if (settled) {
        return
      }

      settled = true
      cleanup()
      logger?.info('Main window became ready', { eventName })
      resolve(eventName)
    }

    const fail = (error) => {
      if (settled) {
        return
      }

      settled = true
      cleanup()
      logger?.error('Main window failed before becoming ready', { message: error.message })
      reject(error)
    }

    const onReadyToShow = () => finish('ready-to-show')
    const onDidFinishLoad = () => finish('did-finish-load')
    const onDidFailLoad = (_event, code, description, validatedUrl, isMainFrame) => {
      if (isMainFrame === false) {
        return
      }

      fail(new Error(`Main window failed to load (${code}): ${description} ${validatedUrl || ''}`.trim()))
    }

    browserWindow.once('ready-to-show', onReadyToShow)
    browserWindow.webContents.once('did-finish-load', onDidFinishLoad)
    browserWindow.webContents.once('did-fail-load', onDidFailLoad)

    timer = setTimeout(() => {
      fail(new Error(`Main window did not become ready within ${timeoutMs}ms`))
    }, timeoutMs)

    Promise.resolve()
      .then(loadAction)
      .catch(fail)
  })
}

module.exports = {
  loadWindowUntilReady
}
