import { fetchWithTimeout } from '@/lib/clientApi'
import type { ShareLink } from '@/types/share'

export async function requestLogout(xsrfToken: string) {
  const response = await fetch('/api/logout', {
    method: 'POST',
    credentials: 'include',
    headers: { 'X-XSRF-TOKEN': xsrfToken },
  })

  if (!response.ok) {
    throw new Error('Logout failed')
  }
}

export async function fetchShareLinkStatus(): Promise<ShareLink | null> {
  const response = await fetchWithTimeout('/api/share', { timeoutMs: 10000 })

  if (response.status === 404) {
    return null
  }

  if (!response.ok) {
    throw new Error(`Failed to load share link status: ${response.status}`)
  }

  const data = await response.json()
  return { shareCode: data.shareCode, expiresAt: data.expiresAt }
}

export async function deleteShareLink(xsrfToken: string) {
  return fetch('/api/share', {
    method: 'DELETE',
    credentials: 'include',
    headers: { 'X-XSRF-TOKEN': xsrfToken },
  })
}

export async function deleteAccount(xsrfToken: string) {
  return fetch('/api/user', {
    method: 'DELETE',
    credentials: 'include',
    headers: { 'X-XSRF-TOKEN': xsrfToken },
  })
}