export type Theme = 'system' | 'light' | 'dark'

export const THEME_STORAGE_KEY = 'artist-insight.theme'
export const DARK_MODE_QUERY = '(prefers-color-scheme: dark)'
export const THEME_COLORS = { light: '#f7fbf7', dark: '#191414' }

export function readTheme(value: string | null): Theme {
  return value === 'light' || value === 'dark' ? value : 'system'
}

export const themeInitScript = `(() => {
  let theme;
  try { theme = localStorage.getItem(${JSON.stringify(THEME_STORAGE_KEY)}); } catch {}
  if (theme !== 'light' && theme !== 'dark') {
    theme = matchMedia(${JSON.stringify(DARK_MODE_QUERY)}).matches ? 'dark' : 'light';
  }
  document.documentElement.dataset.theme = theme;
  document.querySelector('meta[name="theme-color"]')?.setAttribute('content', ${JSON.stringify(THEME_COLORS)}[theme]);
})();`
