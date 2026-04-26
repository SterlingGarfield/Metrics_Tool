const fs = require('fs')
const fsPromises = require('fs/promises')
const os = require('os')
const path = require('path')
const childProcess = require('child_process')
const readline = require('readline')

const OCR_WORKER_STARTUP_TIMEOUT_MS = 45000
const OCR_REQUEST_TIMEOUT_MS = 90000
const OCR_WORKER_SCRIPT = String.raw`
import json
import os
import sys

from paddleocr import PaddleOCR
import cv2

model_home = sys.argv[1] if len(sys.argv) > 1 else ''
if model_home:
    os.environ['PADDLEOCR_HOME'] = model_home

ocr = PaddleOCR(use_angle_cls=True, lang='ch', show_log=False)
print(json.dumps({'type': 'ready'}, ensure_ascii=False), flush=True)

for raw_line in sys.stdin:
    raw_line = raw_line.strip()
    if not raw_line:
        continue

    request_id = None
    try:
        payload = json.loads(raw_line)
        request_id = payload.get('requestId')
        image_path = payload.get('imagePath')
        if not image_path:
            raise ValueError('imagePath is required')

        image_width = None
        image_height = None
        image = cv2.imread(image_path)
        if image is not None:
            image_height, image_width = image.shape[:2]

        result = ocr.ocr(image_path, cls=True)
        lines = []
        scores = []
        tokens = []
        for page in result or []:
            for item in page or []:
                if not item or len(item) < 2:
                    continue
                text = str(item[1][0]).strip() if item[1] and len(item[1]) > 0 else ''
                score = float(item[1][1]) if item[1] and len(item[1]) > 1 else 0.0
                if text:
                    polygon = []
                    for point in item[0] or []:
                        if not point or len(point) < 2:
                            continue
                        polygon.append([float(point[0]), float(point[1])])
                    if polygon:
                        xs = [point[0] for point in polygon]
                        ys = [point[1] for point in polygon]
                        bbox = [min(xs), min(ys), max(xs), max(ys)]
                    else:
                        bbox = []
                    lines.append(text)
                    scores.append(score)
                    tokens.append({
                        'text': text,
                        'confidence': score,
                        'bbox': bbox,
                        'polygon': polygon
                    })

        average_confidence = sum(scores) / len(scores) if scores else 0.0
        print(json.dumps({
            'requestId': request_id,
            'available': True,
            'recognizedText': lines,
            'averageConfidence': average_confidence,
            'warnings': [],
            'tokens': tokens,
            'imageWidth': image_width,
            'imageHeight': image_height
        }, ensure_ascii=False), flush=True)
    except Exception as exc:
        print(json.dumps({
            'requestId': request_id,
            'available': False,
            'recognizedText': [],
            'averageConfidence': 0.0,
            'warnings': [str(exc)],
            'tokens': [],
            'imageWidth': None,
            'imageHeight': None
        }, ensure_ascii=False), flush=True)
`

function resolveAnalyzerJarPath(options = {}) {
  if (process.env.METRICS_ANALYZER_JAR) {
    return process.env.METRICS_ANALYZER_JAR
  }

  const appFilesPath = options.runtimePaths?.appFilesDir
    ? path.join(options.runtimePaths.appFilesDir, 'backend', 'metrics-backend-0.0.1-SNAPSHOT.jar')
    : null
  if (appFilesPath && fs.existsSync(appFilesPath)) {
    return appFilesPath
  }

  const stagedPackagedPath = path.join(process.resourcesPath || '', 'payload', 'backend', 'metrics-backend-0.0.1-SNAPSHOT.jar')
  if (stagedPackagedPath && fs.existsSync(stagedPackagedPath)) {
    return stagedPackagedPath
  }

  const packagedPath = path.join(process.resourcesPath || '', 'backend', 'metrics-backend-0.0.1-SNAPSHOT.jar')
  if (packagedPath && fs.existsSync(packagedPath)) {
    return packagedPath
  }

  return path.resolve(__dirname, '../../../metrics-backend/target/metrics-backend-0.0.1-SNAPSHOT.jar')
}

