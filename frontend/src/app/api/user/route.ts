import { NextRequest, NextResponse } from 'next/server'
import { fetchFromBackend } from '@/lib/backend'
import { withApiMetrics } from '@/lib/metrics'

async function handleGet(req: NextRequest) {
  const res = await fetchFromBackend(req, '/user')
  return new NextResponse(res.body, { status: res.status, headers: res.headers })
}

export const GET = withApiMetrics(handleGet, { route: '/api/user' })
