'use client'

import { useEffect, useState } from 'react'
import ArtistList, { EnrichableArtistObject } from '../components/ArtistList'
import AdvisoryCards, { Advisory } from '../components/AdvisoryCards'
import { CSVLink } from 'react-csv'
import GptUsageBlock from '../components/GptUsageBlock'
import Loading from '../components/Loading'
import ErrorNotification from '../components/ErrorNotification'
import Header from '@/components/Header'
import { useUser } from "../hooks/useUser"
import { getRuntimeConfig } from '@/lib/runtimeConfig'

function getCookie(name: string) {
  if (typeof document === 'undefined') {
    return null
  }
  const match = document.cookie.match(new RegExp('(^| )' + name + '=([^;]+)'))
  return match ? decodeURIComponent(match[2]) : null
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

  const user = useUser(setErrorMessage);

  useEffect(() => {
    getRuntimeConfig().then(config => setCsrfCookieName(config.csrfCookieName))
  }, [])

  useEffect(() => {
    if (user) {
      setGptUsagesLeft(user.gptUsagesLeft);
      loadUserFollowings()
      checkExistingShareLink()
    }
  }, [user])

  const loadUserFollowings = async () => {
    setLoading(true)
    const controller = new AbortController()
    const timeoutId = setTimeout(() => controller.abort(), 30000)
    try {
      const res = await fetch('/api/followings', { credentials: 'include', signal: controller.signal })
      if (res.status === 504) {
        await setAdvisoriesOrSetErrorMessage(res, 'Request timed out. Please try again.')
      } else if (res.status == 500) {
        await setAdvisoriesOrSetErrorMessage(res, 'Server error. Please try again later.')
      } else if (res.ok) {
        const jsonResponse = await res.json()
        setArtists(jsonResponse.artists)
        setAdvisories(jsonResponse.advisories)
      }
    } catch (err) {
      if (err instanceof Error && err.name === 'AbortError') {
        setErrorMessage('Request timed out. Please try again.')
      } else {
        setErrorMessage('Unable to connect to the server. Please check your connection.')
      }
    } finally {
      clearTimeout(timeoutId)
      // await new Promise(resolve => setTimeout(resolve, 100000))
      setLoading(false)
    }
  }

  interface ErrorData {
    advisories: Advisory[];
  }

  const loadEnrichedFollowings = async () => {
    setEnriching(true)
    const controller = new AbortController()
    const timeoutId = setTimeout(() => controller.abort(), 60000)
    try {
      const res = await fetch('/api/followings/enriched', { credentials: 'include', signal: controller.signal })
      if (res.status === 504) {
        await setAdvisoriesOrSetErrorMessage(res, 'Request timed out. Please try again.')
      } else if (res.status == 500) {
        await setAdvisoriesOrSetErrorMessage(res, 'Server error. Please try again later.')
      } else if (res.ok) {
        const jsonResponse = await res.json()
        setArtists(jsonResponse.artists)
        setAdvisories(jsonResponse.advisories)
        setGptUsagesLeft(jsonResponse.gptUsagesLeft)
      }
      // TODO: test AbortError
    } catch (err) {
      if (err instanceof Error && err.name === 'AbortError') {
        setErrorMessage('Request timed out. Please try again.')
      } else {
        setErrorMessage('Unable to connect to the server. Please check your connection.')
      }
    } finally {
      clearTimeout(timeoutId)
      setEnriching(false)
    }
  }

    async function setAdvisoriesOrSetErrorMessage(res: Response, errorMessage: string) {
      try {
        let errorBody: ErrorData = await res.json()
        if (errorBody.advisories.length == 0) {
          setErrorMessage(errorMessage)
        } else {
          setAdvisories(errorBody.advisories)
        }
      } catch {
        setErrorMessage(errorMessage)
      }
    }

  const checkExistingShareLink = async () => {
    try {
      const res = await fetch('/api/share', { credentials: 'include' })
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
      const res = await fetch('/api/share', {
        method: 'POST',
        credentials: 'include',
        headers: { 'X-XSRF-TOKEN': xsrfToken },
      })
      
      if (res.ok) {
        const data = await res.json()
        const fullUrl = `${window.location.origin}/share/${data.shareCode}`
        setHasExistingShareLink(true)
        
        // Copy to clipboard
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
      setErrorMessage('Unable to connect to the server. Please check your connection.')
    } finally {
      setShareLoading(false)
    }
  }

  const csvData = [['name', 'genres', 'followers', 'popularity', 'spotify_url']]
  enrichableArtistObjects.forEach((enrichableArtistObject) => {
    const artist = enrichableArtistObject.artistObject
    csvData.push([
      artist.name,
      artist.genres ? artist.genres.join(', ') : '',
      artist.followers?.total?.toString() ?? '0',
      artist.popularity?.toString() ?? '0',
      artist.externalUrls?.spotify ?? ''
    ])
  })

  return (
    <div>
      {errorMessage && (
        <ErrorNotification message={errorMessage} onClose={() => setErrorMessage(null)} />
      )}
      <Header user={user}/>
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
                <button onClick={() => loadEnrichedFollowings()} disabled={enriching}>
                  {enriching ? 'Enriching…' : 'Enrich followings'}
                </button>
                <CSVLink data={csvData} filename={'exported-artists.csv'} className="button">
                  Download CSV
                </CSVLink>
                <button onClick={handleShare} disabled={shareLoading || enriching}>
                  {shareLoading ? 'Generating…' : hasExistingShareLink ? 'Update Share Link' : 'Share'}
                </button>
              </div>
            )}
            {shareCopiedNotification && (
              <div className="share-copied-notification">
                Link copied!
              </div>
            )}
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
