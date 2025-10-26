import type { NextRequest } from 'next/server'
import client from 'prom-client'

type MetricLabelNames = 'method' | 'route' | 'status'

type MetricLabels = Record<MetricLabelNames, string>

const DEFAULT_BUCKETS = [0.05, 0.1, 0.2, 0.5, 1, 2, 5, 10]

type MetricsGlobal = typeof globalThis & {
  __ARTIST_INSIGHT_METRICS__?: {
    registry: client.Registry
    requestCounter: client.Counter<MetricLabelNames>
    requestDuration: client.Histogram<MetricLabelNames>
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

  const requestDuration = new client.Histogram<MetricLabelNames>({
    name: 'http_request_duration_seconds',
    help: 'Duration of HTTP requests processed by Next.js API routes in seconds.',
    labelNames: ['method', 'route', 'status'],
    buckets: DEFAULT_BUCKETS,
    registers: [registry],
  })

  globalWithMetrics.__ARTIST_INSIGHT_METRICS__ = {
    registry,
    requestCounter,
    requestDuration,
  }
}

const { registry, requestCounter, requestDuration } = globalWithMetrics.__ARTIST_INSIGHT_METRICS__!

export const metricsContentType = registry.contentType

export async function renderMetrics() {
  return registry.metrics()
}

export function recordRequestMetrics(labels: MetricLabels, durationSeconds: number) {
  requestCounter.inc(labels)
  requestDuration.observe(labels, durationSeconds)
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

