import { NextResponse } from 'next/server'
import { LOCAL_BACKEND_URL } from '@/lib/backend'

export const dynamic = 'force-dynamic'

export async function GET() {
  const components: Record<string, { status: string; details?: string }> = {}

  // Check backend connectivity
  try {
    const controller = new AbortController()
    const timeoutId = setTimeout(() => controller.abort(), 5000)
    const res = await fetch(`${LOCAL_BACKEND_URL}/actuator/health/liveness`, {
      signal: controller.signal,
    })
    clearTimeout(timeoutId)
    components.backend = { status: res.ok ? 'UP' : 'DOWN' }
  } catch (err) {
    components.backend = {
      status: 'DOWN',
      details: err instanceof Error ? err.message : 'unreachable',
    }
  }

  const overallStatus = Object.values(components).every(c => c.status === 'UP')
    ? 'UP'
    : 'DOWN'

  return NextResponse.json(
    { status: overallStatus, components },
    { status: overallStatus === 'UP' ? 200 : 503 },
  )
}
