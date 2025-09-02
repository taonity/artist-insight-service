import { NextRequest, NextResponse } from 'next/server'
import { fetchFromBackend } from '@/lib/backend'

export async function GET(req: NextRequest) {
  const res = await fetchFromBackend(req, '/user')
  return new NextResponse(res.body, { status: res.status, headers: res.headers })
}
