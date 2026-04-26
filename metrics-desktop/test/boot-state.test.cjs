const assert = require('node:assert/strict')

function run() {
  const { createBootState } = require('../src/main/boot-state.cjs')

  const bootState = createBootState()
  bootState.update('init', 'Preparing runtime')
  bootState.fail('missing_jar', 'Desktop analyzer jar not found')

  assert.equal(bootState.snapshot().status, 'failed')
  assert.equal(bootState.snapshot().stage, 'init')
  assert.equal(bootState.snapshot().code, 'missing_jar')
  assert.equal(bootState.snapshot().message, 'Desktop analyzer jar not found')
  console.log('boot-state.test.cjs: PASS')
}

try {
  run()
} catch (error) {
  console.error('boot-state.test.cjs: FAIL')
  console.error(error)
  process.exitCode = 1
}
