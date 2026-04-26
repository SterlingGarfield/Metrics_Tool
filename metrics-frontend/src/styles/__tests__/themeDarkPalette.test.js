import { readFileSync } from 'node:fs'
import { resolve } from 'node:path'

const themePath = resolve(process.cwd(), 'src/styles/theme.css')

describe('dark theme palette', () => {
  test('uses the Claude-inspired charcoal palette while preserving the green accent', () => {
    const css = readFileSync(themePath, 'utf8')

    expect(css).toContain(":root[data-theme='dark'] {")
    expect(css).toContain("--accent: #8DC752;")
    expect(css).toContain("--accent-rgb: 141 199 82;")
    expect(css).toContain("--accent-strong: #76AB43;")
    expect(css).toContain("--accent-soft: rgba(var(--accent-rgb), 0.16);")
    expect(css).toContain("--accent-glow: rgba(var(--accent-rgb), 0.22);")
    expect(css).toContain("--paper: #141413;")
    expect(css).toContain("--paper-strong: #1A1A18;")
    expect(css).toContain("--paper-muted: #1E1E1C;")
    expect(css).toContain("--surface: rgba(30, 30, 28, 0.88);")
    expect(css).toContain("--surface-strong: #232320;")
    expect(css).toContain("--surface-elevated: #2A2A27;")
    expect(css).toContain("--ink: #FAF9F5;")
    expect(css).toContain("--ink-soft: #E7E3D9;")
    expect(css).toContain("--ink-muted: #C9C3B7;")
    expect(css).toContain("--line: rgba(250, 249, 245, 0.12);")
    expect(css).toContain("--line-strong: rgba(250, 249, 245, 0.20);")
    expect(css).toContain("--chart-text: #FAF9F5;")
    expect(css).toContain("--chart-text-soft: rgba(231, 227, 217, 0.78);")
    expect(css).toContain("--chart-axis: rgba(250, 249, 245, 0.14);")
    expect(css).toContain("--chart-grid: rgba(250, 249, 245, 0.08);")
    expect(css).toContain("--chart-tooltip-bg: rgba(26, 26, 24, 0.98);")
    expect(css).toContain("--chart-tooltip-border: rgba(250, 249, 245, 0.12);")
    expect(css).toContain(":root[data-theme='dark'] body {")
    expect(css).toContain(":root[data-theme='dark'] .app-header,")
  })
})
