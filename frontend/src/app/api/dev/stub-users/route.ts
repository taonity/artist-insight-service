import { NextRequest, NextResponse } from 'next/server'
import { getServerEnv } from '@/lib/env'
import { fetchFromBackend } from '@/lib/backend'
export const dynamic = 'force-dynamic'
export async function GET(req: NextRequest) {
  if (!['local', 'demo'].includes(getServerEnv().profile))
    return new NextResponse(null, { status: 404 })
  const response = await fetchFromBackend(req, '/dev/stub-users', {}, 6000)
  return new NextResponse(response.body, { status: response.status, headers: response.headers })
}
