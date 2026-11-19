import { NextResponse } from 'next/server'
import { getServerEnv } from '@/lib/env'
export const dynamic = 'force-dynamic'
export function GET() {
  const { profile, publicBackendUrl, csrfCookieName, kofiUrl } = getServerEnv()
  return NextResponse.json({ profile, publicBackendUrl, csrfCookieName, kofiUrl })
}
