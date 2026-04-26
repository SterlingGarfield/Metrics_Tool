const assert = require('node:assert/strict')
const fs = require('node:fs')
const path = require('node:path')

function run() {
  const scriptPath = path.resolve(__dirname, '../scripts/stage-runtime.ps1')
  const script = fs.readFileSync(scriptPath, 'utf8')

  assert.equal(script.includes("Remove-Item -LiteralPath $frontendOut -Recurse -Force"), true)
  assert.equal(script.includes("Remove-Item -LiteralPath $backendOut -Recurse -Force"), true)
  assert.equal(script.includes("Remove-Item -LiteralPath $jreOut -Recurse -Force"), true)
  assert.equal(script.includes("Remove-Item -LiteralPath $ocrRuntimeOut -Recurse -Force"), true)
  assert.equal(script.includes("Remove-Item -LiteralPath $ocrModelsOut -Recurse -Force"), true)
  assert.equal(script.includes("target\\classes\\static"), true)
  assert.equal(script.includes("Copy-Item -Path (Join-Path $ocrRuntimeSourceDir '*') -Destination $ocrRuntimeOut -Recurse -Force"), true)
  assert.equal(script.includes("Copy-Item -Path (Join-Path $ocrModelSourceDir '*') -Destination $ocrModelsOut -Recurse -Force"), true)
  console.log('stage-runtime-cleanup.test.cjs: PASS')
}

try {
  run()
} catch (error) {
  console.error('stage-runtime-cleanup.test.cjs: FAIL')
  console.error(error)
  process.exitCode = 1
}
