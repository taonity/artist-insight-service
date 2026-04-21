import {
  DEFAULT_NETWORK_ERROR_MESSAGE,
  DEFAULT_TIMEOUT_ERROR_MESSAGE,
  fetchWithTimeout,
} from '@/lib/clientApi'
import type { Advisory } from '@/types/advisory'
import type { EnrichableArtistObject } from '@/types/followings'

interface FollowingsResponse {
  artists: EnrichableArtistObject[]
  advisories: Advisory[]
  gptUsagesLeft?: number
}

interface ErrorData {
  advisories: Advisory[]
}

async function parseAdvisories(res: Response, fallbackErrorMessage: string) {
  try {
    const errorBody = (await res.json()) as ErrorData
    return errorBody.advisories.length > 0
      ? { advisories: errorBody.advisories }
      : { errorMessage: fallbackErrorMessage }
  } catch {
    return { errorMessage: fallbackErrorMessage }
  }
}

export async function fetchFollowings(path: string, timeoutMs: number) {
  const response = await fetchWithTimeout(path, { timeoutMs })

  if (response.status === 504) {
    return {
      ok: false as const,
      ...(await parseAdvisories(response, DEFAULT_TIMEOUT_ERROR_MESSAGE)),
    }
  }

  if (response.status === 500) {
    return {
      ok: false as const,
      ...(await parseAdvisories(response, 'Server error. Please try again later.')),
    }
  }

  if (!response.ok) {
    return {
      ok: false as const,
      errorMessage: DEFAULT_NETWORK_ERROR_MESSAGE,
    }
  }

  return {
    ok: true as const,
    data: (await response.json()) as FollowingsResponse,
  }
}

export async function checkExistingShareLink() {
  try {
    const response = await fetchWithTimeout('/api/share', { timeoutMs: 10000 })
    return response.ok
  } catch {
    return false
  }
}

export async function createShareLink(xsrfToken: string) {
  try {
    const response = await fetchWithTimeout('/api/share', {
      method: 'POST',
      timeoutMs: 15000,
      headers: { 'X-XSRF-TOKEN': xsrfToken },
    })

    if (!response.ok) {
      return {
        ok: false as const,
        message: 'Failed to create share link. Please try again.',
      }
    }

    return {
      ok: true as const,
      data: (await response.json()) as { shareCode: string },
    }
  } catch {
    return {
      ok: false as const,
      message: DEFAULT_NETWORK_ERROR_MESSAGE,
    }
  }
}

export function buildFollowingsCsvData(enrichableArtistObjects: EnrichableArtistObject[]) {
  return [
    ['name', 'genres', 'followers', 'popularity', 'spotify_url'],
    ...enrichableArtistObjects.map(({ artistObject }) => [
      artistObject.name,
      artistObject.genres?.join(', ') ?? '',
      artistObject.followers?.total?.toString() ?? '0',
      artistObject.popularity?.toString() ?? '0',
      artistObject.externalUrls?.spotify ?? '',
    ]),
  ]
}