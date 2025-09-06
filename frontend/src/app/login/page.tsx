'use client'

import { useEffect, useState } from 'react'
import ErrorNotification from '../../components/ErrorNotification'

export default function Login() {
  const [loading, setLoading] = useState(true)
  const [loggedIn, setLoggedIn] = useState(false)
  const [errorMessage, setErrorMessage] = useState<string | null>(null)

  useEffect(() => {
    const controller = new AbortController()
    const timeoutId = setTimeout(() => controller.abort(), 6000)

    fetch('/api/user', { credentials: 'include', signal: controller.signal })
      .then((res) => {
        if (res.ok) {
          setLoggedIn(true)
        } else if (res.status === 504) {
          setErrorMessage('Request timed out. Please try again.')
        }
      })
      .catch((err) => {
        if (err.name === 'AbortError') {
          setErrorMessage('Request timed out. Please try again.')
        } else {
          setErrorMessage('Unable to connect to the server. Please check your connection.')
        }
      })
      .finally(() => {
        clearTimeout(timeoutId)
        setLoading(false)
      })
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
      {errorMessage && (
        <ErrorNotification message={errorMessage} onClose={() => setErrorMessage(null)} />
      )}
      <div className="login-card">
        <h1>Artist Insight</h1>
        <p className="tagline">
          Easily fetch your Spotify followings and share them with friends. Start exploring and enjoy the experience!
        </p>
        <a
          className="button"
          href="/api/oauth2/authorization/spotify-artist-insight-service"
        >
          Login with Spotify
        </a>
        <div className="auth-info">
          <p className="redirect-note">
            You may be redirected to Spotify to log in. This app will be able to:
          </p>
          <ul className="scope-list">
            {scopes.map((scope) => (
              <li key={scope}>{scope}</li>
            ))}
          </ul>
        </div>
      </div>
    </div>
  )
}
