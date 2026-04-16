import axios from 'axios'

const client = axios.create({
  baseURL: '/api/metrics'
})

export function checkHealth() {
  return client.get('/health')
}

export function analyzeText(payload) {
  return client.post('/analyze/text', payload)
}

export function analyzeFiles(files) {
  const formData = new FormData()
  files.forEach((file) => formData.append('files', file))
  return client.post('/analyze/files', formData)
}

export function analyzeFolder(files) {
  const formData = new FormData()
  const relativePaths = files.map((file) => file.webkitRelativePath || file.name)
  files.forEach((file) => formData.append('files', file))
  formData.append('relativePaths', JSON.stringify(relativePaths))
  return client.post('/analyze/folder', formData)
}
