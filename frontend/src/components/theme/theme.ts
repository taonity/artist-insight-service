export type Theme = 'system' | 'light' | 'dark'
export type ResolvedTheme = Exclude<Theme, 'system'>

export const THEME_STORAGE_KEY = 'artist-insight.theme'
export const DARK_MODE_QUERY = '(prefers-color-scheme: dark)'
export const THEME_COLORS = { light: '#f7fbf7', dark: '#191414' }

export function readTheme(value: string | null): Theme {
  return value === 'light' || value === 'dark' ? value : 'system'
}

// Keep this self-contained: it also runs inline before React hydrates.
export function applyDocumentTheme(theme: ResolvedTheme, color: string) {
  document.documentElement.dataset.theme = theme
  document.documentElement.style.colorScheme = theme
  document.querySelectorAll('meta[name="theme-color"]').forEach((meta) => {
    meta.setAttribute('content', color)
  })
  document.querySelector('meta[name="color-scheme"]')?.setAttribute('content', theme)
  const manifest = document.querySelector('link[rel="manifest"]')
  const manifestUrl = `/manifest.webmanifest?theme=${theme}`
  if (manifest?.getAttribute('href') !== manifestUrl) manifest?.setAttribute('href', manifestUrl)
}

export const themeInitScript = `(() => {
  function sync() {
    let theme;
    try { theme = localStorage.getItem(${JSON.stringify(THEME_STORAGE_KEY)}); } catch {}
    if (theme !== 'light' && theme !== 'dark') {
      theme = matchMedia(${JSON.stringify(DARK_MODE_QUERY)}).matches ? 'dark' : 'light';
    }
    (${applyDocumentTheme.toString()})(theme, ${JSON.stringify(THEME_COLORS)}[theme]);
  }
  sync();
  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', sync, { once: true });
  }
})();`
