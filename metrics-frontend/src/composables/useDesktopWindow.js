import { onBeforeUnmount, onMounted, ref, unref } from 'vue'
import {
  closeDesktopWindow,
  getDesktopWindowState,
  minimizeDesktopWindow,
  onDesktopWindowStateChanged,
  showDesktopAppMenu,
  toggleDesktopMaximizeWindow
} from '../api/metrics'

export function useDesktopWindow(desktopEnabled) {
  const isMaximized = ref(false)
  let unsubscribe = () => {}

  onMounted(async () => {
    if (!unref(desktopEnabled)) {
      return
    }

    const initialState = await getDesktopWindowState()
    isMaximized.value = Boolean(initialState?.isMaximized)
    unsubscribe = onDesktopWindowStateChanged((state) => {
      isMaximized.value = Boolean(state?.isMaximized)
    })
  })

  onBeforeUnmount(() => {
    unsubscribe?.()
    unsubscribe = () => {}
  })

  return {
    isMaximized,
    minimizeWindow: () => minimizeDesktopWindow(),
    toggleMaximizeWindow: async () => {
      const state = await toggleDesktopMaximizeWindow()
      isMaximized.value = Boolean(state?.isMaximized)
    },
    closeWindow: () => closeDesktopWindow(),
    showAppMenu: () => showDesktopAppMenu()
  }
}
