import { getAppManifest } from '@/lib/appManifest'

export function GET(request: Request) {
  // A dark fallback avoids a bright launch screen before the page can resolve the theme.
  const theme = new URL(request.url).searchParams.get('theme') === 'light' ? 'light' : 'dark'
  return Response.json(getAppManifest(theme), {
    headers: {
      'Content-Type': 'application/manifest+json',
      'Cache-Control': 'no-cache',
    },
  })
}
