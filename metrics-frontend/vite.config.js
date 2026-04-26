import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

export default defineConfig({
  base: './',
  plugins: [vue()],
  build: {
    rollupOptions: {
      output: {
        manualChunks(id) {
          id = id.replaceAll('\\', '/')

          if (id.includes('/node_modules/vue/')) {
            return 'framework'
          }

          if (id.includes('/node_modules/echarts/')) {
            return 'charts'
          }

          if (id.includes('/node_modules/@icon-park/vue-next/')) {
            return 'icons'
          }
        }
      }
    }
  },
  test: {
    environment: 'jsdom',
    setupFiles: './vitest.setup.js',
    globals: true
  },
  server: {
    port: 5173
  }
})
