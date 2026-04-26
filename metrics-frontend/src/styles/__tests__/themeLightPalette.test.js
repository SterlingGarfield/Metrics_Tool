import { readFileSync } from 'node:fs'
import { resolve } from 'node:path'

const themePath = resolve(process.cwd(), 'src/styles/theme.css')

describe('light theme palette', () => {
  test('reduces light theme accent brightness while keeping accent surfaces consistent', () => {
    const css = readFileSync(themePath, 'utf8')

    const rootBlock = css.slice(
      css.indexOf(':root {'),
      css.indexOf(":root[data-theme='dark'] {")
    )

    expect(rootBlock).toContain('--accent: #76AB43;')
    expect(rootBlock).toContain('--accent-rgb: 118 171 67;')
    expect(rootBlock).toContain('--accent-strong: #5F8E34;')
    expect(rootBlock).toContain('--accent-soft: rgba(var(--accent-rgb), 0.16);')
    expect(rootBlock).toContain('--accent-glow: rgba(var(--accent-rgb), 0.22);')
    expect(rootBlock).toContain('--chart-bar: #76AB43;')
    expect(css).toContain('rgba(var(--accent-rgb), 0.28)')
    expect(css).toContain('rgba(var(--accent-rgb), 0.08)')
    expect(css).toContain('rgba(var(--accent-rgb), 0.09)')
    expect(css).toContain('rgba(var(--accent-rgb), 0.14)')
  })
})
