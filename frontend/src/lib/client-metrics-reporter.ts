'use client'

const ENDPOINT = '/api/actuator/metrics/client'

export type WebVitalPayload = {
  type: 'web-vital'
  metric: string
  value: number
  rating?: string
  page: string
}

export type ErrorPayload = {
  type: 'error'
  subtype: 'runtime' | 'resource' | 'promise'
  page: string
}

export type ClientMetricPayload = WebVitalPayload | ErrorPayload

function sendWithBeacon(body: string) {
  if (typeof navigator === 'undefined' || typeof navigator.sendBeacon !== 'function') {
    return false
  }

  try {
    const blob = new Blob([body], { type: 'application/json' })
    return navigator.sendBeacon(ENDPOINT, blob)
  } catch (error) {
    if (process.env.NODE_ENV !== 'production') {
      console.warn('Failed to send client metric via Beacon', error)
    }
    return false
  }
}

export function sendClientMetric(payload: ClientMetricPayload) {
  const body = JSON.stringify({
    ...payload,
    timestamp: Date.now(),
  })

  if (sendWithBeacon(body)) {
    return
  }

  void fetch(ENDPOINT, {
    method: 'POST',
    body,
    keepalive: true,
    headers: {
      'Content-Type': 'application/json',
    },
  }).catch((error) => {
    if (process.env.NODE_ENV !== 'production') {
      console.warn('Failed to send client metric via fetch', error)
    }
  })
}
