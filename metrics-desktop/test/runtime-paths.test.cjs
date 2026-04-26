const assert = require('node:assert/strict')
const fs = require('node:fs/promises')
const os = require('node:os')
const path = require('node:path')

function exists(targetPath) {
  return fs.access(targetPath).then(() => true).catch(() => false)
}

async function run() {
  const { ensureDesktopDirectories } = require('../src/main/runtime-paths.cjs')
  const tempRoot = await fs.mkdtemp(path.join(os.tmpdir(), 'metrics-runtime-'))

  try {
    const runtimePaths = await ensureDesktopDirectories({ appDataRoot: tempRoot })
    const manifest = JSON.parse(await fs.readFile(runtimePaths.manifestFile, 'utf8'))

    assert.equal(await exists(runtimePaths.logsDir), true)
    assert.equal(await exists(runtimePaths.runtimeDir), true)
    assert.equal(await exists(runtimePaths.appFilesDir), true)
    assert.equal(await exists(runtimePaths.manifestFile), true)
    assert.equal(manifest.appFilesDir, runtimePaths.appFilesDir)
    console.log('runtime-paths.test.cjs: PASS')
  } finally {
    await fs.rm(tempRoot, { recursive: true, force: true })
  }
}

run().catch((error) => {
  console.error('runtime-paths.test.cjs: FAIL')
  console.error(error)
  process.exitCode = 1
})
