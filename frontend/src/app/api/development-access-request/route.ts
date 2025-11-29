import { NextRequest, NextResponse } from 'next/server'
import { fetchFromBackend } from '@/lib/backend'

export async function POST(req: NextRequest) {
  const body = await req.text()
  const xsrf = req.headers.get('x-xsrf-token') ?? ''
  const res = await fetchFromBackend(req, '/development-access-request', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      'X-XSRF-TOKEN': xsrf,
    },
    body: body
  })
  return new NextResponse(res.body, { status: res.status, headers: res.headers })
}