function resolveJavaBin(options = {}) {
  if (process.env.METRICS_JAVA_BIN) {
    return process.env.METRICS_JAVA_BIN
  }

  const bundledJavaPath = options.runtimePaths?.appFilesDir
    ? path.join(options.runtimePaths.appFilesDir, 'jre', 'bin', 'java.exe')
    : null
  if (bundledJavaPath && fs.existsSync(bundledJavaPath)) {
    return bundledJavaPath
  }

  const packagedJavaPath = path.join(process.resourcesPath || '', 'payload', 'jre', 'bin', 'java.exe')
  if (packagedJavaPath && fs.existsSync(packagedJavaPath)) {
    return packagedJavaPath
  }

  const javaHomePath = process.env.METRICS_JAVA_HOME || process.env.JAVA_HOME
  if (javaHomePath) {
    const javaHomeBin = path.join(javaHomePath, 'bin', 'java.exe')
    if (fs.existsSync(javaHomeBin)) {
      return javaHomeBin
    }
  }

  return 'java'
}

function resolveOcrPythonBin(options = {}) {
  if (process.env.METRICS_OCR_PYTHON_BIN) {
    return process.env.METRICS_OCR_PYTHON_BIN
  }

  const bundledPythonPath = options.runtimePaths?.appFilesDir
    ? path.join(options.runtimePaths.appFilesDir, 'ocr-runtime', 'python.exe')
    : null
  if (bundledPythonPath && fs.existsSync(bundledPythonPath)) {
    return bundledPythonPath
  }

  const packagedPythonPath = path.join(process.resourcesPath || '', 'payload', 'ocr-runtime', 'python.exe')
  if (packagedPythonPath && fs.existsSync(packagedPythonPath)) {
    return packagedPythonPath
  }

  const localPythonPath = path.resolve(__dirname, '../../../.ocr311/python.exe')
  if (fs.existsSync(localPythonPath)) {
    return localPythonPath
  }

  return null
}

function resolveOcrModelHome(options = {}) {
  if (process.env.METRICS_OCR_MODEL_HOME) {
    return process.env.METRICS_OCR_MODEL_HOME
  }

  const bundledModelPath = options.runtimePaths?.appFilesDir
    ? path.join(options.runtimePaths.appFilesDir, 'ocr-models')
    : null
  if (bundledModelPath && fs.existsSync(bundledModelPath)) {
    return bundledModelPath
  }

  const packagedModelPath = path.join(process.resourcesPath || '', 'payload', 'ocr-models')
  if (packagedModelPath && fs.existsSync(packagedModelPath)) {
    return packagedModelPath
  }

  const localModelPath = path.resolve(__dirname, '../../../models/paddleocr')
  if (fs.existsSync(localModelPath)) {
    return localModelPath
  }

  return null
}

function createAnalyzerState(options = {}) {
  const analyzerJarPath = resolveAnalyzerJarPath(options)
  const logger = options.logger
  const ocrPythonBin = resolveOcrPythonBin(options)
  const ocrModelHome = resolveOcrModelHome(options)

  const state = {
    analyzerJarPath,
    analyzerReady: fs.existsSync(analyzerJarPath),
    javaBin: resolveJavaBin(options),
    ocrPythonBin,
    ocrModelHome,
    ocrReady: Boolean(ocrPythonBin && ocrModelHome),
    logger,
    runtimePaths: options.runtimePaths || null,
    spawn: options.spawn || childProcess.spawn,
    ocrRequestTimeoutMs: options.ocrRequestTimeoutMs || OCR_REQUEST_TIMEOUT_MS,
    ocrWorkerStartupTimeoutMs: options.ocrWorkerStartupTimeoutMs || OCR_WORKER_STARTUP_TIMEOUT_MS,
    ocrWorkerProcess: null,
    ocrWorkerStdoutInterface: null,
    ocrWorkerReady: false,
    ocrWorkerStartupPromise: null,
    ocrWorkerStartupResolve: null,
    ocrWorkerStartupReject: null,
    ocrWorkerStartupTimer: null,
    ocrWorkerPendingRequests: new Map(),
    ocrWorkerQueue: Promise.resolve(),
    ocrRequestCounter: 0
  }

  logger?.info('Resolved analyzer runtime', {
    analyzerJarPath: state.analyzerJarPath,
    analyzerReady: state.analyzerReady,
    javaBin: state.javaBin,
    ocrPythonBin: state.ocrPythonBin,
    ocrModelHome: state.ocrModelHome,
    ocrReady: state.ocrReady
  })

  return state
}

