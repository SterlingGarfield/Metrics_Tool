const assert = require('node:assert/strict')

function run() {
  const { buildAppMenuTemplate } = require('../src/main/app-menu.cjs')
  const template = buildAppMenuTemplate({
    runtimePaths: {
      logsDir: 'D:/Logs'
    }
  })

  const labels = template.map((entry) => entry.label)

  assert.deepEqual(labels, ['File', 'Edit', 'View', 'Window', 'Help'])
  assert.equal(
    template.some((entry) => entry.label === 'Help' && entry.submenu.some((item) => item.label === '打开日志目录')),
    true
  )
  console.log('app-menu.test.cjs: PASS')
}

try {
  run()
} catch (error) {
  console.error('app-menu.test.cjs: FAIL')
  console.error(error)
  process.exitCode = 1
}
