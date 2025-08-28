import { useEffect, useState } from 'react'

const API_BASE = process.env.NEXT_PUBLIC_BACKEND_URL || ''

export default function Login() {
  const [loading, setLoading] = useState(true)
  const [loggedIn, setLoggedIn] = useState(false)


  // runs two times in development mode, but only once in production
  useEffect(() => {
    fetch(`${API_BASE}/user`, { credentials: 'include' })
      .then((res) => {
        if (res.ok) {
          setLoggedIn(true)
        }
      })
      .finally(() => setLoading(false))
  }, [])

  if (loading) return null
  if (loggedIn) {
    if (typeof window !== 'undefined') {
      window.location.href = '/'
    }
    return null
  }

  const scopes = [
    'Read your private profile information',
    'See your followed artists',
  ]

  return (
    <div className="login-container">
      <h1>Artist Insight</h1>
      <p className="tagline">
        Easily fetch your Spotify followings and share them with friends. Start exploring and enjoy the experience!
      </p>
      <p>You may be redirected to Spotify to log in. This app will be able to:</p>
      <ul className="scope-list">
        {scopes.map((scope) => (
          <li key={scope}>{scope}</li>
        ))}
      </ul>
      <a
        className="button"
        href={`${API_BASE}/oauth2/authorization/spotify-artist-insight-service`}
      >
        Login with Spotify
      </a>
    </div>
  )
}
