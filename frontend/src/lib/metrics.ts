import type { NextRequest } from 'next/server'
import client from 'prom-client'

type MetricLabelNames = 'method' | 'route' | 'status'

type WebVitalLabelNames = 'metric' | 'page' | 'rating'

type FrontendErrorLabelNames = 'type' | 'page'

type MetricLabels = Record<MetricLabelNames, string>

type WebVitalLabels = Record<WebVitalLabelNames, string>

type FrontendErrorLabels = Record<FrontendErrorLabelNames, string>

type MetricsGlobal = typeof globalThis & {
  __ARTIST_INSIGHT_METRICS__?: {
    registry: client.Registry
    requestCounter: client.Counter<MetricLabelNames>
    requestDurationMax: client.Gauge<MetricLabelNames>
    webVitalGauge: client.Gauge<WebVitalLabelNames>
    frontendErrorCounter: client.Counter<FrontendErrorLabelNames>
    maxValues: Map<string, number>
    resetTimer: NodeJS.Timeout
  }
}

const globalWithMetrics = globalThis as MetricsGlobal

if (!globalWithMetrics.__ARTIST_INSIGHT_METRICS__) {
  const registry = new client.Registry()
  client.collectDefaultMetrics({ register: registry })

  const requestCounter = new client.Counter<MetricLabelNames>({
    name: 'http_request_total',
    help: 'Count of HTTP requests received by Next.js API routes.',
    labelNames: ['method', 'route', 'status'],
    registers: [registry],
  })

  const requestDurationMax = new client.Gauge<MetricLabelNames>({
    name: 'http_request_duration_seconds_max',
    help: 'Maximum duration of HTTP requests processed by Next.js API routes in seconds within the reset window.',
    labelNames: ['method', 'route', 'status'],
    registers: [registry],
  })

  const webVitalGauge = new client.Gauge<WebVitalLabelNames>({
    name: 'frontend_web_vital_value',
    help: 'Latest real user monitoring value reported from the browser for a given web vital metric.',
    labelNames: ['metric', 'page', 'rating'],
    registers: [registry],
  })

  const frontendErrorCounter = new client.Counter<FrontendErrorLabelNames>({
    name: 'frontend_error_total',
    help: 'Count of client-observed failures grouped by error type and page.',
    labelNames: ['type', 'page'],
    registers: [registry],
  })

  const maxValues = new Map<string, number>()
  const resetTimer = setInterval(() => {
    requestDurationMax.reset()
    maxValues.clear()
  }, 2 * 60 * 1000)

  if (typeof resetTimer.unref === 'function') {
    resetTimer.unref()
  }

  globalWithMetrics.__ARTIST_INSIGHT_METRICS__ = {
    registry,
    requestCounter,
    requestDurationMax,
    webVitalGauge,
    frontendErrorCounter,
    maxValues,
    resetTimer,
  }
}

const {
  registry,
  requestCounter,
  requestDurationMax,
  webVitalGauge,
  frontendErrorCounter,
  maxValues,
} =
  globalWithMetrics.__ARTIST_INSIGHT_METRICS__!

export const metricsContentType = registry.contentType

export async function renderMetrics() {
  return registry.metrics()
}

export function recordRequestMetrics(labels: MetricLabels, durationSeconds: number) {
  requestCounter.inc(labels)
  const key = JSON.stringify(labels)
  const currentMax = maxValues.get(key) ?? 0

  if (durationSeconds > currentMax) {
    maxValues.set(key, durationSeconds)
    requestDurationMax.set(labels, durationSeconds)
  }
}

type WebVitalRecord = {
  metric: string
  page: string
  rating: string
  value: number
}

export function recordWebVitalMetric(record: WebVitalRecord) {
  const labels: WebVitalLabels = {
    metric: record.metric,
    page: record.page,
    rating: record.rating,
  }
  webVitalGauge.set(labels, record.value)
}

type FrontendErrorRecord = {
  type: string
  page: string
}

export function recordFrontendErrorMetric(record: FrontendErrorRecord) {
  const labels: FrontendErrorLabels = {
    type: record.type,
    page: record.page,
  }
  frontendErrorCounter.inc(labels)
}

type HandlerOptions = {
  route?: string
}

type MetricsRouteHandler<T = unknown> = (
  request: NextRequest,
  context?: T,
) => Response | Promise<Response>

export function withApiMetrics<T = unknown>(
  handler: MetricsRouteHandler<T>,
  options: HandlerOptions = {},
): MetricsRouteHandler<T> {
  return async (request: NextRequest, context?: T) => {
    const route = options.route ?? 'unknown'
    const method = request.method ?? 'UNKNOWN'
    let status = '500'
    const start = process.hrtime.bigint()

    try {
      const response = await handler(request, context)
      if (response) {
        status = response.status.toString()
      }
      return response
    } finally {
      const end = process.hrtime.bigint()
      const durationSeconds = Number(end - start) / 1e9
      recordRequestMetrics({ method, route, status }, durationSeconds)
    }
  }
}

