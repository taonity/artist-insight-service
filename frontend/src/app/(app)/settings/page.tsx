'use client'

import ErrorNotification from '@/components/feedback/ErrorNotification'
import Header from '@/components/layout/Header'
import { useSettingsPage } from '@/features/settings/useSettingsPage'

export default function SettingsPage() {
  const {
    user,
    errorMessage,
    isProcessing,
    shareLink,
    shareLinkLoading,
    isLoadingUser,
    clearErrorMessage,
    handleLogout,
    handleDeleteAccount,
    handleDeleteShareLink,
  } = useSettingsPage()

  const displayName = user?.privateUserObject.displayName ?? 'Unavailable'
  const spotifyId = user?.privateUserObject.id ?? 'Unavailable'
  const gptUsagesLeft = user?.gptUsagesLeft ?? 'Unavailable'

  return (
    <div>
      {errorMessage && (
        <ErrorNotification message={errorMessage} onClose={clearErrorMessage} />
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
              displayName
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
              spotifyId
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
              gptUsagesLeft
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