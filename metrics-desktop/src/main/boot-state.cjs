function createBootState() {
  let snapshot = {
    status: 'idle',
    stage: 'idle',
    code: null,
    message: '',
    details: null,
    updatedAt: new Date().toISOString()
  }
  const listeners = new Set()

  function emit() {
    snapshot = {
      ...snapshot,
      updatedAt: new Date().toISOString()
    }

    listeners.forEach((listener) => listener(snapshot))
  }

  return {
    reset() {
      snapshot = {
        status: 'idle',
        stage: 'idle',
        code: null,
        message: '',
        details: null,
        updatedAt: new Date().toISOString()
      }
      emit()
    },
    update(stage, message, details = null) {
      snapshot = {
        ...snapshot,
        status: 'running',
        stage,
        message,
        details,
        code: null
      }
      emit()
    },
    ready(message, details = null) {
      snapshot = {
        ...snapshot,
        status: 'ready',
        message,
        details,
        code: null
      }
      emit()
    },
    fail(code, message, details = null) {
      snapshot = {
        ...snapshot,
        status: 'failed',
        code,
        message,
        details
      }
      emit()
    },
    snapshot() {
      return { ...snapshot }
    },
    onChange(listener) {
      listeners.add(listener)
      return () => listeners.delete(listener)
    }
  }
}

module.exports = {
  createBootState
}
