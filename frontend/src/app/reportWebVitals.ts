import type { Metric } from 'next/dist/compiled/web-vitals'
import { sendClientMetric } from '@/lib/client-metrics-reporter'

type RatedMetric = Metric & {
  rating?: 'good' | 'needs-improvement' | 'poor'
}

function normalizeRating(metric: RatedMetric) {
  if (metric.rating === 'good' || metric.rating === 'needs-improvement' || metric.rating === 'poor') {
    return metric.rating
  }

  return 'unknown'
}

function currentPage() {
  if (typeof window === 'undefined') {
    return 'unknown'
  }

  return window.location.pathname || 'unknown'
}

export function reportWebVitals(metric: RatedMetric) {
  sendClientMetric({
    type: 'web-vital',
    metric: metric.name,
    value: metric.value,
    rating: normalizeRating(metric),
    page: currentPage(),
  })
}

export default reportWebVitals
