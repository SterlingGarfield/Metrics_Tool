const assert = require('node:assert/strict')
const fs = require('node:fs/promises')
const os = require('node:os')
const path = require('node:path')

async function run() {
  const { createAnalyzerState } = require('../src/main/analyzer.cjs')
  const tempRoot = await fs.mkdtemp(path.join(os.tmpdir(), 'metrics-ocr-'))
  const appFilesDir = path.join(tempRoot, 'app-files')
  const pythonBin = path.join(appFilesDir, 'ocr-runtime', 'python.exe')
  const modelHome = path.join(appFilesDir, 'ocr-models')

  try {
    await fs.mkdir(path.dirname(pythonBin), { recursive: true })
    await fs.writeFile(pythonBin, '')
    await fs.mkdir(modelHome, { recursive: true })

    const state = createAnalyzerState({
      runtimePaths: { appFilesDir }
    })

    assert.equal(state.ocrPythonBin, pythonBin)
    assert.equal(state.ocrModelHome, modelHome)
    console.log('ocr-runtime.test.cjs: PASS')
  } finally {
    await fs.rm(tempRoot, { recursive: true, force: true })
  }
}

run().catch((error) => {
  console.error('ocr-runtime.test.cjs: FAIL')
  console.error(error)
  process.exitCode = 1
})
