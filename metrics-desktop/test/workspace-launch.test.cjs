const assert = require('node:assert/strict')

async function run() {
  const { launchWorkspace } = require('../src/main/workspace-launch.cjs')
  const events = []
  const mainWindow = {
    show() {
      events.push('show')
    },
    isDestroyed() {
      return false
    }
  }

  const result = await launchWorkspace({
    analyzerState: {
      analyzerReady: false,
      analyzerJarPath: 'missing.jar'
    },
    createMainWindow: async () => {
      events.push('create')
      return mainWindow
    },
    logger: {
      warn(message, details) {
        events.push({ level: 'warn', message, details })
      }
    }
  })

  assert.equal(result.analyzerReady, false)
  assert.equal(events.includes('create'), true)
  assert.equal(events.includes('show'), true)
  assert.equal(
    events.some((entry) => entry.level === 'warn' && entry.message === 'Desktop analyzer not ready for direct launch'),
    true
  )
  console.log('workspace-launch.test.cjs: PASS')
}

run().catch((error) => {
  console.error('workspace-launch.test.cjs: FAIL')
  console.error(error)
  process.exitCode = 1
})
