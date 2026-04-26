import { ref } from 'vue'
import {
  analyzeDesign,
  analyzeEstimation,
  analyzeUseCasePoints,
  analyzeFiles,
  analyzeFolder,
  analyzeText,
  selectFiles,
  selectFolder
} from '../api/metrics'

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

  function hasSelection(selection) {
    if (!selection) {
      return false
    }

    if (Array.isArray(selection)) {
      return selection.length > 0
    }

    if (Array.isArray(selection.files)) {
      return selection.files.length > 0
    }

    return true
  }

  async function runTextAnalysis(payload) {
    await runWith(() => analyzeText(payload))
  }

  async function runSingleFileAnalysis(files) {
    const selection = hasSelection(files)
      ? files
      : await selectFiles({ multiple: false })

    if (!hasSelection(selection)) {
      return
    }

    await runWith(() => analyzeFiles(selection))
  }

  async function runMultiFileAnalysis(files) {
    const selection = hasSelection(files)
      ? files
      : await selectFiles({ multiple: true })

    if (!hasSelection(selection)) {
      return
    }

    await runWith(() => analyzeFiles(selection))
  }

  async function runFolderAnalysis(folderSelection) {
    const selection = hasSelection(folderSelection)
      ? folderSelection
      : await selectFolder()

    if (!hasSelection(selection)) {
      return
    }

    await runWith(() => analyzeFolder(selection))
  }

  async function runDesignAnalysis(payload) {
    await runWith(() => analyzeDesign(payload))
  }

  async function runEstimationAnalysis(payload) {
    await runWith(() => analyzeEstimation(payload))
  }

  async function runUseCasePointAnalysis(payload) {
    await runWith(() => analyzeUseCasePoints(payload))
  }

  return {
    loading,
    result,
    error,
    runTextAnalysis,
    runSingleFileAnalysis,
    runMultiFileAnalysis,
    runFolderAnalysis,
    runDesignAnalysis,
    runEstimationAnalysis,
    runUseCasePointAnalysis
  }
}
