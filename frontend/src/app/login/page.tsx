'use client'

import { useEffect, useState } from 'react'
import { Righteous } from 'next/font/google'
import Image from 'next/image'
import ErrorNotification from '../../components/ErrorNotification'
import BackgroundPhrases from '../../components/BackgroundPhrases'

const righteous = Righteous({ 
  weight: '400',
  subsets: ['latin'],
})

export default function Login() {
  const [livenessLoading, setLivenessLoading] = useState(false)
  const [errorMessage, setErrorMessage] = useState<string | null>(null)
  const [genreDescription, setGenreDescription] = useState<string | null>(null)

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
      <BackgroundPhrases onGenreHover={setGenreDescription} />
      {errorMessage && (
        <ErrorNotification message={errorMessage} onClose={() => setErrorMessage(null)} />
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
