import { afterEach, describe, expect, it, vi } from 'vitest'

import {
  buildFollowingsCsvData,
  checkExistingShareLink,
  createShareLink,
  fetchFollowings,
} from '@/features/followings/api'
import { DEFAULT_NETWORK_ERROR_MESSAGE, DEFAULT_TIMEOUT_ERROR_MESSAGE } from '@/lib/clientApi'
import type { EnrichableArtistObject } from '@/types/followings'

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

describe('followings api', () => {
  it('returns followings payload on success', async () => {
    fetchWithTimeoutMock.mockResolvedValueOnce(
      new Response(JSON.stringify({ artists: [], advisories: [], gptUsagesLeft: 3 }), { status: 200 }),
    )

    await expect(fetchFollowings('/api/followings', 30000)).resolves.toEqual({
      ok: true,
      data: { artists: [], advisories: [], gptUsagesLeft: 3 },
    })
  })

  it('returns advisories for timeout responses with advisory body', async () => {
    fetchWithTimeoutMock.mockResolvedValueOnce(
      new Response(JSON.stringify({ advisories: [{ code: 'T', title: 'Timeout', detail: 'slow', severity: 'WARNING' }] }), { status: 504 }),
    )

    await expect(fetchFollowings('/api/followings', 30000)).resolves.toEqual({
      ok: false,
      advisories: [{ code: 'T', title: 'Timeout', detail: 'slow', severity: 'WARNING' }],
    })
  })

  it('returns timeout fallback when timeout response has no advisories', async () => {
    fetchWithTimeoutMock.mockResolvedValueOnce(
      new Response(JSON.stringify({ advisories: [] }), { status: 504 }),
    )

    await expect(fetchFollowings('/api/followings', 30000)).resolves.toEqual({
      ok: false,
      errorMessage: DEFAULT_TIMEOUT_ERROR_MESSAGE,
    })
  })

  it('returns share code after creating share link', async () => {
    fetchWithTimeoutMock.mockResolvedValueOnce(
      new Response(JSON.stringify({ shareCode: 'abc123' }), { status: 200 }),
    )

    await expect(createShareLink('token')).resolves.toEqual({
      ok: true,
      data: { shareCode: 'abc123' },
    })
  })

  it('returns false when checking share link throws', async () => {
    fetchWithTimeoutMock.mockRejectedValueOnce(new Error('boom'))

    await expect(checkExistingShareLink()).resolves.toBe(false)
  })

  it('builds csv export rows from artist objects', () => {
    const artists: EnrichableArtistObject[] = [
      {
        artistObject: {
          id: '1',
          name: 'Artist',
          genres: ['rock'],
          externalUrls: { spotify: 'https://spotify.test/artist/1' },
          followers: { total: 99 },
          popularity: 75,
        },
        genreEnriched: false,
      },
    ]

    expect(buildFollowingsCsvData(artists)).toEqual([
      ['name', 'genres', 'followers', 'popularity', 'spotify_url'],
      ['Artist', 'rock', '99', '75', 'https://spotify.test/artist/1'],
    ])
  })

  it('returns network error when creating share link throws', async () => {
    fetchWithTimeoutMock.mockRejectedValueOnce(new Error('boom'))

    await expect(createShareLink('token')).resolves.toEqual({
      ok: false,
      message: DEFAULT_NETWORK_ERROR_MESSAGE,
    })
  })
})