const assert = require('node:assert/strict')
const fs = require('node:fs')
const path = require('node:path')

function run() {
  const root = path.resolve(__dirname, '../..')
  const scriptPath = path.resolve(__dirname, '../scripts/generate-brand-icons.ps1')
  const script = fs.readFileSync(scriptPath, 'utf8')
  const svgPath = path.join(root, 'metrics-frontend', 'src', 'assets', 'branding', 'logo-desktop.svg')
  const pngPath = path.join(root, 'metrics-frontend', 'src', 'assets', 'branding', 'logo-desktop.png')

  assert.equal(script.includes('logo-desktop.png'), true)
  assert.equal(fs.existsSync(svgPath), true)
  assert.equal(fs.existsSync(pngPath), true)
  assert.equal(fs.statSync(svgPath).size > 0, true)
  assert.equal(fs.statSync(pngPath).size > 0, true)
  console.log('brand-icon-source.test.cjs: PASS')
}

try {
  run()
} catch (error) {
  console.error('brand-icon-source.test.cjs: FAIL')
  console.error(error)
  process.exitCode = 1
}
