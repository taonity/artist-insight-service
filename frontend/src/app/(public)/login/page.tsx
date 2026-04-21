'use client'

import { useEffect, useState, Suspense } from 'react'
import { useRouter } from 'next/navigation'
import { Righteous } from 'next/font/google'
import Image from 'next/image'
import DevelopmentAccessNotification from '@/components/feedback/DevelopmentAccessNotification'
import ErrorNotification from '@/components/feedback/ErrorNotification'
import BackgroundPhrases from '@/components/marketing/BackgroundPhrases'
import { checkBackendLiveness, fetchAuthenticatedUserStatus } from '@/features/auth/api'
import { deleteCookie, getCookie } from '@/lib/cookies'
import { logError, logDebug } from '@/lib/logger'

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
    const authErrorCode = getCookie('auth_error')
    logDebug('LoginPage', `authErrorCode is ${authErrorCode}`)
    if (authErrorCode) {
      deleteCookie('auth_error')

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
      const result = await fetchAuthenticatedUserStatus()

      if (result.status === 'authenticated') {
        router.replace('/')
        return
      }

      if (result.status === 'error') {
        logError('LoginPage', 'User fetch failed', result)
        setErrorMessage(result.message)
        return
      }

      logError('LoginPage', `User fetch failed with status: ${result.httpStatus}`)
    }

    void loadCurrentUser()
  }, [router])

  const handleLogin = async (event: React.MouseEvent) => {
    event.preventDefault()
    setLivenessLoading(true)
    setErrorMessage(null)

    try {
      const result = await checkBackendLiveness()

      if (!result.ok) {
        logError('LoginPage', 'Liveness check failed', result)
        setErrorMessage(result.message)
        return
      }

      window.location.assign('/api/oauth2/authorization/spotify-artist-insight-service')
    } catch (error) {
      logError('LoginPage', 'Unexpected liveness check error', error)
      setErrorMessage('Unable to connect to the server. Please check your connection.')
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
      <button className="button login-button" onClick={handleLogin} disabled={livenessLoading}>
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
            See the development{' '}
            <a
              href="https://github.com/taonity/artist-insight-service?tab=readme-ov-file#roadmap"
              target="_blank"
              rel="noopener noreferrer"
            >
              roadmap
            </a>{' '}
            and suggest your ideas
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