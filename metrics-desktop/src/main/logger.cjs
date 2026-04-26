const fs = require('fs')
const path = require('path')

function serializeDetails(details) {
  if (!details) {
    return ''
  }

  if (typeof details === 'string') {
    return details
  }

  try {
    return JSON.stringify(details)
  } catch {
    return String(details)
  }
}

function createLogger(runtimePaths) {
  const logFile = path.join(runtimePaths.logsDir, 'desktop.log')

  function write(level, message, details) {
    const detailSuffix = serializeDetails(details)
    const line = `[${new Date().toISOString()}] [${level}] ${message}${detailSuffix ? ` ${detailSuffix}` : ''}\n`
    fs.appendFileSync(logFile, line, 'utf8')
  }

  function attachStream(stream, label, level = 'INFO') {
    if (!stream) {
      return
    }

    stream.on('data', (chunk) => {
      write(level, `${label}: ${chunk.toString().trim()}`)
    })
  }

  return {
    logFile,
    info: (message, details) => write('INFO', message, details),
    warn: (message, details) => write('WARN', message, details),
    error: (message, details) => write('ERROR', message, details),
    attachStream
  }
}

module.exports = {
  createLogger
}
