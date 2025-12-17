import { NextRequest, NextResponse } from 'next/server'
import { fetchFromBackend } from '@/lib/backend'

export async function GET(req: NextRequest) {
  const res = await fetchFromBackend(req, '/share')
  return new NextResponse(res.body, { status: res.status, headers: res.headers })
}

export async function POST(req: NextRequest) {
  const res = await fetchFromBackend(req, '/share', {
    method: 'POST',
    headers: {
      'X-XSRF-TOKEN': req.headers.get('X-XSRF-TOKEN') || '',
    },
  })
  return new NextResponse(res.body, { status: res.status, headers: res.headers })
}

export async function DELETE(req: NextRequest) {
  const res = await fetchFromBackend(req, '/share', {
    method: 'DELETE',
    headers: {
      'X-XSRF-TOKEN': req.headers.get('X-XSRF-TOKEN') || '',
    },
  })
  return new NextResponse(res.body, { status: res.status, headers: res.headers })
}
