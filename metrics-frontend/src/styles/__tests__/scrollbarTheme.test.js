import { readFileSync } from 'node:fs'
import { resolve } from 'node:path'

const themePath = resolve(process.cwd(), 'src/styles/theme.css')

describe('global scrollbar theme', () => {
  test('uses one codex-like internal content scrollbar and hides the outer page scrollbar', () => {
    const css = readFileSync(themePath, 'utf8')

    expect(css).toContain('--scrollbar-size: 6px;')
    expect(css).toContain('--scrollbar-track: transparent;')
    expect(css).toContain('--scrollbar-thumb: rgba(92, 75, 67, 0.32);')
    expect(css).toContain('--scrollbar-thumb-hover: rgba(92, 75, 67, 0.46);')
    expect(css).toContain('--scrollbar-thumb-active: color-mix(')
    expect(css).toContain('html,')
    expect(css).toContain('#app,')
    expect(css).toContain('height: 100%;')
    expect(css).toContain('overflow: hidden;')
    expect(css).toContain('.desktop-workbench {')
    expect(css).toContain('height: 100vh;')
    expect(css).toContain('.desktop-content-scroll {')
    expect(css).toContain('height: calc(100vh - var(--desktop-titlebar-height) - 56px);')
    expect(css).toContain('overflow: auto;')
    expect(css).toContain('scrollbar-width: thin;')
    expect(css).toContain('.desktop-content-scroll::-webkit-scrollbar {')
    expect(css).toContain('.desktop-content-scroll::-webkit-scrollbar-thumb {')
    expect(css).toContain('border-radius: var(--scrollbar-radius);')
    expect(css).toContain(":root[data-theme='dark'] {")
    expect(css).toContain('--scrollbar-thumb: rgba(250, 249, 245, 0.22);')
    expect(css).toContain('--scrollbar-thumb-hover: rgba(250, 249, 245, 0.34);')
    expect(css).not.toContain('*::-webkit-scrollbar {')
  })
})
