const assert = require('node:assert/strict')
const { EventEmitter } = require('node:events')

class FakeWindow extends EventEmitter {
  constructor() {
    super()
    this.maximized = false
    this.closed = false
    this.minimized = false
  }

  isMaximized() {
    return this.maximized
  }

  maximize() {
    this.maximized = true
  }

  unmaximize() {
    this.maximized = false
  }

  minimize() {
    this.minimized = true
  }

  close() {
    this.closed = true
  }
}

async function run() {
  const {
    buildMainWindowOptions,
    getWindowStateSnapshot,
    toggleWindowMaximize,
    bindWindowStateEvents,
    registerDesktopWindowHandlers
  } = require('../src/main/desktop-window.cjs')

  const options = buildMainWindowOptions({
    preloadPath: 'D:/Projects/SQA/Metrics_Tool/metrics-desktop/src/preload/index.cjs'
  })

  assert.equal(options.frame, false)
  assert.equal(options.autoHideMenuBar, true)
  assert.equal(options.backgroundColor, '#141413')

  const fakeWindow = new FakeWindow()
  const events = []
  const unsubscribe = bindWindowStateEvents(fakeWindow, (state) => {
    events.push(state)
  })

  assert.deepEqual(getWindowStateSnapshot(fakeWindow), { isMaximized: false })

  fakeWindow.maximized = true
  fakeWindow.emit('maximize')
  assert.deepEqual(events.at(-1), { isMaximized: true })

  fakeWindow.maximized = false
  fakeWindow.emit('unmaximize')
  assert.deepEqual(events.at(-1), { isMaximized: false })

  unsubscribe()

  toggleWindowMaximize(fakeWindow)
  assert.equal(fakeWindow.isMaximized(), true)

  toggleWindowMaximize(fakeWindow)
  assert.equal(fakeWindow.isMaximized(), false)

  const handlers = new Map()
  const popupCalls = []
  registerDesktopWindowHandlers({
    handle(channel, handler) {
      handlers.set(channel, handler)
    }
  }, {
    getMainWindow: () => fakeWindow,
    showAppMenu: (browserWindow) => {
      popupCalls.push(browserWindow)
    }
  })

  assert.equal(typeof handlers.get('desktop:getWindowState'), 'function')
  assert.equal(typeof handlers.get('desktop:minimizeWindow'), 'function')
  assert.equal(typeof handlers.get('desktop:toggleMaximizeWindow'), 'function')
  assert.equal(typeof handlers.get('desktop:closeWindow'), 'function')
  assert.equal(typeof handlers.get('desktop:showAppMenu'), 'function')

  assert.deepEqual(await handlers.get('desktop:getWindowState')(), { isMaximized: false })
  await handlers.get('desktop:minimizeWindow')()
  assert.equal(fakeWindow.minimized, true)
  await handlers.get('desktop:toggleMaximizeWindow')()
  assert.equal(fakeWindow.isMaximized(), true)
  await handlers.get('desktop:closeWindow')()
  assert.equal(fakeWindow.closed, true)
  await handlers.get('desktop:showAppMenu')()
  assert.equal(popupCalls.length, 1)
  console.log('desktop-window.test.cjs: PASS')
}

run().catch((error) => {
  console.error('desktop-window.test.cjs: FAIL')
  console.error(error)
  process.exitCode = 1
})
