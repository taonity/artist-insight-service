import { describe, expect, it, vi } from 'vitest'

import { fetchSharedArtists } from '@/features/share/api'

const fetchMock = vi.fn<typeof fetch>()

describe('share api', () => {
  it('returns not-found state for 404 response', async () => {
    vi.stubGlobal('fetch', fetchMock)
    fetchMock.mockResolvedValueOnce(new Response(null, { status: 404 }))

    await expect(fetchSharedArtists('missing')).resolves.toEqual({ status: 'not-found' })
    vi.unstubAllGlobals()
  })

  it('returns expired state for 410 response', async () => {
    vi.stubGlobal('fetch', fetchMock)
    fetchMock.mockResolvedValueOnce(new Response(null, { status: 410 }))

    await expect(fetchSharedArtists('expired')).resolves.toEqual({ status: 'expired' })
    vi.unstubAllGlobals()
  })

  it('returns share payload when response succeeds', async () => {
    vi.stubGlobal('fetch', fetchMock)
    fetchMock.mockResolvedValueOnce(
      new Response(
        JSON.stringify({
          owner: { displayName: 'Alice', avatarUrl: null },
          artists: [],
        }),
        { status: 200 },
      ),
    )

    await expect(fetchSharedArtists('ok')).resolves.toEqual({
      status: 'ok',
      data: {
        owner: { displayName: 'Alice', avatarUrl: null },
        artists: [],
      },
    })
    vi.unstubAllGlobals()
  })
})