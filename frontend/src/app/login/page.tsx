'use client'

import { useEffect, useState } from 'react'
import ErrorNotification from '../../components/ErrorNotification'

export default function Login() {
  const [livenessLoading, setLivenessLoading] = useState(false)
  const [errorMessage, setErrorMessage] = useState<string | null>(null)

  useEffect(() => {
    const controller = new AbortController()
    const timeoutId = setTimeout(() => controller.abort(), 6000)

    fetch('/api/user', { credentials: 'include', signal: controller.signal })
      .then((res) => {
        if (res.ok) {
          if (typeof window !== 'undefined') {
            window.location.href = '/'
          }
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
      })
  }, [])

  const scopes = [
    'Read your private profile information',
    'See your followed artists',
  ]

  const handleLogin = async (e: React.MouseEvent) => {
    e.preventDefault()
    setLivenessLoading(true)
    setErrorMessage(null)

    const controller = new AbortController()
    const timeoutId = setTimeout(() => controller.abort(), 6000)
    try {
      const response = await fetch('/api/actuator/health/liveness', { signal: controller.signal })
      const data = await response.json()
      if (!response.ok || data.status !== 'UP') {
        setErrorMessage('Backend server is currently unavailable. Please try again later.')
        setLivenessLoading(false)
        return
      }
      window.location.href = '/api/oauth2/authorization/spotify-artist-insight-service'
    } catch (err: any) {
      if (err.name === 'AbortError') {
        setErrorMessage('Request timed out. Please try again.')
      } else {
        setErrorMessage('Unable to connect to the server. Please check your connection.')
      }
    } finally {
      clearTimeout(timeoutId)
      setLivenessLoading(false)
    }
  }

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
        <button
          className="button"
          onClick={handleLogin}
          disabled={livenessLoading}
        >
          {livenessLoading ? 'Checking server...' : 'Login with Spotify'}
        </button>
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