function ensureAnalyzerReady(state) {
  if (!state.analyzerReady) {
    throw new Error(`Desktop analyzer jar not found: ${state.analyzerJarPath}`)
  }
}

function clearOcrWorkerStartup(state) {
  if (state.ocrWorkerStartupTimer) {
    clearTimeout(state.ocrWorkerStartupTimer)
    state.ocrWorkerStartupTimer = null
  }
}

function normalizeWarnings(warnings) {
  if (!Array.isArray(warnings)) {
    return []
  }

  return Array.from(new Set(
    warnings
      .map((warning) => String(warning || '').trim())
      .filter(Boolean)
  ))
}

function createUnavailableOcrScanResult(warnings) {
  const normalizedWarnings = Array.isArray(warnings)
    ? normalizeWarnings(warnings)
    : normalizeWarnings([warnings || 'OCR 建议获取失败，请改用手工录入。'])

  return {
    available: false,
    recognizedText: [],
    averageConfidence: 0,
    warnings: normalizedWarnings,
    tokens: [],
    imageWidth: null,
    imageHeight: null
  }
}

function toFiniteNumber(value) {
  const numeric = Number(value)
  return Number.isFinite(numeric) ? numeric : null
}

function normalizeOcrPolygon(polygon) {
  if (!Array.isArray(polygon)) {
    return []
  }

  const normalized = []
  for (const point of polygon) {
    if (!Array.isArray(point) || point.length < 2) {
      continue
    }

    const x = toFiniteNumber(point[0])
    const y = toFiniteNumber(point[1])
    if (x == null || y == null) {
      continue
    }

    normalized.push([x, y])
  }

  return normalized
}

function normalizeOcrBBox(bbox, polygon) {
  if (Array.isArray(bbox) && bbox.length >= 4) {
    const minX = toFiniteNumber(bbox[0])
    const minY = toFiniteNumber(bbox[1])
    const maxX = toFiniteNumber(bbox[2])
    const maxY = toFiniteNumber(bbox[3])
    if ([minX, minY, maxX, maxY].every((value) => value != null)) {
      return [
        Math.min(minX, maxX),
        Math.min(minY, maxY),
        Math.max(minX, maxX),
        Math.max(minY, maxY)
      ]
    }
  }

  if (!Array.isArray(polygon) || polygon.length === 0) {
    return null
  }

  const xs = polygon.map((point) => point[0])
  const ys = polygon.map((point) => point[1])
  return [Math.min(...xs), Math.min(...ys), Math.max(...xs), Math.max(...ys)]
}

function normalizeOcrTokens(tokens) {
  if (!Array.isArray(tokens)) {
    return []
  }

  const normalized = []
  for (const token of tokens) {
    const text = String(token?.text || '').trim().replace(/\s+/g, ' ')
    if (!text) {
      continue
    }

    const polygon = normalizeOcrPolygon(token?.polygon)
    const bbox = normalizeOcrBBox(token?.bbox, polygon)
    if (!bbox) {
      continue
    }

    const confidenceNumber = toFiniteNumber(token?.confidence)
    const confidence = confidenceNumber == null ? 0 : Math.max(0, Math.min(confidenceNumber, 1))
    normalized.push({
      text,
      confidence,
      bbox,
      polygon
    })
  }

  return normalized
}

function normalizePositiveInteger(value) {
  const numeric = Number(value)
  if (!Number.isFinite(numeric) || numeric <= 0) {
    return null
  }

  return Math.floor(numeric)
}

