export function logError(context: string, message: string, error?: unknown) {
  if (typeof window !== 'undefined' && localStorage.getItem('artist-insight-debug') === 'true') {
    console.error(`[${context}] ${message}`, error)
  }
}

export function logDebug(context: string, message: string, error?: unknown) {
  if (typeof window !== 'undefined' && localStorage.getItem('artist-insight-debug') === 'true') {
    console.debug(`[${context}] ${message}`, error)
  }
}