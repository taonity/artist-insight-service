'use client'

import { useEffect } from 'react'
import { sendClientMetric } from '@/lib/client-metrics-reporter'

type ErrorSubtype = 'runtime' | 'resource' | 'promise'

function classifyError(event: ErrorEvent): ErrorSubtype {
  const target = event.target as EventTarget | null

  if (target && target !== window && target instanceof Element) {
    return 'resource'
  }

  return 'runtime'
}

function currentPage() {
  if (typeof window === 'undefined') {
    return 'unknown'
  }

  return window.location.pathname || 'unknown'
}

export default function MetricsCollector() {
  useEffect(() => {
    const handleError = (event: ErrorEvent) => {
      sendClientMetric({
        type: 'error',
        subtype: classifyError(event),
        page: currentPage(),
      })
    }

    const handleRejection = () => {
      sendClientMetric({
        type: 'error',
        subtype: 'promise',
        page: currentPage(),
      })
    }

    window.addEventListener('error', handleError)
    window.addEventListener('unhandledrejection', handleRejection)

    return () => {
      window.removeEventListener('error', handleError)
      window.removeEventListener('unhandledrejection', handleRejection)
    }
  }, [])

  return null
}