function settleOcrWorkerStartup(state, mode, value) {
  if (mode === 'resolve' && typeof state.ocrWorkerStartupResolve === 'function') {
    state.ocrWorkerStartupResolve(value)
  }

  if (mode === 'reject' && typeof state.ocrWorkerStartupReject === 'function') {
    state.ocrWorkerStartupReject(value)
  }

  state.ocrWorkerStartupPromise = null
  state.ocrWorkerStartupResolve = null
  state.ocrWorkerStartupReject = null
  clearOcrWorkerStartup(state)
}

function rejectPendingOcrRequests(state, error) {
  for (const entry of state.ocrWorkerPendingRequests.values()) {
    clearTimeout(entry.timeoutId)
    entry.reject(error)
  }
  state.ocrWorkerPendingRequests.clear()
}

function releaseOcrWorkerResources(state) {
  if (state.ocrWorkerStdoutInterface) {
    state.ocrWorkerStdoutInterface.close()
  }

  state.ocrWorkerProcess = null
  state.ocrWorkerStdoutInterface = null
  state.ocrWorkerReady = false
  clearOcrWorkerStartup(state)
  state.ocrWorkerStartupPromise = null
  state.ocrWorkerStartupResolve = null
  state.ocrWorkerStartupReject = null
  state.ocrWorkerPendingRequests = new Map()
}

function tearDownOcrWorker(state, child, message, options = {}) {
  const activeChild = child || state.ocrWorkerProcess
  if (!activeChild || state.ocrWorkerProcess !== activeChild) {
    return
  }

  const error = options.error || new Error(message || 'OCR worker unavailable.')
  const logLevel = options.logLevel || (options.killProcess ? 'warn' : 'info')
  if (message) {
    state.logger?.[logLevel]?.('OCR worker lifecycle update', {
      message,
      ocrPythonBin: state.ocrPythonBin,
      ocrModelHome: state.ocrModelHome
    })
  }

  settleOcrWorkerStartup(state, 'reject', error)
  rejectPendingOcrRequests(state, error)
  releaseOcrWorkerResources(state)

  if (options.killProcess && typeof activeChild.kill === 'function') {
    try {
      activeChild.kill('SIGTERM')
    } catch (killError) {
      state.logger?.warn('Failed to terminate OCR worker', { message: killError.message })
    }
  }
}

function normalizeOcrWorkerResponse(payload) {
  const normalizedTokens = normalizeOcrTokens(payload?.tokens)
  return {
    available: Boolean(payload?.available),
    recognizedText: Array.isArray(payload?.recognizedText)
      ? payload.recognizedText.map((line) => String(line || '').trim()).filter(Boolean)
      : [],
    averageConfidence: Number(payload?.averageConfidence || 0),
    warnings: normalizeWarnings(payload?.warnings),
    tokens: normalizedTokens,
    imageWidth: normalizePositiveInteger(payload?.imageWidth),
    imageHeight: normalizePositiveInteger(payload?.imageHeight)
  }
}

function handleOcrWorkerLine(state, child, line, startedAt) {
  if (state.ocrWorkerProcess !== child) {
    return
  }

  let payload
  try {
    payload = JSON.parse(line)
  } catch (error) {
    tearDownOcrWorker(state, child, `OCR worker returned invalid JSON: ${error.message}`, {
      error: new Error(`OCR worker returned invalid JSON: ${error.message}`),
      killProcess: true,
      logLevel: 'error'
    })
    return
  }

  if (payload.type === 'ready') {
    state.ocrWorkerReady = true
    settleOcrWorkerStartup(state, 'resolve')
    state.logger?.info('OCR worker ready', {
      startupMs: Date.now() - startedAt,
      ocrPythonBin: state.ocrPythonBin,
      ocrModelHome: state.ocrModelHome
    })
    return
  }

  const requestId = payload.requestId
  if (!requestId || !state.ocrWorkerPendingRequests.has(requestId)) {
    state.logger?.warn('Received OCR worker response for an unknown request', { requestId })
    return
  }

  const entry = state.ocrWorkerPendingRequests.get(requestId)
  clearTimeout(entry.timeoutId)
  state.ocrWorkerPendingRequests.delete(requestId)
  entry.resolve(normalizeOcrWorkerResponse(payload))
}

