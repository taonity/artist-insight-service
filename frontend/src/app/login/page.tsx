'use client'

import { useEffect, useState, Suspense } from 'react'
import { useRouter } from 'next/navigation'
import { Righteous } from 'next/font/google'
import Image from 'next/image'
import ErrorNotification from '../../components/ErrorNotification'
import BackgroundPhrases from '../../components/BackgroundPhrases'
import DevelopmentAccessNotification from '../../components/DevelopmentAccessNotification'
import { deleteCookie, getCookie } from '@/lib/cookies'
import { fetchWithTimeout, DEFAULT_NETWORK_ERROR_MESSAGE, DEFAULT_TIMEOUT_ERROR_MESSAGE } from '@/lib/clientApi'
import { logError, logDebug } from '@/utils/logger'

const righteous = Righteous({ 
  weight: '400',
  subsets: ['latin'],
})

function LoginContent() {
  const router = useRouter()
  const [livenessLoading, setLivenessLoading] = useState(false)
  const [errorMessage, setErrorMessage] = useState<string | null>(null)
  const [showAccessRequestForm, setShowAccessRequestForm] = useState(false)
  const [genreDescription, setGenreDescription] = useState<string | null>(null)

  useEffect(() => {
    // Check for error code from OAuth2 authentication failure (stored in cookie)
    const authErrorCode = getCookie('auth_error')
    logDebug('LoginPage', 'authErrorCode is ' + authErrorCode)
    if (authErrorCode) {
      deleteCookie('auth_error') // Remove cookie after reading
      
      if (authErrorCode === 'UNAUTHORIZED_SPOTIFY_ACCOUNT') {
        logError('LoginPage', 'Unauthorized Spotify account')
        setShowAccessRequestForm(true)
      } else if (authErrorCode === 'AUTHENTICATION_FAILED') {
        logError('LoginPage', 'Authentication failed')
        setErrorMessage('Authentication failed. Please try again.')
      }
      return
    }

    async function loadCurrentUser() {
      try {
        const response = await fetchWithTimeout('/api/user', { timeoutMs: 6000 })

        if (response.ok) {
          router.replace('/')
          return
        }

        if (response.status === 504) {
          logError('LoginPage', 'User fetch timed out with 504 status')
          setErrorMessage(DEFAULT_TIMEOUT_ERROR_MESSAGE)
          return
        }

        logError('LoginPage', 'User fetch failed with status: ' + response.status)
      } catch (error) {
        logError('LoginPage', 'User fetch network error', error)
        setErrorMessage(DEFAULT_NETWORK_ERROR_MESSAGE)
      }
    }

    void loadCurrentUser()
  }, [router])

  const scopes = [
    'Read your private profile information',
    'See your followed artists',
  ]

  const handleLogin = async (e: React.MouseEvent) => {
    e.preventDefault()
    setLivenessLoading(true)
    setErrorMessage(null)

    try {
      const response = await fetchWithTimeout('/api/actuator/health/liveness', { timeoutMs: 6000 })

      if (response.status === 504) {
        logError('LoginPage', 'Liveness check timed out')
        setErrorMessage(DEFAULT_TIMEOUT_ERROR_MESSAGE)
        return
      }

      const data = await response.json()
      if (!response.ok || data.status !== 'UP') {
        logError('LoginPage', 'Liveness check failed', { status: response.status, data })
        setErrorMessage('Backend server is currently unavailable. Please try again later.')
        return
      }
      window.location.assign('/api/oauth2/authorization/spotify-artist-insight-service')
    } catch (error) {
      logError('LoginPage', 'Liveness check network error', error)
      setErrorMessage(DEFAULT_NETWORK_ERROR_MESSAGE)
    } finally {
      setLivenessLoading(false)
    }
  }

  return (
    <div className="login-container">
      <BackgroundPhrases onGenreHover={setGenreDescription} />
      {errorMessage && (
        <ErrorNotification message={errorMessage} onClose={() => setErrorMessage(null)} />
      )}
      {showAccessRequestForm && (
        <DevelopmentAccessNotification 
          message="Your Spotify account is not authorized to use this application. Please request development access." 
          onClose={() => setShowAccessRequestForm(false)}
        />
      )}
      <h1 className={`login-logo ${righteous.className}`}>Artist Insight</h1>
      <button
        className="button login-button"
        onClick={handleLogin}
        disabled={livenessLoading}
      >
        {!livenessLoading && (
          <Image 
            src="/spotify-logo-white-transparent.png" 
            alt="Spotify" 
            width={24} 
            height={24}
            className="button-logo"
          />
        )}
        {livenessLoading ? 'Checking server...' : 'Continue with Spotify'}
      </button>
      {genreDescription ? (
        <div key="description" className="login-features genre-description-box">
          <p>{genreDescription}</p>
        </div>
      ) : (
        <div key="features" className="login-features">
          <p>See your followings genres</p>
          <p>Discover new genres for your followings</p>
          <p>Share your followings with friends</p>
          <p>
            See the development <a href="https://github.com/taonity/artist-insight-service?tab=readme-ov-file#roadmap" target="_blank" rel="noopener noreferrer">roadmap</a> and suggest your ideas
          </p>
        </div>
      )}
    </div>
  )
}

export default function Login() {
  return (
    <Suspense fallback={<div className="login-container"><div>Loading...</div></div>}>
      <LoginContent />
    </Suspense>
  )
}
