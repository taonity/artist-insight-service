import { NextResponse } from 'next/server'
import type { NextRequest } from 'next/server'
import {
  recordFrontendErrorMetric,
  recordWebVitalMetric,
  withApiMetrics,
} from '@/lib/metrics'

export const dynamic = 'force-dynamic'

type ClientMetricBody =
  | {
      type: 'web-vital'
      metric: unknown
      value: unknown
      rating?: unknown
      page?: unknown
    }
  | {
      type: 'error'
      subtype: unknown
      page?: unknown
    }

function normalizePage(input: unknown) {
  if (typeof input !== 'string' || input.length === 0) {
    return 'unknown'
  }

  const normalized = input[0] === '/' ? input : `/${input}`

  if (normalized.length > 120) {
    return normalized.slice(0, 120)
  }

  return normalized
}

function isFiniteNumber(value: unknown): value is number {
  return typeof value === 'number' && Number.isFinite(value)
}

function normalizeRating(value: unknown) {
  if (
    value === 'good' ||
    value === 'needs-improvement' ||
    value === 'poor'
  ) {
    return value
  }

  return 'unknown'
}

function normalizeErrorSubtype(value: unknown) {
  if (value === 'runtime' || value === 'resource' || value === 'promise') {
    return value
  }

  return 'runtime'
}

const handler = async (request: NextRequest) => {
  let body: ClientMetricBody

  try {
    body = (await request.json()) as ClientMetricBody
  } catch (error) {
    return NextResponse.json({ error: 'invalid_json' }, { status: 400 })
  }

  if (body.type === 'web-vital') {
    if (!isFiniteNumber(body.value) || typeof body.metric !== 'string') {
      return NextResponse.json({ error: 'invalid_web_vital' }, { status: 400 })
    }

    recordWebVitalMetric({
      metric: body.metric,
      page: normalizePage(body.page),
      rating: normalizeRating(body.rating),
      value: body.value,
    })

    return NextResponse.json({ status: 'ok' }, { status: 202 })
  }

  if (body.type === 'error') {
    recordFrontendErrorMetric({
      type: normalizeErrorSubtype(body.subtype),
      page: normalizePage(body.page),
    })

    return NextResponse.json({ status: 'ok' }, { status: 202 })
  }

  return NextResponse.json({ error: 'unsupported_metric_type' }, { status: 400 })
}

export const POST = withApiMetrics(handler, {
  route: '/api/actuator/metrics/client',
})
