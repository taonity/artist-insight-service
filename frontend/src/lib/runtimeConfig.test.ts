import { afterEach, expect, it, vi } from 'vitest'
import { parseRuntimeConfig } from './runtimeConfig'
afterEach(() => {
  vi.unstubAllGlobals()
  vi.resetModules()
})
const config = {
  profile: 'local',
  csrfCookieName: 'XSRF-TOKEN-LOCAL',
  publicBackendUrl: 'http://127.0.0.1:8080',
  kofiUrl: 'http://127.0.0.1:8080',
}
it('validates the runtime payload', () => {
  expect(parseRuntimeConfig(config)).toEqual(config)
  expect(() => parseRuntimeConfig({ ...config, csrfCookieName: '' })).toThrow()
  expect(() => parseRuntimeConfig({ ...config, kofiUrl: undefined })).toThrow()
})
it('retries configuration after failure and coalesces concurrent reads', async () => {
  const fetch = vi
    .fn()
    .mockResolvedValueOnce(new Response(null, { status: 503 }))
    .mockResolvedValueOnce(Response.json(config))
  vi.stubGlobal('fetch', fetch)
  const { getRuntimeConfig } = await import('./runtimeConfig')
  await expect(getRuntimeConfig()).rejects.toThrow('503')
  const [a, b] = await Promise.all([getRuntimeConfig(), getRuntimeConfig()])
  expect(a).toEqual(config)
  expect(a).toBe(b)
  expect(fetch).toHaveBeenCalledTimes(2)
})