function ensureOcrWorkerReady(state) {
  if (!state.ocrReady) {
    return Promise.reject(new Error('本地 OCR 运行时或模型不可用。'))
  }

  if (state.ocrWorkerReady && state.ocrWorkerProcess) {
    return Promise.resolve()
  }

  if (state.ocrWorkerStartupPromise) {
    return state.ocrWorkerStartupPromise
  }

  const startedAt = Date.now()
  state.logger?.info('Starting OCR worker', {
    ocrPythonBin: state.ocrPythonBin,
    ocrModelHome: state.ocrModelHome
  })

  const child = state.spawn(state.ocrPythonBin, ['-u', '-c', OCR_WORKER_SCRIPT, state.ocrModelHome], {
    stdio: ['pipe', 'pipe', 'pipe'],
    env: {
      ...process.env,
      PYTHONIOENCODING: 'utf-8',
      PADDLEOCR_HOME: state.ocrModelHome
    }
  })

  state.ocrWorkerProcess = child
  state.ocrWorkerReady = false
  state.ocrWorkerPendingRequests = new Map()
  state.ocrWorkerStdoutInterface = readline.createInterface({ input: child.stdout })
  state.ocrWorkerStartupPromise = new Promise((resolve, reject) => {
    state.ocrWorkerStartupResolve = resolve
    state.ocrWorkerStartupReject = reject
  })
  state.ocrWorkerStartupTimer = setTimeout(() => {
    tearDownOcrWorker(state, child, 'OCR worker 启动超时。', {
      error: new Error('OCR worker 启动超时。'),
      killProcess: true
    })
  }, state.ocrWorkerStartupTimeoutMs)

  state.ocrWorkerStdoutInterface.on('line', (line) => {
    handleOcrWorkerLine(state, child, line, startedAt)
  })

  state.logger?.attachStream?.(child.stderr, 'ocr-worker.stderr', 'WARN')

  child.once('error', (error) => {
    tearDownOcrWorker(state, child, `OCR worker 启动失败：${error.message}`, {
      error: new Error(`OCR worker 启动失败：${error.message}`),
      logLevel: 'error'
    })
  })

  child.once('exit', (exitCode, signal) => {
    if (state.ocrWorkerProcess !== child) {
      return
    }

    const message = state.ocrWorkerReady
      ? `OCR worker 已退出，exitCode=${exitCode}, signal=${signal || 'none'}`
      : `OCR worker 启动失败，exitCode=${exitCode}, signal=${signal || 'none'}`
    tearDownOcrWorker(state, child, message, {
      error: new Error(message)
    })
  })

  return state.ocrWorkerStartupPromise
}

function resolveImageExtension(fileName, imageType) {
  if (fileName) {
    const extension = path.extname(fileName)
    if (extension) {
      return extension
    }
  }

  if (imageType === 'image/jpeg') {
    return '.jpg'
  }

  if (imageType === 'image/webp') {
    return '.webp'
  }

  return '.png'
}

async function prepareOcrImageFile(payload) {
  const tempDir = await fsPromises.mkdtemp(path.join(os.tmpdir(), 'metrics-design-'))
  const imagePath = path.join(tempDir, `diagram${resolveImageExtension(payload?.imageName, payload?.imageType)}`)
  await fsPromises.writeFile(imagePath, Buffer.from(payload.imageBytes || []))
  return {
    tempDir,
    imagePath
  }
}

async function cleanupOcrImageFile(tempDir) {
  if (!tempDir) {
    return
  }

  await fsPromises.rm(tempDir, { recursive: true, force: true })
}

