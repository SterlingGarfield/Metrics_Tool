const assert = require('node:assert/strict')
const fs = require('node:fs/promises')
const os = require('node:os')
const path = require('node:path')

async function run() {
  const { createAnalyzerState } = require('../src/main/analyzer.cjs')
  const tempRoot = await fs.mkdtemp(path.join(os.tmpdir(), 'metrics-java-'))
  const bundledJava = path.join(tempRoot, 'app-files', 'jre', 'bin', 'java.exe')

  try {
    await fs.mkdir(path.dirname(bundledJava), { recursive: true })
    await fs.writeFile(bundledJava, '')

    const state = createAnalyzerState({
      runtimePaths: {
        appFilesDir: path.join(tempRoot, 'app-files')
      }
    })

    assert.equal(state.javaBin, bundledJava)
    console.log('java-runtime.test.cjs: PASS')
  } finally {
    await fs.rm(tempRoot, { recursive: true, force: true })
  }
}

run().catch((error) => {
  console.error('java-runtime.test.cjs: FAIL')
  console.error(error)
  process.exitCode = 1
})
