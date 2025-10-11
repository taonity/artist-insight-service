export const dynamic = 'force-dynamic';

import { NextResponse } from 'next/server'
import { PUBLIC_BACKEND_URL } from '@/lib/backend'

export function GET() {
  return NextResponse.redirect(
    `${PUBLIC_BACKEND_URL}/oauth2/authorization/spotify-artist-insight-service`
  )
}
