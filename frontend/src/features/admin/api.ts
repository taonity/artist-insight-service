import { fetchWithTimeout } from '@/lib/clientApi'
import { getRuntimeConfig } from '@/lib/runtimeConfig'
import { getCookie } from '@/lib/cookies'
export type Role = 'NONE' | 'ADMIN' | 'OWNER'
export interface Access {
  spotifyId: string
  role: Role
  isAdmin: boolean
  isOwner: boolean
}
export interface Page<T> {
  content: T[]
  totalElements: number
  totalPages: number
  number: number
  size: number
}
export interface UserRow {
  spotifyId: string
  displayName: string
  gptUsagesLeft: number
  role: Role
  avatarUrl: string | null
}
export interface Setting {
  key: string
  label: string
  type: string
  value: string | number
  defaultValue: string | number
  overridden: boolean
  min: string | null
  max: string | null
}
export async function adminRequest<T>(path: string, method = 'GET', body?: unknown): Promise<T> {
  const headers: Record<string, string> = {}
  if (method !== 'GET') {
    const config = await getRuntimeConfig()
    const token = getCookie(config.csrfCookieName)
    if (!token) throw new Error('Unable to verify your request. Please refresh.')
    headers['X-XSRF-TOKEN'] = token
    if (body !== undefined) headers['Content-Type'] = 'application/json'
  }
  const response = await fetchWithTimeout(`/api/console${path}`, {
    method,
    headers,
    body: body === undefined ? undefined : JSON.stringify(body),
    timeoutMs: method === 'GET' ? 60000 : 10000,
    cache: 'no-store',
  })
  if (response.status === 401) {
    window.location.assign('/login')
    throw new Error('Please log in again.')
  }
  if (!response.ok) {
    const error = await response.json().catch(() => null)
    throw new Error(error?.errorMessage || `Request failed (${response.status})`)
  }
  return response.json() as Promise<T>
}
