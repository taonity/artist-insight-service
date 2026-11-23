import type { MetadataRoute } from 'next'
import { THEME_COLORS } from '@/components/theme/theme'
import { getAppIcons } from '@/lib/appIcons'

export const dynamic = 'force-dynamic'

export default function manifest(): MetadataRoute.Manifest {
  const { manifestIconPrefix } = getAppIcons()
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
      { src: `${manifestIconPrefix}-192.png`, sizes: '192x192', type: 'image/png', purpose: 'any' },
      { src: `${manifestIconPrefix}-512.png`, sizes: '512x512', type: 'image/png', purpose: 'any' },
      { src: `${manifestIconPrefix}-maskable-512.png`, sizes: '512x512', type: 'image/png', purpose: 'maskable' },
    ],
  }
}
