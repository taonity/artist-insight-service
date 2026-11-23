'use client'

import { createContext, useContext, useEffect, useState, type ReactNode } from 'react'
import { DARK_MODE_QUERY, readTheme, THEME_COLORS, THEME_STORAGE_KEY, type Theme } from './theme'

const ThemeContext = createContext<{ preference: Theme; setPreference: (theme: Theme) => void }>({
  preference: 'system',
  setPreference: () => {},
})

function applyTheme(preference: Theme) {
  const theme = preference === 'system'
    ? (matchMedia(DARK_MODE_QUERY).matches ? 'dark' : 'light')
    : preference
  document.documentElement.dataset.theme = theme
  document.querySelector('meta[name="theme-color"]')?.setAttribute('content', THEME_COLORS[theme])
}

export default function ThemeProvider({ children }: { children: ReactNode }) {
  const [preference, setTheme] = useState<Theme>('system')

  useEffect(() => {
    let saved: Theme = 'system'
    try {
      saved = readTheme(localStorage.getItem(THEME_STORAGE_KEY))
    } catch {}
    setTheme(saved)
    applyTheme(saved)

    function syncPreference(event: StorageEvent) {
      if (event.key !== null && event.key !== THEME_STORAGE_KEY) return
      const theme = readTheme(event.newValue)
      setTheme(theme)
      applyTheme(theme)
    }
    window.addEventListener('storage', syncPreference)
    return () => window.removeEventListener('storage', syncPreference)
  }, [])

  useEffect(() => {
    if (preference !== 'system') return
    const media = matchMedia(DARK_MODE_QUERY)
    const update = () => {
      if (media.matches !== (document.documentElement.dataset.theme === 'dark')) applyTheme('system')
    }
    media.addEventListener('change', update)
    return () => media.removeEventListener('change', update)
  }, [preference])

  function setPreference(theme: Theme) {
    setTheme(theme)
    applyTheme(theme)
    try {
      localStorage.setItem(THEME_STORAGE_KEY, theme)
    } catch {}
  }

  return <ThemeContext.Provider value={{ preference, setPreference }}>{children}</ThemeContext.Provider>
}

export function useTheme() {
  return useContext(ThemeContext)
}
