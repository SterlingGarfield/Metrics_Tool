const assert = require('node:assert/strict')
const fs = require('node:fs')
const path = require('node:path')

function run() {
  const desktopRoot = path.resolve(__dirname, '..')
  const configPath = path.resolve(__dirname, '../electron-builder.json')
  const config = JSON.parse(fs.readFileSync(configPath, 'utf8'))
  const iconPath = path.join(desktopRoot, 'installer', 'metrics-tool.ico')

  assert.equal(config.appId, 'com.metrics.desktop')
  assert.equal(config.win.icon, 'installer/metrics-tool.ico')
  assert.equal(config.win.target[0], 'nsis')
  assert.equal(config.nsis.oneClick, false)
  assert.equal(config.nsis.allowToChangeInstallationDirectory, true)
  assert.equal(config.nsis.installerIcon, 'installer/metrics-tool.ico')
  assert.equal(config.nsis.uninstallerIcon, 'installer/metrics-tool.ico')
  assert.equal(config.nsis.installerHeaderIcon, 'installer/metrics-tool.ico')
  assert.equal(Array.isArray(config.extraResources), true)
  assert.equal(config.extraResources.length >= 5, true)
  assert.equal(config.extraResources.some((entry) => entry.to === 'payload/jre'), true)
  assert.equal(config.extraResources.some((entry) => entry.to === 'payload/ocr-runtime'), true)
  assert.equal(config.extraResources.some((entry) => entry.to === 'payload/ocr-models'), true)
  assert.equal(typeof config.nsis.include, 'string')
  assert.equal(fs.existsSync(iconPath), true)
  assert.equal(fs.statSync(iconPath).size > 0, true)
  console.log('installer-config.test.cjs: PASS')
}

try {
  run()
} catch (error) {
  console.error('installer-config.test.cjs: FAIL')
  console.error(error)
  process.exitCode = 1
}
