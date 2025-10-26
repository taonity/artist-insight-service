import { NextResponse } from 'next/server'
import { metricsContentType, renderMetrics } from '@/lib/metrics'

export const dynamic = 'force-dynamic'
export const revalidate = 0

export async function GET() {
  const body = await renderMetrics()
  return new NextResponse(body, {
    status: 200,
    headers: {
      'Content-Type': metricsContentType,
      'Cache-Control': 'no-cache, no-store, max-age=0, must-revalidate',
    },
  })
}
