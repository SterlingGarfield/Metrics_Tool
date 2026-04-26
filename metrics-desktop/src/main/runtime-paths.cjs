const fs = require('fs/promises')
const os = require('os')
const path = require('path')

function resolveAppDataRoot(options = {}) {
  if (options.appDataRoot) {
    return options.appDataRoot
  }

  if (process.env.METRICS_DESKTOP_APPDATA) {
    return process.env.METRICS_DESKTOP_APPDATA
  }

  return path.join(os.homedir(), 'AppData', 'Roaming', 'Metrics Tool Desktop')
}

function buildRuntimePaths(options = {}) {
  const appDataRoot = resolveAppDataRoot(options)
  const runtimeDir = path.join(appDataRoot, 'runtime')
  const logsDir = path.join(appDataRoot, 'logs')
  const appFilesDir = options.appFilesDir || process.env.METRICS_APP_FILES_DIR || path.join(appDataRoot, 'app-files')
  const manifestFile = path.join(runtimeDir, 'desktop-manifest.json')

  return {
    appDataRoot,
    runtimeDir,
    logsDir,
    appFilesDir,
    manifestFile
  }
}

async function ensureDesktopDirectories(options = {}) {
  const runtimePaths = buildRuntimePaths(options)

  await fs.mkdir(runtimePaths.runtimeDir, { recursive: true })
  await fs.mkdir(runtimePaths.logsDir, { recursive: true })
  await fs.mkdir(runtimePaths.appFilesDir, { recursive: true })

  const manifest = {
    appDataRoot: runtimePaths.appDataRoot,
    runtimeDir: runtimePaths.runtimeDir,
    logsDir: runtimePaths.logsDir,
    appFilesDir: runtimePaths.appFilesDir,
    initializedAt: new Date().toISOString()
  }

  await fs.writeFile(runtimePaths.manifestFile, `${JSON.stringify(manifest, null, 2)}\n`, 'utf8')
  return runtimePaths
}

module.exports = {
  buildRuntimePaths,
  ensureDesktopDirectories
}
