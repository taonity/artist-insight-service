import { NextRequest, NextResponse } from 'next/server'
import { LOCAL_BACKEND_URL } from '@/lib/backend'

export async function GET(
  req: NextRequest,
  { params }: { params: Promise<{ shareCode: string }> }
) {
  const { shareCode } = await params
  
  const res = await fetch(`${LOCAL_BACKEND_URL}/share/${shareCode}`, {
    method: 'GET',
    cache: 'no-store'
  })
  
  return new NextResponse(res.body, { status: res.status, headers: res.headers })
}
