'use client'

import { useEffect, useState } from 'react'
import { CSVLink } from 'react-csv'
import AdvisoryCards from '@/features/followings/components/AdvisoryCards'
import ArtistList from '@/features/followings/components/ArtistList'
import GptUsageBlock from '@/features/followings/components/GptUsageBlock'
import ErrorNotification from '@/components/feedback/ErrorNotification'
import Loading from '@/components/feedback/Loading'
import Header from '@/components/layout/Header'
import { useUser } from '@/hooks/useUser'
import { getCookie } from '@/lib/cookies'
import {
  fetchWithTimeout,
  DEFAULT_NETWORK_ERROR_MESSAGE,
  DEFAULT_TIMEOUT_ERROR_MESSAGE,
} from '@/lib/clientApi'
import { getRuntimeConfig } from '@/lib/runtimeConfig'
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

async function parseAdvisoriesOrThrow(res: Response, fallbackErrorMessage: string) {
  try {
    const errorBody = (await res.json()) as ErrorData
    return errorBody.advisories.length > 0
      ? { advisories: errorBody.advisories }
      : { errorMessage: fallbackErrorMessage }
  } catch {
    return { errorMessage: fallbackErrorMessage }
  }
}

async function fetchFollowings(path: string, timeoutMs: number) {
  const response = await fetchWithTimeout(path, { timeoutMs })

  if (response.status === 504) {
    return {
      ok: false as const,
      ...(await parseAdvisoriesOrThrow(response, DEFAULT_TIMEOUT_ERROR_MESSAGE)),
    }
  }

  if (response.status === 500) {
    return {
      ok: false as const,
      ...(await parseAdvisoriesOrThrow(response, 'Server error. Please try again later.')),
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

export default function Home() {
  const [enrichableArtistObjects, setArtists] = useState<EnrichableArtistObject[]>([])
  const [advisories, setAdvisories] = useState<Advisory[]>([])
  const [loading, setLoading] = useState(true)
  const [enriching, setEnriching] = useState(false)
  const [gptUsagesLeft, setGptUsagesLeft] = useState(0)
  const [errorMessage, setErrorMessage] = useState<string | null>(null)
  const [shareLoading, setShareLoading] = useState(false)
  const [shareCopiedNotification, setShareCopiedNotification] = useState(false)
  const [hasExistingShareLink, setHasExistingShareLink] = useState(false)
  const [csrfCookieName, setCsrfCookieName] = useState('XSRF-TOKEN')

  const user = useUser({ onError: setErrorMessage })

  useEffect(() => {
    void getRuntimeConfig().then((config) => setCsrfCookieName(config.csrfCookieName))
  }, [])

  useEffect(() => {
    if (!user) {
      return
    }

    setGptUsagesLeft(user.gptUsagesLeft)
    void loadUserFollowings()
    void checkExistingShareLink()
  }, [user])

  const loadUserFollowings = async () => {
    setLoading(true)

    try {
      const result = await fetchFollowings('/api/followings', 30000)

      if (!result.ok) {
        if (result.advisories) {
          setAdvisories(result.advisories)
        } else if (result.errorMessage) {
          setErrorMessage(result.errorMessage)
        }
        return
      }

      setArtists(result.data.artists)
      setAdvisories(result.data.advisories)
    } catch {
      setErrorMessage(DEFAULT_NETWORK_ERROR_MESSAGE)
    } finally {
      setLoading(false)
    }
  }

  const loadEnrichedFollowings = async () => {
    setEnriching(true)

    try {
      const result = await fetchFollowings('/api/followings/enriched', 60000)

      if (!result.ok) {
        if (result.advisories) {
          setAdvisories(result.advisories)
        } else if (result.errorMessage) {
          setErrorMessage(result.errorMessage)
        }
        return
      }

      setArtists(result.data.artists)
      setAdvisories(result.data.advisories)
      setGptUsagesLeft(result.data.gptUsagesLeft ?? 0)
    } catch {
      setErrorMessage(DEFAULT_NETWORK_ERROR_MESSAGE)
    } finally {
      setEnriching(false)
    }
  }

  const checkExistingShareLink = async () => {
    try {
      const res = await fetchWithTimeout('/api/share', { timeoutMs: 10000 })
      if (res.ok) {
        setHasExistingShareLink(true)
      }
    } catch {
      // Ignore errors - just means no existing share link
    }
  }

  const handleShare = async () => {
    const xsrfToken = getCookie(csrfCookieName)
    if (!xsrfToken) {
      setErrorMessage('Unable to verify your request. Please refresh the page and try again.')
      return
    }

    setShareLoading(true)
    try {
      const res = await fetchWithTimeout('/api/share', {
        method: 'POST',
        timeoutMs: 15000,
        headers: { 'X-XSRF-TOKEN': xsrfToken },
      })

      if (res.ok) {
        const data = await res.json()
        const fullUrl = `${window.location.origin}/share/${data.shareCode}`
        setHasExistingShareLink(true)

        try {
          await navigator.clipboard.writeText(fullUrl)
          setShareCopiedNotification(true)
          setTimeout(() => setShareCopiedNotification(false), 8000)
        } catch {
          setErrorMessage('Failed to copy link to clipboard.')
        }
      } else {
        setErrorMessage('Failed to create share link. Please try again.')
      }
    } catch {
      setErrorMessage(DEFAULT_NETWORK_ERROR_MESSAGE)
    } finally {
      setShareLoading(false)
    }
  }

  const csvData = [
    ['name', 'genres', 'followers', 'popularity', 'spotify_url'],
    ...enrichableArtistObjects.map(({ artistObject }) => [
      artistObject.name,
      artistObject.genres?.join(', ') ?? '',
      artistObject.followers?.total?.toString() ?? '0',
      artistObject.popularity?.toString() ?? '0',
      artistObject.externalUrls?.spotify ?? '',
    ]),
  ]

  return (
    <div>
      {errorMessage && (
        <ErrorNotification message={errorMessage} onClose={() => setErrorMessage(null)} />
      )}
      <Header user={user} />
      <div className="main-content">
        {loading ? (
          <Loading items={enrichableArtistObjects.length || 10} />
        ) : (
          <>
            {enriching ? (
              <div className="gpt-usage skeleton-gpt-usage">
                <div className="skeleton-line" style={{ width: 140, height: 18.9 }} />
              </div>
            ) : (
              <GptUsageBlock count={gptUsagesLeft} />
            )}
            {enrichableArtistObjects.length > 0 && (
              <div className="actions">
                <button type="button" onClick={loadEnrichedFollowings} disabled={enriching}>
                  {enriching ? 'Enriching…' : 'Enrich followings'}
                </button>
                <CSVLink data={csvData} filename={'exported-artists.csv'} className="button">
                  Download CSV
                </CSVLink>
                <button type="button" onClick={handleShare} disabled={shareLoading || enriching}>
                  {shareLoading ? 'Generating…' : hasExistingShareLink ? 'Update Share Link' : 'Share'}
                </button>
              </div>
            )}
            {shareCopiedNotification && <div className="share-copied-notification">Link copied!</div>}
            <AdvisoryCards advisories={advisories} />
            {enriching ? (
              <Loading items={enrichableArtistObjects.length || 10} tableOnly />
            ) : (
              <ArtistList enrichableArtistObjects={enrichableArtistObjects} />
            )}
          </>
        )}
      </div>
    </div>
  )
}