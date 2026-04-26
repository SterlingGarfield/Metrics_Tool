const { contextBridge, ipcRenderer } = require('electron')

contextBridge.exposeInMainWorld('metricsDesktopSplash', {
  getBootState: () => ipcRenderer.invoke('desktop:getBootState'),
  retryBoot: () => ipcRenderer.invoke('desktop:retryBoot'),
  openLogs: () => ipcRenderer.invoke('desktop:openLogs'),
  onBootStateChanged: (listener) => {
    const handler = (_, state) => listener(state)
    ipcRenderer.on('desktop:bootStateChanged', handler)
    return () => ipcRenderer.removeListener('desktop:bootStateChanged', handler)
  }
})
