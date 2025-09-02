import { NextResponse } from 'next/server'
import { BACKEND_URL } from '@/lib/backend'

export function GET() {
  return NextResponse.redirect(
    `${BACKEND_URL}/oauth2/authorization/spotify-artist-insight-service`
  )
}
