import { afterEach, describe, expect, it, vi } from 'vitest'

import {
  deleteAccount,
  deleteShareLink,
  fetchShareLinkStatus,
  requestLogout,
} from '@/features/settings/api'

vi.mock('@/lib/clientApi', () => ({
  fetchWithTimeout: vi.fn(),
}))

import { fetchWithTimeout } from '@/lib/clientApi'

const fetchWithTimeoutMock = vi.mocked(fetchWithTimeout)
const fetchMock = vi.fn<typeof fetch>()

afterEach(() => {
  vi.clearAllMocks()
})

describe('settings api', () => {
  it('returns null when no share link exists', async () => {
    fetchWithTimeoutMock.mockResolvedValueOnce(new Response(null, { status: 404 }))

    await expect(fetchShareLinkStatus()).resolves.toBeNull()
  })

  it('returns share link data when request succeeds', async () => {
    fetchWithTimeoutMock.mockResolvedValueOnce(
      new Response(JSON.stringify({ shareCode: 'abc', expiresAt: '2026-05-01T00:00:00Z' }), { status: 200 }),
    )

    await expect(fetchShareLinkStatus()).resolves.toEqual({
      shareCode: 'abc',
      expiresAt: '2026-05-01T00:00:00Z',
    })
  })

  it('throws when logout request fails', async () => {
    vi.stubGlobal('fetch', fetchMock)
    fetchMock.mockResolvedValueOnce(new Response(null, { status: 500 }))

    await expect(requestLogout('token')).rejects.toThrow('Logout failed')
    vi.unstubAllGlobals()
  })

  it('sends delete share link request with xsrf header', async () => {
    vi.stubGlobal('fetch', fetchMock)
    fetchMock.mockResolvedValueOnce(new Response(null, { status: 204 }))

    await deleteShareLink('token')

    expect(fetchMock).toHaveBeenCalledWith('/api/share', {
      method: 'DELETE',
      credentials: 'include',
      headers: { 'X-XSRF-TOKEN': 'token' },
    })
    vi.unstubAllGlobals()
  })

  it('sends delete account request with xsrf header', async () => {
    vi.stubGlobal('fetch', fetchMock)
    fetchMock.mockResolvedValueOnce(new Response(null, { status: 204 }))

    await deleteAccount('token')

    expect(fetchMock).toHaveBeenCalledWith('/api/user', {
      method: 'DELETE',
      credentials: 'include',
      headers: { 'X-XSRF-TOKEN': 'token' },
    })
    vi.unstubAllGlobals()
  })
})