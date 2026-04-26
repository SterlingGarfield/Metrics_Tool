const assert = require('node:assert/strict')
const { EventEmitter } = require('node:events')

class FakeWindow extends EventEmitter {
  constructor() {
    super()
    this.webContents = new EventEmitter()
  }
}

async function run() {
  const { loadWindowUntilReady } = require('../src/main/window-lifecycle.cjs')
  const fakeWindow = new FakeWindow()

  await loadWindowUntilReady(fakeWindow, () => {
    fakeWindow.emit('ready-to-show')
    fakeWindow.webContents.emit('did-finish-load')
    return Promise.resolve()
  })

  console.log('window-lifecycle.test.cjs: PASS')
}

run().catch((error) => {
  console.error('window-lifecycle.test.cjs: FAIL')
  console.error(error)
  process.exitCode = 1
})
