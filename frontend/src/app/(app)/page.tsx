'use client'

import { useEffect, useState } from 'react'
import { CSVLink } from 'react-csv'
import { AdvisoryCards, ArtistList, GptUsageBlock } from '@/features/followings/components'
import ErrorNotification from '@/components/feedback/ErrorNotification'
import Loading from '@/components/feedback/Loading'
import Header from '@/components/layout/Header'
import {
  buildFollowingsCsvData,
  checkExistingShareLink,
  createShareLink,
  fetchFollowings,
} from '@/features/followings/api'
import { useUser } from '@/hooks/useUser'
import { getCookie } from '@/lib/cookies'
import { DEFAULT_NETWORK_ERROR_MESSAGE } from '@/lib/clientApi'
import { getRuntimeConfig } from '@/lib/runtimeConfig'
import type { Advisory } from '@/types/advisory'
import type { EnrichableArtistObject } from '@/types/followings'

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
    void loadExistingShareLinkState()
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

  const loadExistingShareLinkState = async () => {
    setHasExistingShareLink(await checkExistingShareLink())
  }

  const handleShare = async () => {
    const xsrfToken = getCookie(csrfCookieName)
    if (!xsrfToken) {
      setErrorMessage('Unable to verify your request. Please refresh the page and try again.')
      return
    }

    setShareLoading(true)
    const result = await createShareLink(xsrfToken)

    try {
      if (result.ok) {
        const fullUrl = `${window.location.origin}/share/${result.data.shareCode}`
        setHasExistingShareLink(true)

        try {
          await navigator.clipboard.writeText(fullUrl)
          setShareCopiedNotification(true)
          setTimeout(() => setShareCopiedNotification(false), 8000)
        } catch {
          setErrorMessage('Failed to copy link to clipboard.')
        }
      } else {
        setErrorMessage(result.message)
      }
    } finally {
      setShareLoading(false)
    }
  }

  const csvData = buildFollowingsCsvData(enrichableArtistObjects)

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