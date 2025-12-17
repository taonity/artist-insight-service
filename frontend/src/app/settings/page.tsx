'use client'

import { useState, useEffect } from 'react'
import Header from '@/components/Header'
import ErrorNotification from '@/components/ErrorNotification'
import { useUser } from '@/hooks/useUser'
import { getRuntimeConfig } from '@/lib/runtimeConfig'
import { logError } from '@/utils/logger'

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
  const [csrfCookieName, setCsrfCookieName] = useState('XSRF-TOKEN')
  const [shareLink, setShareLink] = useState<{ shareCode: string; expiresAt: string } | null>(null)
  const [shareLinkLoading, setShareLinkLoading] = useState(true)
  const user = useUser(setErrorMessage)

  useEffect(() => {
    getRuntimeConfig().then(config => setCsrfCookieName(config.csrfCookieName))
    loadShareLinkStatus()
  }, [])

  const handleLogout = async () => {
    const xsrfToken = getCookie(csrfCookieName)
    if (!xsrfToken) {
      logError('SettingsPage', 'CSRF token not found for logout with CSRF_COOKIE_NAME=' + csrfCookieName)
      setErrorMessage(csrfErrorMessage)
      return
    }

    setIsProcessing(true)
    try {
      await requestLogout(xsrfToken)
      window.location.href = '/login'
    } catch (err) {
      logError('SettingsPage', 'Logout failed', err)
      setErrorMessage('Unable to log out. Please try again.')
    } finally {
      setIsProcessing(false)
    }
  }

  const handleDeleteAccount = async () => {
    if (!window.confirm('Are you sure you want to delete your account? This action cannot be undone.')) {
      return
    }

    const xsrfToken = getCookie(csrfCookieName)
    if (!xsrfToken) {
      logError('SettingsPage', 'CSRF token not found for delete account with CSRF_COOKIE_NAME=' + csrfCookieName)
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
        logError('SettingsPage', 'Delete account forbidden - CSRF validation failed')
        setErrorMessage(csrfErrorMessage)
        return
      }

      if (!res.ok && res.status !== 204) {
        logError('SettingsPage', 'Delete account failed with status: ' + res.status)
        setErrorMessage('Failed to delete your account. Please try again.')
        return
      }

      try {
        await requestLogout(xsrfToken)
      } catch (err) {
        logError('SettingsPage', 'Logout after delete account failed', err)
        // Ignore logout failures here and continue redirecting the user.
      }

      window.location.href = '/login'
    } catch (err) {
      logError('SettingsPage', 'Delete account network error', err)
      setErrorMessage(networkErrorMessage)
    } finally {
      setIsProcessing(false)
    }
  }

  const loadShareLinkStatus = async () => {
    try {
      const res = await fetch('/api/share', { credentials: 'include' })
      if (res.status === 404) {
        setShareLink(null)
      } else if (res.ok) {
        const data = await res.json()
        setShareLink({ shareCode: data.shareCode, expiresAt: data.expiresAt })
      }
    } catch (err) {
      logError('SettingsPage', 'Failed to load share link status', err)
    } finally {
      setShareLinkLoading(false)
    }
  }

  const handleDeleteShareLink = async () => {
    if (!window.confirm('Are you sure you want to delete your share link? Anyone with the link will no longer be able to access it.')) {
      return
    }

    const xsrfToken = getCookie(csrfCookieName)
    if (!xsrfToken) {
      logError('SettingsPage', 'CSRF token not found for delete share link')
      setErrorMessage(csrfErrorMessage)
      return
    }

    setIsProcessing(true)
    try {
      const res = await fetch('/api/share', {
        method: 'DELETE',
        credentials: 'include',
        headers: { 'X-XSRF-TOKEN': xsrfToken },
      })

      if (res.status === 401) {
        window.location.href = '/login'
        return
      }

      if (res.status === 403) {
        logError('SettingsPage', 'Delete share link forbidden - CSRF validation failed')
        setErrorMessage(csrfErrorMessage)
        return
      }

      if (!res.ok && res.status !== 204) {
        logError('SettingsPage', 'Delete share link failed with status: ' + res.status)
        setErrorMessage('Failed to delete your share link. Please try again.')
        return
      }

      setShareLink(null)
    } catch (err) {
      logError('SettingsPage', 'Delete share link network error', err)
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
        
        <div className="settings-page-card">
          <h2>Share link</h2>
          {shareLinkLoading ? (
            <p>
              <span className="skeleton-line" style={{ display: 'inline-block', verticalAlign: 'middle', height: '1em', marginTop: 0 }} />
            </p>
          ) : shareLink ? (
            <>
              <p>
                <strong>Share code:</strong> {shareLink.shareCode}
              </p>
              <p>
                <strong>Expires:</strong> {new Date(shareLink.expiresAt).toLocaleDateString()}
              </p>
              <p>
                <strong>URL:</strong>{' '}
                <a href={`/share/${shareLink.shareCode}`} target="_blank" rel="noopener noreferrer">
                  {typeof window !== 'undefined' ? `${window.location.origin}/share/${shareLink.shareCode}` : `/share/${shareLink.shareCode}`}
                </a>
              </p>
              <button 
                className="danger-button" 
                onClick={handleDeleteShareLink} 
                disabled={isProcessing}
                style={{ marginTop: '1rem' }}
              >
                Delete share link
              </button>
            </>
          ) : (
            <p>You don&apos;t have an active share link. Create one from the home page.</p>
          )}
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