function sendRequestToOcrWorker(state, imagePath) {
  const child = state.ocrWorkerProcess
  if (!child || !state.ocrWorkerReady) {
    return Promise.reject(new Error('OCR worker 尚未就绪。'))
  }

  const requestId = `ocr-${++state.ocrRequestCounter}`
  return new Promise((resolve, reject) => {
    const timeoutId = setTimeout(() => {
      state.ocrWorkerPendingRequests.delete(requestId)
      const timeoutError = new Error('OCR 识别超时，请稍后重试或改用手工录入。')
      reject(timeoutError)
      tearDownOcrWorker(state, child, timeoutError.message, {
        error: timeoutError,
        killProcess: true
      })
    }, state.ocrRequestTimeoutMs)

    state.ocrWorkerPendingRequests.set(requestId, {
      resolve,
      reject,
      timeoutId
    })

    child.stdin.write(`${JSON.stringify({ requestId, imagePath })}\n`, (error) => {
      if (!error) {
        return
      }

      clearTimeout(timeoutId)
      state.ocrWorkerPendingRequests.delete(requestId)
      reject(error)
      tearDownOcrWorker(state, child, `无法向 OCR worker 写入请求：${error.message}`, {
        error: new Error(`无法向 OCR worker 写入请求：${error.message}`),
        killProcess: true,
        logLevel: 'error'
      })
    })
  })
}

function queueOcrScan(state, task) {
  const nextTask = state.ocrWorkerQueue.then(task, task)
  state.ocrWorkerQueue = nextTask.catch(() => undefined)
  return nextTask
}

async function scanDesignImageWithWorker(state, payload) {
  return queueOcrScan(state, async () => {
    if (!payload?.imageBytes || payload.imageBytes.length === 0) {
      return createUnavailableOcrScanResult('未提供可识别的设计图图片。')
    }

    if (!state.ocrReady) {
      return createUnavailableOcrScanResult('本地 OCR 运行时或模型不可用。')
    }

    let tempDir = null
    const startedAt = Date.now()

    try {
      await ensureOcrWorkerReady(state)
      const tempImage = await prepareOcrImageFile(payload)
      tempDir = tempImage.tempDir
      const scanResult = await sendRequestToOcrWorker(state, tempImage.imagePath)
      state.logger?.info('OCR request completed', {
        imageName: payload.imageName || null,
        durationMs: Date.now() - startedAt,
        available: scanResult.available,
        recognizedTextCount: scanResult.recognizedText.length,
        tokenCount: scanResult.tokens.length
      })
      return scanResult
    } catch (error) {
      state.logger?.warn('OCR request failed', {
        imageName: payload?.imageName || null,
        message: error.message,
        durationMs: Date.now() - startedAt
      })
      return createUnavailableOcrScanResult(error.message || 'OCR 建议获取失败，请改用手工录入。')
    } finally {
      await cleanupOcrImageFile(tempDir)
    }
  })
}

function runAnalyzerRequest(state, request) {
  ensureAnalyzerReady(state)
  state.logger?.info('Starting analyzer request', { command: request.command })

  return new Promise((resolve, reject) => {
    const childEnv = {
      ...process.env
    }
    if (state.ocrPythonBin) {
      childEnv.METRICS_OCR_PYTHON_BIN = state.ocrPythonBin
    }
    if (state.ocrModelHome) {
      childEnv.METRICS_OCR_MODEL_HOME = state.ocrModelHome
    }

    const child = state.spawn(state.javaBin, ['-jar', state.analyzerJarPath, '--metrics.desktop.mode=cli'], {
      stdio: ['pipe', 'pipe', 'pipe'],
      env: childEnv
    })
    let stdout = ''
    let stderr = ''

    state.logger?.attachStream?.(child.stdout, 'analyzer.stdout')
    state.logger?.attachStream?.(child.stderr, 'analyzer.stderr', 'WARN')

    child.stdout.on('data', (chunk) => {
      stdout += chunk.toString()
    })

    child.stderr.on('data', (chunk) => {
      stderr += chunk.toString()
    })

    child.on('error', (error) => {
      state.logger?.error('Analyzer process failed to start', { message: error.message })
      reject(error)
    })
    child.on('close', (exitCode) => {
      if (!stdout.trim()) {
        const error = new Error(stderr.trim() || `Desktop analyzer exited with code ${exitCode}`)
        state.logger?.error('Analyzer process returned no stdout payload', { exitCode, stderr })
        reject(error)
        return
      }

      let response
      try {
        response = JSON.parse(stdout)
      } catch (error) {
        state.logger?.error('Analyzer returned invalid JSON', { exitCode, stdout })
        reject(new Error(`Desktop analyzer returned invalid JSON: ${error.message}`))
        return
      }

      if (exitCode !== 0 || response.status === 'error') {
        state.logger?.error('Analyzer request failed', { exitCode, error: response.error || stderr.trim() })
        reject(new Error(response.error || stderr.trim() || `Desktop analyzer exited with code ${exitCode}`))
        return
      }

      state.logger?.info('Analyzer request completed', { command: request.command, exitCode })
      resolve(response)
    })

    child.stdin.write(JSON.stringify(request))
    child.stdin.end()
  })
}

