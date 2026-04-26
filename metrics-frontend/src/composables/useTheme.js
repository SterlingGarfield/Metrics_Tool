import { computed, ref } from 'vue'

export const THEME_STORAGE_KEY = 'metrics-theme'
const THEME_QUERY = '(prefers-color-scheme: dark)'

function normalizeTheme(value) {
  return value === 'light' || value === 'dark' ? value : null
}

function readStoredTheme(storage) {
  if (!storage?.getItem) {
    return null
  }

  return normalizeTheme(storage.getItem(THEME_STORAGE_KEY))
}

function writeStoredTheme(storage, theme) {
  storage?.setItem?.(THEME_STORAGE_KEY, theme)
}

function attachMediaListener(mediaQuery, handler) {
  if (!mediaQuery) {
    return () => {}
  }

  if (typeof mediaQuery.addEventListener === 'function') {
    mediaQuery.addEventListener('change', handler)
    return () => mediaQuery.removeEventListener('change', handler)
  }

  if (typeof mediaQuery.addListener === 'function') {
    mediaQuery.addListener(handler)
    return () => mediaQuery.removeListener(handler)
  }

  return () => {}
}

export function createThemeController(options = {}) {
  const storage = options.storage
  const root = options.root ?? null
  const mediaQuery = typeof options.matchMedia === 'function'
    ? options.matchMedia(THEME_QUERY)
    : null

  const selectedTheme = ref(readStoredTheme(storage))
  const systemTheme = ref(mediaQuery?.matches ? 'dark' : 'light')
  const theme = computed(() => selectedTheme.value ?? systemTheme.value)

  function applyTheme() {
    root?.setAttribute?.('data-theme', theme.value)
  }

  const stopListening = attachMediaListener(mediaQuery, (event) => {
    systemTheme.value = event.matches ? 'dark' : 'light'
    if (!selectedTheme.value) {
      applyTheme()
    }
  })

  function toggleTheme() {
    const nextTheme = theme.value === 'dark' ? 'light' : 'dark'
    selectedTheme.value = nextTheme
    writeStoredTheme(storage, nextTheme)
    applyTheme()
  }

  applyTheme()

  return {
    theme,
    toggleTheme,
    dispose() {
      stopListening()
    }
  }
}

export function useTheme() {
  if (typeof window === 'undefined' || typeof document === 'undefined') {
    return createThemeController()
  }

  return createThemeController({
    storage: window.localStorage,
    matchMedia: window.matchMedia?.bind(window),
    root: document.documentElement
  })
}
