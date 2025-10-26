import { NextRequest, NextResponse } from 'next/server'
import { fetchFromBackend } from '@/lib/backend'
import { withApiMetrics } from '@/lib/metrics'

async function handlePost(req: NextRequest) {
  const xsrf = req.headers.get('x-xsrf-token') ?? ''
  const res = await fetchFromBackend(req, '/logout', {
    method: 'POST',
    headers: { 'X-XSRF-TOKEN': xsrf },
  })
  return new NextResponse(res.body, { status: res.status, headers: res.headers })
}

export const POST = withApiMetrics(handlePost, { route: '/api/logout' })
