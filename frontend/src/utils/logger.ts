/**
 * Logs error messages to the console when debug mode is enabled.
 * Debug mode is activated by setting 'artist-insight-debug' to 'true' in localStorage.
 * 
 * @param context - The context or component name where the error occurred
 * @param message - The error message to log
 * @param error - Optional error object or additional details
 */
export function logError(context: string, message: string, error?: any) {
  if (typeof window !== 'undefined' && localStorage.getItem('artist-insight-debug') === 'true') {
    console.error(`[${context}] ${message}`, error)
  }
}


export function logDebug(context: string, message: string, error?: any) {
  if (typeof window !== 'undefined' && localStorage.getItem('artist-insight-debug') === 'true') {
    console.debug(`[${context}] ${message}`, error)
  }
}