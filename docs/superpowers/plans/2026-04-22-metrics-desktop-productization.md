# Metrics Desktop Productization Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Turn the new Electron desktop foundation into a productization slice with runtime initialization, logs, failure-aware startup flow, and a Windows installer baseline.

**Architecture:** Keep the strict desktop path as `renderer -> preload -> Electron main -> Java CLI`, and add a desktop runtime layer around it. Electron main becomes responsible for creating writable app-data directories, recording logs, surfacing splash boot state, and staging offline build artifacts for Windows packaging.

**Tech Stack:** Electron, Node.js CommonJS, PowerShell, electron-builder, Spring Boot jar packaging

---

### Task 1: Runtime Paths And Logging

**Files:**
- Create: `metrics-desktop/src/main/runtime-paths.cjs`
- Create: `metrics-desktop/src/main/logger.cjs`
- Create: `metrics-desktop/test/runtime-paths.test.cjs`
- Modify: `metrics-desktop/src/main/index.cjs`
- Modify: `metrics-desktop/src/main/analyzer.cjs`

- [ ] **Step 1: Write failing Node tests for first-run directory creation and runtime manifest persistence**

```javascript
test('ensureDesktopDirectories creates writable runtime folders and manifest', async () => {
  const tempRoot = await fs.mkdtemp(path.join(os.tmpdir(), 'metrics-runtime-'))
  const paths = await ensureDesktopDirectories({ appDataRoot: tempRoot })

  assert.equal(await exists(paths.logsDir), true)
  assert.equal(await exists(paths.runtimeDir), true)
  assert.equal(await exists(paths.manifestFile), true)
})
```

- [ ] **Step 2: Run test to verify it fails**

Run: `node --test test/runtime-paths.test.cjs`
Expected: FAIL with module-not-found or function-not-defined for `ensureDesktopDirectories`

- [ ] **Step 3: Implement runtime path resolution and bootstrap logging**

```javascript
async function ensureDesktopDirectories(options = {}) {
  const appDataRoot = options.appDataRoot || path.join(os.homedir(), 'AppData', 'Roaming', 'MetricsToolDesktop')
  const runtimeDir = path.join(appDataRoot, 'runtime')
  const logsDir = path.join(appDataRoot, 'logs')
  const manifestFile = path.join(runtimeDir, 'desktop-manifest.json')
  await fs.mkdir(runtimeDir, { recursive: true })
  await fs.mkdir(logsDir, { recursive: true })
  await fs.writeFile(manifestFile, JSON.stringify({ initializedAt: new Date().toISOString() }, null, 2))
  return { appDataRoot, runtimeDir, logsDir, manifestFile }
}
```

- [ ] **Step 4: Re-run test to verify it passes**

Run: `node --test test/runtime-paths.test.cjs`
Expected: PASS

### Task 2: Splash Boot State And Failure Recovery

**Files:**
- Create: `metrics-desktop/src/main/boot-state.cjs`
- Create: `metrics-desktop/src/preload/splash.cjs`
- Create: `metrics-desktop/test/boot-state.test.cjs`
- Modify: `metrics-desktop/src/main/index.cjs`
- Modify: `metrics-desktop/src/main/ipc.cjs`
- Modify: `metrics-desktop/src/splash/index.html`

- [ ] **Step 1: Write failing Node tests for boot-state transitions and failure payload delivery**

```javascript
test('createBootState tracks stage transitions and terminal failures', () => {
  const bootState = createBootState()
  bootState.update('init', 'Preparing runtime')
  bootState.fail('missing_jar', 'Desktop analyzer jar not found')

  assert.equal(bootState.snapshot().status, 'failed')
  assert.equal(bootState.snapshot().code, 'missing_jar')
})
```

- [ ] **Step 2: Run test to verify it fails**

Run: `node --test test/boot-state.test.cjs`
Expected: FAIL with module-not-found for `createBootState`

- [ ] **Step 3: Implement splash-state updates, retry hook, and open-log-directory IPC**

```javascript
ipcMain.handle('desktop:retryBoot', async () => {
  await bootDesktopShell({ forceRetry: true })
})

ipcMain.handle('desktop:openLogs', async () => {
  await shell.openPath(runtimePaths.logsDir)
})
```

- [ ] **Step 4: Re-run boot-state test and syntax checks**

Run: `node --test test/boot-state.test.cjs`
Expected: PASS

Run: `node --check src/main/index.cjs`
Expected: exit 0

### Task 3: Windows Packaging Baseline

**Files:**
- Create: `metrics-desktop/electron-builder.json`
- Create: `metrics-desktop/scripts/stage-runtime.ps1`
- Create: `metrics-desktop/scripts/build-installer.ps1`
- Create: `metrics-desktop/test/installer-config.test.cjs`
- Modify: `metrics-desktop/package.json`
- Modify: `.gitignore`

- [ ] **Step 1: Write failing Node test for installer configuration invariants**

```javascript
test('electron-builder config enables assisted install and packaged resources', async () => {
  const config = JSON.parse(await fs.readFile('electron-builder.json', 'utf8'))
  assert.equal(config.win.target[0], 'nsis')
  assert.equal(config.nsis.oneClick, false)
  assert.equal(config.nsis.allowToChangeInstallationDirectory, true)
  assert.equal(Array.isArray(config.extraResources), true)
})
```

- [ ] **Step 2: Run test to verify it fails**

Run: `node --test test/installer-config.test.cjs`
Expected: FAIL because `electron-builder.json` does not exist yet

- [ ] **Step 3: Implement offline staging script and Windows installer baseline**

```powershell
Push-Location $root\metrics-frontend
npm run build
Pop-Location

Push-Location $root\metrics-backend
mvn clean package
Pop-Location

Copy-Item $frontendDist\* $desktopApp\frontend -Recurse -Force
Copy-Item $backendJar $desktopApp\backend\metrics-backend-0.0.1-SNAPSHOT.jar -Force
```

- [ ] **Step 4: Re-run packaging config test**

Run: `node --test test/installer-config.test.cjs`
Expected: PASS
