const { Menu, shell } = require('electron')

function buildAppMenuTemplate(context = {}) {
  const helpItems = [
    {
      label: '打开日志目录',
      click: () => shell.openPath(context.runtimePaths?.logsDir || '')
    }
  ]

  return [
    {
      label: 'File',
      submenu: [
        {
          label: '关闭窗口',
          role: 'close'
        },
        process.platform === 'darwin'
          ? null
          : {
              label: '退出应用',
              role: 'quit'
            }
      ].filter(Boolean)
    },
    {
      label: 'Edit',
      submenu: [
        { role: 'undo' },
        { role: 'redo' },
        { type: 'separator' },
        { role: 'cut' },
        { role: 'copy' },
        { role: 'paste' },
        { role: 'selectAll' }
      ]
    },
    {
      label: 'View',
      submenu: [
        { role: 'reload' },
        { role: 'forceReload' },
        { type: 'separator' },
        { role: 'resetZoom' },
        { role: 'zoomIn' },
        { role: 'zoomOut' },
        { type: 'separator' },
        { role: 'togglefullscreen' }
      ]
    },
    {
      label: 'Window',
      submenu: [
        { role: 'minimize' },
        { role: 'close' }
      ]
    },
    {
      label: 'Help',
      submenu: helpItems
    }
  ]
}

function createAppMenu(context = {}) {
  return Menu.buildFromTemplate(buildAppMenuTemplate(context))
}

function installApplicationMenu(context = {}) {
  const menu = createAppMenu(context)
  Menu.setApplicationMenu(menu)
  return menu
}

function showAppMenu(context = {}, browserWindow) {
  const menu = createAppMenu(context)
  menu.popup({
    window: browserWindow || undefined
  })
  return menu
}

module.exports = {
  buildAppMenuTemplate,
  createAppMenu,
  installApplicationMenu,
  showAppMenu
}
