import { useEffect, useState } from 'react'
import ArtistList, { EnrichableArtistObject } from '../components/ArtistList'
import AdvisoryCards, { Advisory } from '../components/Advisary'
import User from '../models/User'
import keysToCamel from '../utils/utils'
import Image from 'next/image'
import { CSVLink, CSVDownload } from "react-csv";


const API_BASE = process.env.NEXT_PUBLIC_BACKEND_URL || ''

export default function Home() {
  const [user, setUser] = useState<User | null>(null)
  const [enrichableArtistObjects, setArtists] = useState<EnrichableArtistObject[]>([])
  const [advisories, setAdvisories] = useState<Advisory[]>([])

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


  useEffect(() => {
    if (user) {
      loadFollowings(false)
    }
  }, [user])


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
      : `${API_BASE}/followings`
    const res = await fetch(endpoint, { credentials: 'include' })
    if (res.ok) {
      const jsonResponse = await res.json()
      setArtists(jsonResponse.artists)
      setAdvisories(jsonResponse.advisories)
      // fetchUser() removed to prevent infinite loop
    }
  }

  if (!user) return null

  const csvData = [
    ["name", "genre", "enriched"]
  ];

  enrichableArtistObjects.forEach((enrichableArtistObject) => {
    const artist = enrichableArtistObject.artistObject;
    const enriched = enrichableArtistObject.genreEnriched ? "yes" : "no";
    if (artist.genres) {
      csvData.push([artist.name, artist.genres.join(", "), enriched]);
    } else {
      csvData.push([artist.name, "", enriched],);
    }   
  });

  return (
    <div>
      <div className="header">
        <div className="user-info">
          <Image
            src={
              user.privateUserObject.images && user.privateUserObject.images.length > 0
                ? user.privateUserObject.images[0].url
                : "/default-user-pfp.png"
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
        <span style={{ marginLeft: '16px' }}>
          GPT Usages Left: {user.gptUsagesLeft}
        </span>
        {enrichableArtistObjects.length > 0 && (
          <div style={{ marginTop: '16px' }}>
            <button onClick={() => loadFollowings(true)}>
              Enrich followings
            </button>
            <CSVLink data={csvData} filename={"exported-artists.csv"} className="btn btn-primary">
              Download CSV
            </CSVLink>
          </div>  
        )}
        <AdvisoryCards advisories={advisories} />
        <ArtistList enrichableArtistObjects={enrichableArtistObjects} />
      </div>
    </div>
  )
}
