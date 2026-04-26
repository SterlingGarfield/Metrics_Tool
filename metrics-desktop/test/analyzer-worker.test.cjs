const assert = require('node:assert/strict')
const { EventEmitter } = require('node:events')
const fs = require('node:fs/promises')
const os = require('node:os')
const path = require('node:path')
const { PassThrough, Writable } = require('node:stream')

function createFakeWorkerProcess(options = {}) {
  const child = new EventEmitter()
  child.stdout = new PassThrough()
  child.stderr = new PassThrough()
  child.stdinWrites = []
  child._closed = false
  child.kill = (signal = 'SIGTERM') => {
    if (child._closed) {
      return true
    }

    child._closed = true
    process.nextTick(() => {
      child.emit('exit', 0, signal)
      child.emit('close', 0, signal)
    })
    return true
  }
  child.stdin = new Writable({
    write(chunk, encoding, callback) {
      const rawText = chunk.toString()
      child.stdinWrites.push(rawText)
      const lines = rawText.split('\n').filter(Boolean)
      for (const line of lines) {
        const request = JSON.parse(line)
        options.onRequest?.(request, child)
      }
      callback()
    }
  })

  process.nextTick(() => {
    if (options.ready !== false) {
      child.stdout.write(`${JSON.stringify({ type: 'ready' })}\n`)
    }
  })

  return child
}

function createFakeAnalyzerProcess(options = {}) {
  const child = new EventEmitter()
  child.stdout = new PassThrough()
  child.stderr = new PassThrough()
  child._closed = false
  child.kill = () => true

  let rawRequest = ''
  child.stdin = new Writable({
    write(chunk, encoding, callback) {
      rawRequest += chunk.toString()
      callback()
    },
    final(callback) {
      const request = rawRequest.trim() ? JSON.parse(rawRequest) : {}
      options.onRequest?.(request, child)
      process.nextTick(() => {
        if (!child._closed) {
          child._closed = true
          child.emit('close', 0)
        }
      })
      callback()
    }
  })

  return child
}

async function createAnalyzerStateWithWorker(overrides = {}) {
  const { createAnalyzerState } = require('../src/main/analyzer.cjs')
  const tempRoot = await fs.mkdtemp(path.join(os.tmpdir(), 'metrics-ocr-worker-'))
  const appFilesDir = path.join(tempRoot, 'app-files')
  const pythonBin = path.join(appFilesDir, 'ocr-runtime', 'python.exe')
  const modelHome = path.join(appFilesDir, 'ocr-models')

  await fs.mkdir(path.dirname(pythonBin), { recursive: true })
  await fs.writeFile(pythonBin, '')
  await fs.mkdir(modelHome, { recursive: true })

  const events = []
  const state = createAnalyzerState({
    runtimePaths: { appFilesDir },
    spawn: overrides.spawn,
    ocrRequestTimeoutMs: overrides.ocrRequestTimeoutMs,
    logger: {
      info(message, details) {
        events.push({ level: 'info', message, details })
      },
      warn(message, details) {
        events.push({ level: 'warn', message, details })
      },
      error(message, details) {
        events.push({ level: 'error', message, details })
      },
      attachStream() {}
    }
  })

  return {
    tempRoot,
    state,
    events
  }
}

async function exists(targetPath) {
  try {
    await fs.access(targetPath)
    return true
  } catch {
    return false
  }
}

