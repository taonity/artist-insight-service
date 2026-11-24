import { afterEach, expect, it, vi } from 'vitest'
import { GET } from '@/app/manifest.webmanifest/route'
import { THEME_COLORS } from '@/components/theme/theme'

afterEach(() => vi.unstubAllEnvs())

it.each(['dark', 'light'] as const)('serves a %s launch screen without changing app identity', async theme => {
  vi.stubEnv('FRONTEND_PROFILE', 'prod')
  const response = GET(new Request(`https://artist-insight.example/manifest.webmanifest?theme=${theme}`))
  expect(response.headers.get('Content-Type')).toBe('application/manifest+json')
  expect(response.headers.get('Cache-Control')).toBe('no-cache')
  expect(await response.json()).toMatchObject({
    id: '/', start_url: '/', scope: '/', display: 'standalone',
    background_color: THEME_COLORS[theme], theme_color: THEME_COLORS[theme],
    icons: [
      { src: '/icons/icon-192.png' },
      { src: '/icons/icon-512.png' },
      { src: '/icons/icon-maskable-512.png' },
    ],
  })
})

it.each(['', '?theme=invalid'])('uses a dark fallback and preserves stage icons for %s', async query => {
  vi.stubEnv('FRONTEND_PROFILE', 'stage')
  const response = GET(new Request(`https://artist-insight.example/manifest.webmanifest${query}`))
  expect(await response.json()).toMatchObject({
    background_color: THEME_COLORS.dark, theme_color: THEME_COLORS.dark,
    icons: [
      { src: '/icons/icon-stage-192.png' },
      { src: '/icons/icon-stage-512.png' },
      { src: '/icons/icon-stage-maskable-512.png' },
    ],
  })
})
