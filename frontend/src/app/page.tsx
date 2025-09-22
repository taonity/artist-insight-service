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

export default function Home() {
  const [enrichableArtistObjects, setArtists] = useState<EnrichableArtistObject[]>([])
  const [advisories, setAdvisories] = useState<Advisory[]>([])
  const [loading, setLoading] = useState(false)
  const [gptUsagesLeft, setGptUsagesLeft] = useState(0)
  const [errorMessage, setErrorMessage] = useState<string | null>(null)

  const user = useUser(setErrorMessage);

  useEffect(() => {
    if (user) {
      setGptUsagesLeft(user.gptUsagesLeft);

      loadUserFollowings()
    }
  }, [user])

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
      <Header user={user}/>
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
