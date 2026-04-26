const { contextBridge, ipcRenderer } = require('electron')

contextBridge.exposeInMainWorld('metricsDesktop', {
  getAppStatus: () => ipcRenderer.invoke('metrics:getAppStatus'),
  analyzeText: (payload) => ipcRenderer.invoke('metrics:analyzeText', payload),
  analyzeDesign: (payload) => ipcRenderer.invoke('metrics:analyzeDesign', payload),
  analyzeEstimation: (payload) => ipcRenderer.invoke('metrics:analyzeEstimation', payload),
  analyzeUseCasePoints: (payload) => ipcRenderer.invoke('metrics:analyzeUseCasePoints', payload),
  suggestDesignMetrics: (payload) => ipcRenderer.invoke('metrics:suggestDesignMetrics', payload),
  analyzeFiles: (selections) => ipcRenderer.invoke('metrics:analyzeFiles', selections),
  analyzeFolder: (selection) => ipcRenderer.invoke('metrics:analyzeFolder', selection),
  selectFiles: (options) => ipcRenderer.invoke('metrics:selectFiles', options),
  selectFolder: () => ipcRenderer.invoke('metrics:selectFolder'),
  exportCsv: (payload) => ipcRenderer.invoke('metrics:exportCsv', payload),
  exportMarkdown: (payload) => ipcRenderer.invoke('metrics:exportMarkdown', payload),
  getWindowState: () => ipcRenderer.invoke('desktop:getWindowState'),
  minimizeWindow: () => ipcRenderer.invoke('desktop:minimizeWindow'),
  toggleMaximizeWindow: () => ipcRenderer.invoke('desktop:toggleMaximizeWindow'),
  closeWindow: () => ipcRenderer.invoke('desktop:closeWindow'),
  showAppMenu: () => ipcRenderer.invoke('desktop:showAppMenu'),
  onWindowStateChanged: (listener) => {
    const handler = (_event, state) => listener(state)
    ipcRenderer.on('desktop:windowStateChanged', handler)
    return () => ipcRenderer.removeListener('desktop:windowStateChanged', handler)
  }
})
