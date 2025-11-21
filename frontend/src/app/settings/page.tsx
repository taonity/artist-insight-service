'use client'

import { useState } from 'react'
import Header from '@/components/Header'
import ErrorNotification from '@/components/ErrorNotification'
import { useUser } from '@/hooks/useUser'
import { CSRF_COOKIE_NAME } from '@/lib/backend'

const csrfErrorMessage = 'Unable to verify your request. Please refresh the page and try again.'
const networkErrorMessage = 'Unable to connect to the server. Please check your connection.'

function getCookie(name: string) {
  if (typeof document === 'undefined') {
    return null
  }
  const match = document.cookie.match(new RegExp('(^| )' + name + '=([^;]+)'))
  return match ? decodeURIComponent(match[2]) : null
}

async function requestLogout(xsrfToken: string) {
  const res = await fetch('/api/logout', {
    method: 'POST',
    credentials: 'include',
    headers: { 'X-XSRF-TOKEN': xsrfToken },
  })

  if (!res.ok) {
    throw new Error('Logout failed')
  }
}

export default function SettingsPage() {
  const [errorMessage, setErrorMessage] = useState<string | null>(null)
  const [isProcessing, setIsProcessing] = useState(false)
  const user = useUser(setErrorMessage)

  const handleLogout = async () => {
    const xsrfToken = getCookie(CSRF_COOKIE_NAME)
    if (!xsrfToken) {
      setErrorMessage(csrfErrorMessage)
      return
    }

    setIsProcessing(true)
    try {
      await requestLogout(xsrfToken)
      window.location.href = '/login'
    } catch (err) {
      setErrorMessage('Unable to log out. Please try again.')
    } finally {
      setIsProcessing(false)
    }
  }

  const handleDeleteAccount = async () => {
    if (!window.confirm('Are you sure you want to delete your account? This action cannot be undone.')) {
      return
    }

    const xsrfToken = getCookie(CSRF_COOKIE_NAME)
    if (!xsrfToken) {
      setErrorMessage(csrfErrorMessage)
      return
    }

    setIsProcessing(true)
    try {
      const res = await fetch('/api/user', {
        method: 'DELETE',
        credentials: 'include',
        headers: { 'X-XSRF-TOKEN': xsrfToken },
      })

      if (res.status === 401) {
        window.location.href = '/login'
        return
      }

      if (res.status === 403) {
        setErrorMessage(csrfErrorMessage)
        return
      }

      if (!res.ok && res.status !== 204) {
        setErrorMessage('Failed to delete your account. Please try again.')
        return
      }

      try {
        await requestLogout(xsrfToken)
      } catch (err) {
        // Ignore logout failures here and continue redirecting the user.
      }

      window.location.href = '/login'
    } catch (err) {
      setErrorMessage(networkErrorMessage)
    } finally {
      setIsProcessing(false)
    }
  }

  const isLoadingUser = !user

  return (
    <div>
      {errorMessage && (
        <ErrorNotification message={errorMessage} onClose={() => setErrorMessage(null)} />
      )}
      <Header user={user} loading={isLoadingUser} />
      <div className="settings-page">
        <div className="settings-page-card">
          <h1>Your account</h1>
          <p>
            <strong>Display name:</strong>{' '}
            {isLoadingUser ? (
              <span className="skeleton-line short" style={{ display: 'inline-block', verticalAlign: 'middle', height: '1em', marginTop: 0 }} />
            ) : (
              user.privateUserObject.displayName
            )}
          </p>
          <p>
            <strong>Spotify ID:</strong>{' '}
            {isLoadingUser ? (
              <span className="skeleton-line" style={{ display: 'inline-block', verticalAlign: 'middle', height: '1em', marginTop: 0 }} />
            ) : (
              user.privateUserObject.id
            )}
          </p>
          <p>
            <strong>GPT enrichments remaining:</strong>{' '}
            {isLoadingUser ? (
              <span className="skeleton-line short" style={{ display: 'inline-block', verticalAlign: 'middle', height: '1em', marginTop: 0, width: '3ch' }} />
            ) : (
              user.gptUsagesLeft
            )}
          </p>
        </div>
        <div className="settings-page-actions">
          <button onClick={handleLogout} disabled={isProcessing || isLoadingUser}>
            Log out
          </button>
          <button className="danger-button" onClick={handleDeleteAccount} disabled={isProcessing || isLoadingUser}>
            Delete account
          </button>
        </div>
        <p className="settings-page-warning">
          Deleting your account removes your saved preferences. Artists and genres that have already been
          enriched stay available for the community.
        </p>
      </div>
    </div>
  )
}