async function run() {
  const analyzer = require('../src/main/analyzer.cjs')

  {
    const spawnedChildren = []
    const { tempRoot, state } = await createAnalyzerStateWithWorker({
      spawn() {
        const child = createFakeWorkerProcess({
          onRequest(request, processHandle) {
            processHandle.stdout.write(`${JSON.stringify({
              requestId: request.requestId,
              available: true,
              recognizedText: ['User'],
              averageConfidence: 0.91,
              warnings: []
            })}\n`)
          }
        })
        spawnedChildren.push(child)
        return child
      }
    })

    try {
      const firstScan = await analyzer.scanDesignImageWithWorker(state, {
        imageBytes: [1, 2, 3],
        imageName: 'design.png',
        imageType: 'image/png'
      })
      const secondScan = await analyzer.scanDesignImageWithWorker(state, {
        imageBytes: [4, 5, 6],
        imageName: 'design.png',
        imageType: 'image/png'
      })

      assert.equal(spawnedChildren.length, 1)
      assert.equal(firstScan.available, true)
      assert.equal(secondScan.available, true)
    } finally {
      await analyzer.disposeAnalyzerState(state)
      await fs.rm(tempRoot, { recursive: true, force: true })
    }
  }

  {
    const spawnedChildren = []
    const { tempRoot, state } = await createAnalyzerStateWithWorker({
      spawn() {
        const child = createFakeWorkerProcess({
          onRequest(request, processHandle) {
            processHandle.emit('exit', 1, null)
            processHandle.emit('close', 1, null)
          }
        })
        spawnedChildren.push(child)
        return child
      }
    })

    try {
      const failedScan = await analyzer.scanDesignImageWithWorker(state, {
        imageBytes: [1, 2, 3],
        imageName: 'broken.png',
        imageType: 'image/png'
      })

      assert.equal(failedScan.available, false)

      spawnedChildren[0].kill = () => true
      state.spawn = () => createFakeWorkerProcess({
        onRequest(request, processHandle) {
          processHandle.stdout.write(`${JSON.stringify({
            requestId: request.requestId,
            available: true,
            recognizedText: ['Admin'],
            averageConfidence: 0.88,
            warnings: []
          })}\n`)
        }
      })

      const recoveredScan = await analyzer.scanDesignImageWithWorker(state, {
        imageBytes: [1, 2, 3],
        imageName: 'recovered.png',
        imageType: 'image/png'
      })

      assert.equal(recoveredScan.available, true)
      assert.equal(spawnedChildren.length, 1)
    } finally {
      await analyzer.disposeAnalyzerState(state)
      await fs.rm(tempRoot, { recursive: true, force: true })
    }
  }

  {
    let capturedImagePath = null
    const { tempRoot, state } = await createAnalyzerStateWithWorker({
      spawn() {
        return createFakeWorkerProcess({
          onRequest(request, processHandle) {
            capturedImagePath = request.imagePath
            processHandle.stdout.write(`${JSON.stringify({
              requestId: request.requestId,
              available: true,
              recognizedText: ['Flow'],
              averageConfidence: 0.8,
              warnings: []
            })}\n`)
          }
        })
      }
    })

    try {
      await analyzer.scanDesignImageWithWorker(state, {
        imageBytes: [9, 8, 7],
        imageName: 'cleanup.png',
        imageType: 'image/png'
      })

      assert.equal(await exists(capturedImagePath), false)
    } finally {
      await analyzer.disposeAnalyzerState(state)
      await fs.rm(tempRoot, { recursive: true, force: true })
    }
  }

  {
    const { tempRoot, state } = await createAnalyzerStateWithWorker({
      ocrRequestTimeoutMs: 25,
      spawn() {
        return createFakeWorkerProcess({
          onRequest() {
            // Intentionally never responds.
          }
        })
      }
    })

    try {
      const timedOutScan = await analyzer.scanDesignImageWithWorker(state, {
        imageBytes: [1, 2, 3],
        imageName: 'timeout.png',
        imageType: 'image/png'
      })

      assert.equal(timedOutScan.available, false)
      assert.equal(timedOutScan.warnings.some((warning) => warning.includes('超时')), true)
    } finally {
      await analyzer.disposeAnalyzerState(state)
      await fs.rm(tempRoot, { recursive: true, force: true })
    }
  }

  {
    let capturedAnalyzerRequest = null
    const { tempRoot, state } = await createAnalyzerStateWithWorker({
      spawn(command, args) {
        if (Array.isArray(args) && args.includes('-u')) {
          return createFakeWorkerProcess({
            onRequest(request, processHandle) {
              processHandle.stdout.write(`${JSON.stringify({
                requestId: request.requestId,
                available: true,
                recognizedText: ['Patient', 'Doctor', '负责'],
                averageConfidence: 0.92,
                warnings: [],
                tokens: [{
                  text: '负责',
                  confidence: 0.95,
                  bbox: [10, 20, 110, 120],
                  polygon: [[10, 20], [110, 20], [110, 120], [10, 120]]
                }],
                imageWidth: 3328,
                imageHeight: 3459
              })}\n`)
            }
          })
        }

        if (Array.isArray(args) && args.includes('-jar')) {
          return createFakeAnalyzerProcess({
            onRequest(request, processHandle) {
              capturedAnalyzerRequest = request
              processHandle.stdout.write(JSON.stringify({
                status: 'ok',
                payload: {
                  available: true,
                  diagramType: 'class',
                  recognizedText: ['Patient', 'Doctor'],
                  suggestedMetrics: {
                    classCount: 2,
                    relationshipCount: 1
                  },
                  confidence: 0.9,
                  warnings: []
                }
              }))
            }
          })
        }

        throw new Error(`Unexpected spawn call: ${command} ${JSON.stringify(args)}`)
      }
    })

    state.analyzerReady = true
    state.analyzerJarPath = path.join(tempRoot, 'backend.jar')
    await fs.writeFile(state.analyzerJarPath, '')

    try {
      const suggestion = await analyzer.suggestDesignMetrics(state, {
        diagramType: 'class',
        imageBytes: [1, 2, 3],
        imageName: 'forwarding.png',
        imageType: 'image/png'
      })

      assert.equal(suggestion.available, true)
      assert.ok(capturedAnalyzerRequest)
      assert.equal(Array.isArray(capturedAnalyzerRequest.ocrTokens), true)
      assert.equal(capturedAnalyzerRequest.ocrTokens.length, 1)
      assert.deepEqual(capturedAnalyzerRequest.ocrTokens[0].bbox, [10, 20, 110, 120])
      assert.equal(capturedAnalyzerRequest.ocrImageWidth, 3328)
      assert.equal(capturedAnalyzerRequest.ocrImageHeight, 3459)
    } finally {
      await analyzer.disposeAnalyzerState(state)
      await fs.rm(tempRoot, { recursive: true, force: true })
    }
  }

  console.log('analyzer-worker.test.cjs: PASS')
}

run().catch((error) => {
  console.error('analyzer-worker.test.cjs: FAIL')
  console.error(error)
  process.exitCode = 1
})
