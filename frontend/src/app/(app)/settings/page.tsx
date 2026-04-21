'use client'

import { useState, useEffect } from 'react'
import { useRouter } from 'next/navigation'
import ErrorNotification from '@/components/feedback/ErrorNotification'
import Header from '@/components/layout/Header'
import { useUser } from '@/hooks/useUser'
import { getCookie } from '@/lib/cookies'
import { fetchWithTimeout, DEFAULT_NETWORK_ERROR_MESSAGE } from '@/lib/clientApi'
import { logError } from '@/lib/logger'
import { getRuntimeConfig } from '@/lib/runtimeConfig'

const csrfErrorMessage = 'Unable to verify your request. Please refresh the page and try again.'

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
  const router = useRouter()
  const [errorMessage, setErrorMessage] = useState<string | null>(null)
  const [isProcessing, setIsProcessing] = useState(false)
  const [csrfCookieName, setCsrfCookieName] = useState('XSRF-TOKEN')
  const [shareLink, setShareLink] = useState<{ shareCode: string; expiresAt: string } | null>(null)
  const [shareLinkLoading, setShareLinkLoading] = useState(true)
  const user = useUser({ onError: setErrorMessage })

  useEffect(() => {
    void getRuntimeConfig().then((config) => setCsrfCookieName(config.csrfCookieName))
    void loadShareLinkStatus()
  }, [])

  const handleLogout = async () => {
    const xsrfToken = getCookie(csrfCookieName)
    if (!xsrfToken) {
      logError('SettingsPage', `CSRF token not found for logout with CSRF_COOKIE_NAME=${csrfCookieName}`)
      setErrorMessage(csrfErrorMessage)
      return
    }

    setIsProcessing(true)
    try {
      await requestLogout(xsrfToken)
      router.replace('/login')
    } catch (error) {
      logError('SettingsPage', 'Logout failed', error)
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
      logError('SettingsPage', `CSRF token not found for delete account with CSRF_COOKIE_NAME=${csrfCookieName}`)
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
        router.replace('/login')
        return
      }

      if (res.status === 403) {
        logError('SettingsPage', 'Delete account forbidden - CSRF validation failed')
        setErrorMessage(csrfErrorMessage)
        return
      }

      if (!res.ok && res.status !== 204) {
        logError('SettingsPage', `Delete account failed with status: ${res.status}`)
        setErrorMessage('Failed to delete your account. Please try again.')
        return
      }

      try {
        await requestLogout(xsrfToken)
      } catch (error) {
        logError('SettingsPage', 'Logout after delete account failed', error)
      }

      router.replace('/login')
    } catch (error) {
      logError('SettingsPage', 'Delete account network error', error)
      setErrorMessage(DEFAULT_NETWORK_ERROR_MESSAGE)
    } finally {
      setIsProcessing(false)
    }
  }

  const loadShareLinkStatus = async () => {
    try {
      const res = await fetchWithTimeout('/api/share', { timeoutMs: 10000 })
      if (res.status === 404) {
        setShareLink(null)
      } else if (res.ok) {
        const data = await res.json()
        setShareLink({ shareCode: data.shareCode, expiresAt: data.expiresAt })
      }
    } catch (error) {
      logError('SettingsPage', 'Failed to load share link status', error)
    } finally {
      setShareLinkLoading(false)
    }
  }

  const handleDeleteShareLink = async () => {
    if (
      !window.confirm(
        'Are you sure you want to delete your share link? Anyone with the link will no longer be able to access it.',
      )
    ) {
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
        router.replace('/login')
        return
      }

      if (res.status === 403) {
        logError('SettingsPage', 'Delete share link forbidden - CSRF validation failed')
        setErrorMessage(csrfErrorMessage)
        return
      }

      if (!res.ok && res.status !== 204) {
        logError('SettingsPage', `Delete share link failed with status: ${res.status}`)
        setErrorMessage('Failed to delete your share link. Please try again.')
        return
      }

      setShareLink(null)
    } catch (error) {
      logError('SettingsPage', 'Delete share link network error', error)
      setErrorMessage(DEFAULT_NETWORK_ERROR_MESSAGE)
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
              <span
                className="skeleton-line short"
                style={{ display: 'inline-block', verticalAlign: 'middle', height: '1em', marginTop: 0 }}
              />
            ) : (
              user.privateUserObject.displayName
            )}
          </p>
          <p>
            <strong>Spotify ID:</strong>{' '}
            {isLoadingUser ? (
              <span
                className="skeleton-line"
                style={{ display: 'inline-block', verticalAlign: 'middle', height: '1em', marginTop: 0 }}
              />
            ) : (
              user.privateUserObject.id
            )}
          </p>
          <p>
            <strong>GPT enrichments remaining:</strong>{' '}
            {isLoadingUser ? (
              <span
                className="skeleton-line short"
                style={{ display: 'inline-block', verticalAlign: 'middle', height: '1em', marginTop: 0, width: '3ch' }}
              />
            ) : (
              user.gptUsagesLeft
            )}
          </p>
        </div>

        <div className="settings-page-card">
          <h2>Share link</h2>
          {shareLinkLoading ? (
            <p>
              <span
                className="skeleton-line"
                style={{ display: 'inline-block', verticalAlign: 'middle', height: '1em', marginTop: 0 }}
              />
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
                  {typeof window !== 'undefined'
                    ? `${window.location.origin}/share/${shareLink.shareCode}`
                    : `/share/${shareLink.shareCode}`}
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
          Deleting your account removes your saved preferences. Artists and genres that have already been enriched stay
          available for the community.
        </p>
      </div>
    </div>
  )
}