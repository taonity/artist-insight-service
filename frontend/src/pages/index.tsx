import { useEffect, useState } from 'react'
import ArtistList, { EnrichableArtistObject } from '../components/ArtistList'
import AdvisoryCards, { Advisory } from '../components/AdvisoryCards'
import User from '../models/User'
import keysToCamel from '../utils/utils'
import Image from 'next/image'
import { CSVLink } from 'react-csv'
import GptUsageBlock from '../components/GptUsageBlock'
import Loading from '../components/Loading'
import { set } from 'lodash'

const API_BASE = process.env.NEXT_PUBLIC_BACKEND_URL || ''

export default function Home() {
  const [user, setUser] = useState<User | null>(null)
  const [enrichableArtistObjects, setArtists] = useState<EnrichableArtistObject[]>([])
  const [advisories, setAdvisories] = useState<Advisory[]>([])
  const [loading, setLoading] = useState(false)
  const [gptUsagesLeft, setGptUsagesLeft] = useState(0)

  const fetchUser = async () => {
    const res = await fetch(`${API_BASE}/user`, { credentials: 'include' })
    if (res.status === 401) {
      window.location.href = '/login'
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
    await fetch(`${API_BASE}/logout`, {
      method: 'POST',
      credentials: 'include',
      headers: xsrfToken ? { 'X-XSRF-TOKEN': xsrfToken } : {},
    })
    window.location.href = '/login'
  }

  const loadUserFollowings = async () => {
    setLoading(true)
    const endpoint = `${API_BASE}/followings`
    const res = await fetch(endpoint, { credentials: 'include' })
    if (res.ok) {
      const jsonResponse = await res.json()
      setArtists(jsonResponse.artists)
      setAdvisories(jsonResponse.advisories)
    }
    setLoading(false)
  }

  const loadEnrichedFollowings = async () => {
    setLoading(true)
    const endpoint = `${API_BASE}/followings/enriched`

    const res = await fetch(endpoint, { credentials: 'include' })
    if (res.ok) {
      const jsonResponse = await res.json()
      setArtists(jsonResponse.artists)
      setAdvisories(jsonResponse.advisories)
      setGptUsagesLeft(jsonResponse.gptUsagesLeft)
    }
    setLoading(false)
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
            <CSVLink
              data={csvData}
              filename={"exported-artists.csv"}
              className="button"
            >
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
