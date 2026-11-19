import { isFrontendProfile, type FrontendProfile } from '@/config/environmentProfiles'
import { httpUrl } from '@/lib/env'
export interface RuntimeConfig {
  profile: FrontendProfile
  csrfCookieName: string
  publicBackendUrl: string
  kofiUrl: string
}
let configCache: RuntimeConfig | null = null
let pending: Promise<RuntimeConfig> | null = null
export function parseRuntimeConfig(value: unknown): RuntimeConfig {
  if (!value || typeof value !== 'object') throw new Error('Invalid runtime configuration')
  const c = value as Record<string, unknown>
  if (typeof c.profile !== 'string' || !isFrontendProfile(c.profile))
    throw new Error('Invalid frontend profile')
  if (typeof c.csrfCookieName !== 'string' || !/^[A-Za-z0-9_-]+$/.test(c.csrfCookieName))
    throw new Error('Invalid CSRF cookie name')
  return {
    profile: c.profile,
    csrfCookieName: c.csrfCookieName,
    publicBackendUrl: httpUrl(c.publicBackendUrl, 'publicBackendUrl'),
    kofiUrl: httpUrl(c.kofiUrl, 'kofiUrl'),
  }
}
export async function getRuntimeConfig(): Promise<RuntimeConfig> {
  if (configCache) return configCache
  if (!pending)
    pending = (async () => {
      const response = await fetch('/api/config', {
        cache: 'no-store',
        signal: AbortSignal.timeout(6000),
      })
      if (!response.ok) throw new Error(`Failed to load configuration (${response.status})`)
      configCache = parseRuntimeConfig(await response.json())
      return configCache
    })().finally(() => {
      pending = null
    })
  return pending
}
