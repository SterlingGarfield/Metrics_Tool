const { dialog, shell } = require('electron')
const path = require('path')
const { registerDesktopWindowHandlers } = require('./desktop-window.cjs')
const {
  analyzeDesign,
  analyzeEstimation,
  analyzeUseCasePoints,
  analyzeSources,
  analyzeText,
  getAnalyzerStatus,
  suggestDesignMetrics
} = require('./analyzer.cjs')
const { collectJavaFileDescriptors, readSourcesFromFolder, readSourcesFromPaths } = require('./fs-sources.cjs')

async function selectFiles(options = {}) {
  const properties = ['openFile']
  if (options.multiple) {
    properties.push('multiSelections')
  }

  const result = await dialog.showOpenDialog({
    properties,
    filters: [
      {
        name: 'Java Source',
        extensions: ['java']
      }
    ]
  })

  if (result.canceled) {
    return []
  }

  return result.filePaths.map((filePath) => ({
    path: filePath,
    name: path.basename(filePath)
  }))
}

async function selectFolder() {
  const result = await dialog.showOpenDialog({
    properties: ['openDirectory']
  })

  if (result.canceled || result.filePaths.length === 0) {
    return null
  }

  const folderPath = result.filePaths[0]
  return {
    folderPath,
    files: await collectJavaFileDescriptors(folderPath)
  }
}

async function saveReport(defaultPath, content) {
  const result = await dialog.showSaveDialog({
    defaultPath
  })

  if (result.canceled || !result.filePath) {
    return { canceled: true }
  }

  const fs = require('fs/promises')
  await fs.writeFile(result.filePath, content, 'utf8')
  return { canceled: false, filePath: result.filePath }
}

function registerIpcHandlers(ipcMain, context, actions = {}) {
  ipcMain.handle('metrics:getAppStatus', async () => ({
    data: await getAnalyzerStatus(context.analyzerState)
  }))

  ipcMain.handle('metrics:analyzeText', async (_, payload) => ({
    data: await analyzeText(context.analyzerState, payload)
  }))

  ipcMain.handle('metrics:analyzeDesign', async (_, payload) => ({
    data: await analyzeDesign(context.analyzerState, payload)
  }))

  ipcMain.handle('metrics:analyzeEstimation', async (_, payload) => ({
    data: await analyzeEstimation(context.analyzerState, payload)
  }))

  ipcMain.handle('metrics:analyzeUseCasePoints', async (_, payload) => ({
    data: await analyzeUseCasePoints(context.analyzerState, payload)
  }))

  ipcMain.handle('metrics:suggestDesignMetrics', async (_, payload) => ({
    data: await suggestDesignMetrics(context.analyzerState, payload)
  }))

  ipcMain.handle('metrics:analyzeFiles', async (_, selections) => ({
    data: await analyzeSources(context.analyzerState, await readSourcesFromPaths(selections))
  }))

  ipcMain.handle('metrics:analyzeFolder', async (_, selection) => ({
    data: await analyzeSources(context.analyzerState, await readSourcesFromFolder(selection))
  }))

  ipcMain.handle('metrics:selectFiles', async (_, options) => selectFiles(options))
  ipcMain.handle('metrics:selectFolder', async () => selectFolder())
  ipcMain.handle('metrics:exportCsv', async (_, payload) => saveReport(payload.filename, payload.content))
  ipcMain.handle('metrics:exportMarkdown', async (_, payload) => saveReport(payload.filename, payload.content))
  ipcMain.handle('desktop:getBootState', async () => context.bootState.snapshot())
  ipcMain.handle('desktop:retryBoot', async () => actions.retryBoot?.())
  ipcMain.handle('desktop:openLogs', async () => shell.openPath(context.runtimePaths.logsDir))
  registerDesktopWindowHandlers(ipcMain, {
    getMainWindow: actions.getMainWindow,
    showAppMenu: actions.showAppMenu
  })
}

module.exports = {
  registerIpcHandlers
}
