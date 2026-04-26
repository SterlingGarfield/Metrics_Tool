import { readFileSync } from 'node:fs'
import { resolve } from 'node:path'

const themePath = resolve(process.cwd(), 'src/styles/theme.css')

describe('background and desktop shell layout', () => {
  test('defines fixed themed backdrop layers and a pinned desktop title bar shell', () => {
    const css = readFileSync(themePath, 'utf8')

    expect(css).toContain('theme-backdrop-light.png')
    expect(css).toContain('theme-backdrop-dark.png')
    expect(css).toContain('.app-background-layer {')
    expect(css).toContain('position: fixed;')
    expect(css).toContain('inset: 0;')
    expect(css).toContain('background-size: cover;')
    expect(css).toContain('background-position: center center;')
    expect(css).toContain('.desktop-titlebar-shell {')
    expect(css).toContain('top: 0;')
    expect(css).toContain('left: 0;')
    expect(css).toContain('right: 0;')
    expect(css).toContain('padding-inline: 0;')
    expect(css).toContain('.desktop-titlebar {')
    expect(css).toContain('width: 100%;')
    expect(css).not.toContain('width: min(1320px, 100%);')
    expect(css).toContain('.app-shell.is-desktop-shell {')
    expect(css).toContain('--desktop-titlebar-height: 40px;')
    expect(css).toContain('font-family: "Söhne";')
    expect(css).toContain('--font-sans: "Söhne", "Sohne"')
    expect(css).toContain('--font-mono: "Söhne Mono", "Sohne Mono"')
    expect(css).toContain('top: calc(var(--desktop-titlebar-height) + 28px);')
  })
})
