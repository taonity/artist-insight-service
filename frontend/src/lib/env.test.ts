import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import { getServerEnv, httpUrl } from './env'
beforeEach(() => {
  for (const key of [
    'LOCAL_BACKEND_URL',
    'PUBLIC_BACKEND_URL',
    'CSRF_COOKIE_NAME',
    'KOFI_URL',
    'BACKEND_REQUEST_TIMEOUT_MS',
  ])
    vi.stubEnv(key, '')
  vi.stubEnv('FRONTEND_PROFILE', 'local')
})
afterEach(() => vi.unstubAllEnvs())
describe('deployment environment', () => {
  it('isolates public configuration from server credentials', () => {
    vi.stubEnv('SPOTIFY_CLIENT_SECRET', 'private')
    expect(getServerEnv()).toMatchObject({
      profile: 'local',
      csrfCookieName: 'XSRF-TOKEN-LOCAL',
      kofiUrl: 'http://127.0.0.1:8080',
      backendRequestTimeoutMs: 60000,
    })
    expect(JSON.stringify(getServerEnv())).not.toContain('private')
  })
  it('supports runtime topology overrides', () => {
    vi.stubEnv('LOCAL_BACKEND_URL', 'http://app:8080/')
    expect(getServerEnv().localBackendUrl).toBe('http://app:8080')
  })
  it('requires an explicit production profile', () => {
    vi.stubEnv('NODE_ENV', 'production')
    vi.stubEnv('FRONTEND_PROFILE', '')
    expect(() => getServerEnv()).toThrow('FRONTEND_PROFILE')
  })
  it.each(['0', '-1', 'abc', '1.5', '2147483648'])('rejects invalid timeout %s', (value) => {
    vi.stubEnv('BACKEND_REQUEST_TIMEOUT_MS', value)
    expect(() => getServerEnv()).toThrow('BACKEND_REQUEST_TIMEOUT_MS')
  })
  it.each([
    'javascript:alert(1)',
    'https://user:secret@example.com',
    'https://example.com?q=secret',
  ])('rejects unsafe URL %s', (value) => expect(() => httpUrl(value, 'url')).toThrow())
})
