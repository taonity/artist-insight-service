import { afterEach, describe, expect, it, vi } from 'vitest'

import { checkBackendLiveness, fetchAuthenticatedUserStatus } from '@/features/auth/api'
import { DEFAULT_NETWORK_ERROR_MESSAGE, DEFAULT_TIMEOUT_ERROR_MESSAGE } from '@/lib/clientApi'

vi.mock('@/lib/clientApi', () => ({
  DEFAULT_NETWORK_ERROR_MESSAGE: 'Unable to connect to the server. Please check your connection.',
  DEFAULT_TIMEOUT_ERROR_MESSAGE: 'Request timed out. Please try again.',
  fetchWithTimeout: vi.fn(),
}))

import { fetchWithTimeout } from '@/lib/clientApi'

const fetchWithTimeoutMock = vi.mocked(fetchWithTimeout)

afterEach(() => {
  vi.clearAllMocks()
})

describe('auth api', () => {
  it('returns authenticated status when user endpoint succeeds', async () => {
    fetchWithTimeoutMock.mockResolvedValueOnce(new Response(null, { status: 200 }))

    await expect(fetchAuthenticatedUserStatus()).resolves.toEqual({ status: 'authenticated' })
  })

  it('returns timeout error for 504 user response', async () => {
    fetchWithTimeoutMock.mockResolvedValueOnce(new Response(null, { status: 504 }))

    await expect(fetchAuthenticatedUserStatus()).resolves.toEqual({
      status: 'error',
      message: DEFAULT_TIMEOUT_ERROR_MESSAGE,
    })
  })

  it('returns unauthenticated status for non-ok user response', async () => {
    fetchWithTimeoutMock.mockResolvedValueOnce(new Response(null, { status: 401 }))

    await expect(fetchAuthenticatedUserStatus()).resolves.toEqual({
      status: 'unauthenticated',
      httpStatus: 401,
    })
  })

  it('returns liveness success when backend is up', async () => {
    fetchWithTimeoutMock.mockResolvedValueOnce(
      new Response(JSON.stringify({ status: 'UP' }), { status: 200 }),
    )

    await expect(checkBackendLiveness()).resolves.toEqual({ ok: true })
  })

  it('returns network error when liveness request throws', async () => {
    fetchWithTimeoutMock.mockRejectedValueOnce(new Error('boom'))

    await expect(checkBackendLiveness()).resolves.toEqual({
      ok: false,
      message: DEFAULT_NETWORK_ERROR_MESSAGE,
    })
  })
})