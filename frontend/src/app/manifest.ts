import type { MetadataRoute } from 'next'
import { THEME_COLORS } from '@/components/theme/theme'

export default function manifest(): MetadataRoute.Manifest {
  return {
    id: '/',
    name: 'Artist Insight',
    short_name: 'Artist Insight',
    description: 'Share the artists you follow on Spotify and explore your music taste.',
    start_url: '/',
    scope: '/',
    display: 'standalone',
    background_color: THEME_COLORS.light,
    theme_color: THEME_COLORS.light,
    icons: [
      { src: '/icons/icon-192.png', sizes: '192x192', type: 'image/png', purpose: 'any' },
      { src: '/icons/icon-512.png', sizes: '512x512', type: 'image/png', purpose: 'any' },
      { src: '/icons/icon-maskable-512.png', sizes: '512x512', type: 'image/png', purpose: 'maskable' },
    ],
  }
}
