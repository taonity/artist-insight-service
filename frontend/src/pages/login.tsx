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

  return (
    <div style={{ padding: '32px' }}>
      <h1>Login</h1>
      <a href={`${API_BASE}/oauth2/authorization/spotify-artist-insight-service`}>
        <button>Login with Spotify</button>
      </a>
    </div>
  )
}
