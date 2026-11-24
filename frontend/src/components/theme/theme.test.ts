import { runInNewContext } from 'node:vm'
import { describe, expect, it } from 'vitest'
import { DARK_MODE_QUERY, THEME_COLORS, THEME_STORAGE_KEY, themeInitScript } from './theme'

function createPage(saved: string | null, dark: boolean, storageBlocked = false) {
  const meta = () => ({
    attributes: {} as Record<string, string>,
    setAttribute(name: string, value: string) { this.attributes[name] = value },
    getAttribute(name: string) { return this.attributes[name] ?? null },
  })
  const themeColors = [meta(), meta()]
  const colorScheme = meta()
  const manifest = meta()
  const root = { dataset: {} as Record<string, string>, style: {} as Record<string, string> }
  const events = new Map<string, () => void>()
  const document = {
    documentElement: root,
    readyState: 'loading',
    querySelectorAll: () => themeColors,
    querySelector: (selector: string): ReturnType<typeof meta> | null => selector.includes('manifest') ? manifest : colorScheme,
    addEventListener: (event: string, callback: () => void) => events.set(event, callback),
  }
  const context = {
    document,
    localStorage: {
      getItem: (key: string) => {
        expect(key).toBe(THEME_STORAGE_KEY)
        if (storageBlocked) throw new Error('Storage blocked')
        return saved
      },
    },
    matchMedia: (query: string) => {
      expect(query).toBe(DARK_MODE_QUERY)
      return { matches: dark }
    },
  }
  return { context, root, themeColors, colorScheme, manifest, events }
}

describe('theme initialization before hydration', () => {
  it.each([
    [null, true, 'dark'],
    [null, false, 'light'],
    ['system', true, 'dark'],
    ['invalid', false, 'light'],
    ['light', true, 'light'],
    ['dark', false, 'dark'],
  ] as const)('resolves saved %s with system dark=%s to %s', (saved, dark, theme) => {
    const page = createPage(saved, dark)
    runInNewContext(themeInitScript, page.context)
    expect(page.root.dataset.theme).toBe(theme)
    expect(page.root.style.colorScheme).toBe(theme)
    expect(page.themeColors.map(meta => meta.attributes.content)).toEqual([
      THEME_COLORS[theme], THEME_COLORS[theme],
    ])
    expect(page.colorScheme.attributes.content).toBe(theme)
    expect(page.manifest.attributes.href).toBe(`/manifest.webmanifest?theme=${theme}`)
  })

  it('uses system appearance when storage is unavailable', () => {
    const page = createPage(null, true, true)
    runInNewContext(themeInitScript, page.context)
    expect(page.root.dataset.theme).toBe('dark')
    expect(page.manifest.attributes.href).toBe('/manifest.webmanifest?theme=dark')
  })

  it('updates metadata that arrives after the inline script', () => {
    const page = createPage('light', true)
    const querySelector = page.context.document.querySelector
    page.context.document.querySelector = () => null
    runInNewContext(themeInitScript, page.context)
    expect(page.root.dataset.theme).toBe('light')
    expect(page.manifest.attributes.href).toBeUndefined()
    page.context.document.querySelector = querySelector
    page.events.get('DOMContentLoaded')?.()
    expect(page.manifest.attributes.href).toBe('/manifest.webmanifest?theme=light')
  })
})
