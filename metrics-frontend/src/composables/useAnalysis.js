import { ref } from 'vue'
import { analyzeFiles, analyzeFolder, analyzeText } from '../api/metrics'

export function useAnalysis() {
  const loading = ref(false)
  const result = ref(null)
  const error = ref('')

  async function runWith(action) {
    loading.value = true
    error.value = ''
    try {
      const response = await action()
      result.value = response.data
    } catch (err) {
      error.value = err?.message || 'Analysis failed'
    } finally {
      loading.value = false
    }
  }

  async function runTextAnalysis(payload) {
    await runWith(() => analyzeText(payload))
  }

  async function runFileAnalysis(files) {
    await runWith(() => analyzeFiles(files))
  }

  async function runFolderAnalysis(files) {
    await runWith(() => analyzeFolder(files))
  }

  return { loading, result, error, runTextAnalysis, runFileAnalysis, runFolderAnalysis }
}
