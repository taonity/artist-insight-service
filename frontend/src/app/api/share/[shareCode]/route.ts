import { NextRequest, NextResponse } from 'next/server'
import { fetchFromBackend } from '@/lib/backend'
export async function GET(
  req: NextRequest,
  { params }: { params: Promise<{ shareCode: string }> },
) {
  const { shareCode } = await params
  const res = await fetchFromBackend(req, `/share/${encodeURIComponent(shareCode)}`, {
    cache: 'no-store',
  })
  return new NextResponse(res.body, { status: res.status, headers: res.headers })
}