async function getAnalyzerStatus(state) {
  if (!state.analyzerReady) {
    return {
      status: 'UNAVAILABLE',
      mode: 'desktop',
      analyzer: 'jar-missing',
      analyzerJarPath: state.analyzerJarPath
    }
  }

  const response = await runAnalyzerRequest(state, { command: 'getAppStatus' })
  return {
    ...response.appStatus,
    status: response.appStatus?.status || 'UP',
    mode: 'desktop',
    analyzer: 'java-cli',
    analyzerJarPath: state.analyzerJarPath
  }
}

async function analyzeText(state, payload) {
  const response = await runAnalyzerRequest(state, {
    command: 'analyzeText',
    fileName: payload.fileName,
    sourceCode: payload.sourceCode
  })

  return response.analysis
}

async function analyzeSources(state, sources) {
  const response = await runAnalyzerRequest(state, {
    command: 'analyzeSources',
    sources
  })

  return response.analysis
}

async function analyzeDesign(state, payload) {
  const response = await runAnalyzerRequest(state, {
    command: 'analyzeDesign',
    diagramType: payload.diagramType,
    classCount: payload.classCount,
    relationshipCount: payload.relationshipCount,
    useCaseCount: payload.useCaseCount,
    actorCount: payload.actorCount,
    flowNodeCount: payload.flowNodeCount,
    imageProvided: payload.imageProvided,
    notes: payload.notes
  })

  return response.analysis
}

async function analyzeEstimation(state, payload) {
  const response = await runAnalyzerRequest(state, {
    command: 'analyzeEstimation',
    loc: payload.loc,
    staffCount: payload.staffCount,
    devMonths: payload.devMonths,
    cost: payload.cost
  })

  return response.analysis
}

async function analyzeUseCasePoints(state, payload) {
  const response = await runAnalyzerRequest(state, {
    command: 'analyzeUseCasePoints',
    simpleActors: payload.simpleActors,
    averageActors: payload.averageActors,
    complexActors: payload.complexActors,
    simpleUseCases: payload.simpleUseCases,
    averageUseCases: payload.averageUseCases,
    complexUseCases: payload.complexUseCases,
    technicalComplexityFactor: payload.technicalComplexityFactor,
    environmentalComplexityFactor: payload.environmentalComplexityFactor
  })

  return response.analysis
}

async function suggestDesignMetrics(state, payload) {
  const scanResult = await scanDesignImageWithWorker(state, payload)
  const response = await runAnalyzerRequest(state, {
    command: 'suggestDesignMetricsFromScan',
    diagramType: payload.diagramType,
    ocrAvailable: scanResult.available,
    ocrRecognizedText: scanResult.recognizedText,
    ocrAverageConfidence: scanResult.averageConfidence,
    ocrWarnings: scanResult.warnings,
    ocrTokens: scanResult.tokens,
    ocrImageWidth: scanResult.imageWidth,
    ocrImageHeight: scanResult.imageHeight
  })

  return response.payload
}

async function disposeAnalyzerState(state) {
  if (!state) {
    return
  }

  tearDownOcrWorker(state, state.ocrWorkerProcess, 'Shutting down OCR worker.', {
    killProcess: true,
    logLevel: 'info'
  })
}

module.exports = {
  analyzeDesign,
  analyzeEstimation,
  analyzeUseCasePoints,
  analyzeSources,
  suggestDesignMetrics,
  analyzeText,
  createAnalyzerState,
  disposeAnalyzerState,
  getAnalyzerStatus,
  resolveAnalyzerJarPath,
  resolveJavaBin,
  resolveOcrModelHome,
  resolveOcrPythonBin,
  scanDesignImageWithWorker
}
