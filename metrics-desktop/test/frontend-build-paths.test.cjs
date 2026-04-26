const assert = require('node:assert/strict')
const fs = require('node:fs')
const path = require('node:path')

function run() {
  const indexHtml = fs.readFileSync(path.resolve(__dirname, '../../metrics-frontend/dist/index.html'), 'utf8')

  assert.equal(indexHtml.includes('src="/assets/'), false)
  assert.equal(indexHtml.includes('href="/assets/'), false)
  console.log('frontend-build-paths.test.cjs: PASS')
}

try {
  run()
} catch (error) {
  console.error('frontend-build-paths.test.cjs: FAIL')
  console.error(error)
  process.exitCode = 1
}
