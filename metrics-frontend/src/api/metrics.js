import axios from 'axios'

const metricsClient = axios.create({
  baseURL: '/api/metrics'
})

const designClient = axios.create({
  baseURL: '/api/design'
})

const recognitionClient = axios.create({
  baseURL: '/api/recognition'
})

const estimationClient = axios.create({
  baseURL: '/api/estimate'
})

export function checkHealth() {
  return metricsClient.get('/health')
}

export function analyzeText(payload) {
  return metricsClient.post('/analyze/text', payload)
}

export function analyzeFiles(files) {
  const formData = new FormData()
  files.forEach((file) => formData.append('files', file))
  return metricsClient.post('/analyze/files', formData)
}

export function analyzeFolder(files) {
  const formData = new FormData()
  const relativePaths = files.map((file) => file.webkitRelativePath || file.name)
  files.forEach((file) => formData.append('files', file))
  formData.append('relativePaths', JSON.stringify(relativePaths))
  return metricsClient.post('/analyze/folder', formData)
}

export function analyzeStructuredDiagram(file, diagramType) {
  const formData = new FormData()
  formData.append('file', file)
  formData.append('diagramType', diagramType)
  return designClient.post('/analyze/structured', formData)
}

export function analyzeImageDiagram(file, diagramType) {
  const formData = new FormData()
  formData.append('file', file)
  formData.append('diagramType', diagramType)
  return designClient.post('/analyze/image', formData)
}

export function checkRecognitionHealth() {
  return recognitionClient.get('/health')
}

export function fetchModelsStatus() {
  return recognitionClient.get('/models/status')
}

export function estimateProject(payload) {
  return estimationClient.post('/project', payload)
}
