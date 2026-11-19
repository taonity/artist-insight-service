import { FRONTEND_PROFILE_CONFIG, isFrontendProfile } from '@/config/environmentProfiles'

export function httpUrl(value: unknown, label: string): string {
  if (typeof value !== 'string' || !value.trim()) throw new Error(`${label} is required`)
  const url = new URL(value)
  if (
    !['http:', 'https:'].includes(url.protocol) ||
    url.username ||
    url.password ||
    url.search ||
    url.hash
  ) {
    throw new Error(`${label} must be an HTTP(S) URL without credentials, query, or fragment`)
  }
  return url.toString().replace(/\/$/, '')
}
export function getServerEnv() {
  const profile =
    process.env.FRONTEND_PROFILE?.trim() || (process.env.NODE_ENV === 'production' ? '' : 'local')
  if (!isFrontendProfile(profile))
    throw new Error('FRONTEND_PROFILE must be local, demo, stage, or prod')
  const defaults = FRONTEND_PROFILE_CONFIG[profile]
  const timeout = Number(process.env.BACKEND_REQUEST_TIMEOUT_MS?.trim() || 60000)
  if (!Number.isSafeInteger(timeout) || timeout <= 0 || timeout > 2147483647)
    throw new Error('BACKEND_REQUEST_TIMEOUT_MS must be a positive 32-bit integer')
  const csrfCookieName = process.env.CSRF_COOKIE_NAME?.trim() || defaults.csrfCookieName
  if (!/^[A-Za-z0-9_-]+$/.test(csrfCookieName)) throw new Error('Invalid CSRF_COOKIE_NAME')
  return {
    profile,
    csrfCookieName,
    backendRequestTimeoutMs: timeout,
    localBackendUrl: httpUrl(
      process.env.LOCAL_BACKEND_URL?.trim() || defaults.localBackendUrl,
      'LOCAL_BACKEND_URL',
    ),
    publicBackendUrl: httpUrl(
      process.env.PUBLIC_BACKEND_URL?.trim() || defaults.publicBackendUrl,
      'PUBLIC_BACKEND_URL',
    ),
    kofiUrl: httpUrl(process.env.KOFI_URL?.trim() || defaults.kofiUrl, 'KOFI_URL'),
  }
}
