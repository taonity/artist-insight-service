'use client'

import { useEffect, useState } from 'react'
import ArtistList, { EnrichableArtistObject } from '../components/ArtistList'
import AdvisoryCards, { Advisory } from '../components/AdvisoryCards'
import User from '../models/User'
import keysToCamel from '../utils/utils'
import Image from 'next/image'
import { CSVLink } from 'react-csv'
import GptUsageBlock from '../components/GptUsageBlock'
import Loading from '../components/Loading'
import ErrorNotification from '../components/ErrorNotification'

export default function Home() {
  const [user, setUser] = useState<User | null>(null)
  const [enrichableArtistObjects, setArtists] = useState<EnrichableArtistObject[]>([])
  const [advisories, setAdvisories] = useState<Advisory[]>([])
  const [loading, setLoading] = useState(false)
  const [gptUsagesLeft, setGptUsagesLeft] = useState(0)
  const [errorMessage, setErrorMessage] = useState<string | null>(null)

  const fetchUser = async () => {
    const controller = new AbortController()
    const timeoutId = setTimeout(() => controller.abort(), 6000)
    try {
      const res = await fetch('/api/user', { credentials: 'include', signal: controller.signal })
      if (res.status === 401) {
        window.location.href = '/login'
        return
      }
      if (res.status === 504) {
        setErrorMessage('Request timed out. Please try again.')
        return
      }
      if (res.ok) {
        const snakeCasedUser = await res.json()
        const camelCasedUser = keysToCamel(snakeCasedUser)
        setUser(camelCasedUser)
        setGptUsagesLeft(camelCasedUser.gptUsagesLeft)
      } else {
        window.location.href = '/login'
      }
    } catch (err) {
      if (err instanceof Error && err.name === 'AbortError') {
        setErrorMessage('Request timed out. Please try again.')
      } else {
        setErrorMessage('Unable to connect to the server. Please check your connection.')
      }
    } finally {
      clearTimeout(timeoutId)
    }
  }

  useEffect(() => {
    fetchUser()
  }, [])

  useEffect(() => {
    if (user) {
      loadUserFollowings()
    }
  }, [user])

  function getCookie(name: string) {
    const match = document.cookie.match(new RegExp('(^| )' + name + '=([^;]+)'))
    return match ? decodeURIComponent(match[2]) : null
  }

  const logout = async () => {
    const xsrfToken = getCookie('XSRF-TOKEN')
    await fetch('/api/logout', {
      method: 'POST',
      credentials: 'include',
      headers: xsrfToken ? { 'X-XSRF-TOKEN': xsrfToken } : {},
    })
    window.location.href = '/login'
  }

  const loadUserFollowings = async () => {
    setLoading(true)
    const controller = new AbortController()
    const timeoutId = setTimeout(() => controller.abort(), 6000)
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
      setLoading(false)
    }
  }

  interface ErrorData {
    advisories: Advisory[];
  }

  const loadEnrichedFollowings = async () => {
    setLoading(true)
    const controller = new AbortController()
    const timeoutId = setTimeout(() => controller.abort(), 6000)
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
    } catch (err) {
      if (err instanceof Error && err.name === 'AbortError') {
        setErrorMessage('Request timed out. Please try again.')
      } else {
        setErrorMessage('Unable to connect to the server. Please check your connection.')
      }
    } finally {
      clearTimeout(timeoutId)
      setLoading(false)
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

  if (!user) return <Loading />

  const csvData = [['name', 'genre', 'enriched']]
  enrichableArtistObjects.forEach((enrichableArtistObject) => {
    const artist = enrichableArtistObject.artistObject
    const enriched = enrichableArtistObject.genreEnriched ? 'yes' : 'no'
    if (artist.genres) {
      csvData.push([artist.name, artist.genres.join(', '), enriched])
    } else {
      csvData.push([artist.name, '', enriched])
    }
  })

  return (
    <div>
      {errorMessage && (
        <ErrorNotification message={errorMessage} onClose={() => setErrorMessage(null)} />
      )}
      <div className="header">
        <div className="user-info">
          <Image
            src={
              user.privateUserObject.images && user.privateUserObject.images.length > 0
                ? user.privateUserObject.images[0].url
                : '/default-user-pfp.png'
            }
            alt={user.privateUserObject.displayName}
            width={48}
            height={48}
            className="artist-image"
          />
          <div>Logged in as {user.privateUserObject.displayName}</div>
        </div>

        <button onClick={logout}>Logout</button>
      </div>
      <div style={{ padding: '16px' }}>
        <GptUsageBlock count={gptUsagesLeft} />
        {enrichableArtistObjects.length > 0 && (
          <div className="actions">
            <button onClick={() => loadEnrichedFollowings()} disabled={loading}>
              {loading ? 'Enriching…' : 'Enrich followings'}
            </button>
            <CSVLink data={csvData} filename={'exported-artists.csv'} className="button">
              Download CSV
            </CSVLink>
          </div>
        )}
        <AdvisoryCards advisories={advisories} />
        {loading ? (
          <Loading items={enrichableArtistObjects.length || 10} />
        ) : (
          <ArtistList enrichableArtistObjects={enrichableArtistObjects} />
        )}
      </div>
    </div>
  )
}
