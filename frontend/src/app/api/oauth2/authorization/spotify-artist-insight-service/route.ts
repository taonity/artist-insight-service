import { NextResponse } from 'next/server'
import { getServerEnv } from '@/lib/env'
export const dynamic = 'force-dynamic'
export function GET() {
  return NextResponse.redirect(
    `${getServerEnv().publicBackendUrl}/oauth2/authorization/spotify-artist-insight-service`,
  )
}
