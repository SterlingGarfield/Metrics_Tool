import { nextTick, onMounted, unref, watch } from 'vue'

export function useAutosizeTextarea(textareaRef, watchSources = []) {
  async function syncHeight() {
    await nextTick()

    const textarea = unref(textareaRef)
    if (!textarea) {
      return
    }

    textarea.style.height = '0px'
    textarea.style.height = `${textarea.scrollHeight}px`
  }

  onMounted(syncHeight)

  if (Array.isArray(watchSources) && watchSources.length > 0) {
    watch(watchSources, syncHeight, { flush: 'post' })
  }

  return {
    syncHeight
  }
}
