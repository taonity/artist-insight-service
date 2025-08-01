import { useEffect, useState } from 'react'
import ArtistList, { EnrichableArtistObject } from '../components/ArtistList'
import User from '../models/User'
import keysToCamel from '../utils/utils'
import Image from 'next/image'


const API_BASE = process.env.NEXT_PUBLIC_BACKEND_URL || ''

export default function Home() {
  const [user, setUser] = useState<User | null>(null)
  const [enrichableArtistObjects, setArtists] = useState<EnrichableArtistObject[]>([])

    const fetchUser = async () => {
      const res = await fetch(`${API_BASE}/user`, { credentials: 'include' })
      if (res.status === 401) {
        window.location.href = '/login'
        return
      }
      if (res.ok) {
        const snakeCasedData = await res.json()
        const camelCasedData = keysToCamel(snakeCasedData)
        setUser(camelCasedData)
      } else {
        window.location.href = '/login'
      }
    }

  // runs two times in development mode, but only once in production
  useEffect(() => {
    fetchUser()
  }, [])


  // Helper to get cookie value
  function getCookie(name: string) {
    const match = document.cookie.match(new RegExp('(^| )' + name + '=([^;]+)'))
    return match ? decodeURIComponent(match[2]) : null
  }

  const logout = async () => {
    const xsrfToken = getCookie('XSRF-TOKEN')
    await fetch(`${API_BASE}/logout`, {
      method: 'POST',
      credentials: 'include',
      headers: xsrfToken ? { 'X-XSRF-TOKEN': xsrfToken } : {}
    })
    window.location.href = '/login'
  }

  const loadFollowings = async (enriched: boolean) => {
    const endpoint = enriched
      ? `${API_BASE}/followings/enriched`
      : `${API_BASE}/followings/raw`
    const res = await fetch(endpoint, { credentials: 'include' })
    if (res.ok) {
      const data = await res.json()
      setArtists(data.artists)
      fetchUser()
    }
  }

  if (!user) return null

  return (
    <div>
      <div className="header">
        <div className="user-info">
          {user.privateUserObject.images && user.privateUserObject.images.length > 0 && (
            <Image
                src={user.privateUserObject.images[0].url}
                alt={user.privateUserObject.displayName}
                width={48}
                height={48}
                className="artist-image"
              />
          )}
          <div>Logged in as {user.privateUserObject.displayName}</div>
        </div>
        
        <button onClick={logout}>Logout</button>
      </div>
      <div style={{ padding: '16px' }}>
        <button onClick={() => loadFollowings(false)}>Load Followings</button>
        <button onClick={() => loadFollowings(true)}>
          Load Followings with Genre Enrichement
        </button>
        <span style={{ marginLeft: '16px' }}>
          GPT Usages Left: {user.gptUsagesLeft}
        </span>
        <ArtistList enrichableArtistObjects={enrichableArtistObjects} />
      </div>
    </div>
  )
}
