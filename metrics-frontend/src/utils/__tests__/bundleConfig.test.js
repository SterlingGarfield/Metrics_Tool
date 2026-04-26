import fs from 'node:fs'
import path from 'node:path'

describe('vite bundle config', () => {
  test('defines explicit vendor chunk boundaries for framework, charts, and icons', () => {
    const source = fs.readFileSync(path.resolve(process.cwd(), 'vite.config.js'), 'utf8')

    expect(source).toContain('manualChunks')
    expect(source).toContain("id.includes('/node_modules/vue/')")
    expect(source).toContain("id.includes('/node_modules/echarts/')")
    expect(source).toContain("id.includes('/node_modules/@icon-park/vue-next/')")
    expect(source).toContain("return 'framework'")
    expect(source).toContain("return 'charts'")
    expect(source).toContain("return 'icons'")
  })
})
