import { THEME_STORAGE_KEY, createThemeController } from '../useTheme'

function createStorage(initial = {}) {
  const store = new Map(Object.entries(initial))
  return {
    getItem(key) {
      return store.has(key) ? store.get(key) : null
    },
    setItem(key, value) {
      store.set(key, String(value))
    },
    removeItem(key) {
      store.delete(key)
    }
  }
}

function createMatchMedia(matches) {
  return () => ({
    matches,
    addEventListener() {},
    removeEventListener() {}
  })
}

describe('useTheme', () => {
  test('falls back to the system theme when no stored preference exists', () => {
    const root = document.createElement('div')
    const controller = createThemeController({
      storage: createStorage(),
      matchMedia: createMatchMedia(true),
      root
    })

    expect(controller.theme.value).toBe('dark')
    expect(root.getAttribute('data-theme')).toBe('dark')

    controller.dispose()
  })

  test('persists the explicit toggle choice', () => {
    const storage = createStorage()
    const root = document.createElement('div')
    const controller = createThemeController({
      storage,
      matchMedia: createMatchMedia(true),
      root
    })

    controller.toggleTheme()

    expect(controller.theme.value).toBe('light')
    expect(storage.getItem(THEME_STORAGE_KEY)).toBe('light')
    expect(root.getAttribute('data-theme')).toBe('light')

    controller.dispose()
  })

  test('restores a saved theme preference on reload', () => {
    const root = document.createElement('div')
    const controller = createThemeController({
      storage: createStorage({ [THEME_STORAGE_KEY]: 'light' }),
      matchMedia: createMatchMedia(true),
      root
    })

    expect(controller.theme.value).toBe('light')
    expect(root.getAttribute('data-theme')).toBe('light')

    controller.dispose()
  })
})
