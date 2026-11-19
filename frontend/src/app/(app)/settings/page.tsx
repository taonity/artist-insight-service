'use client'

import ErrorNotification from '@/components/feedback/ErrorNotification'
import ContentSkeleton from '@/components/feedback/ContentSkeleton'
import { useDevLoading } from '@/features/dev/DevTools'
import Header from '@/components/layout/Header'
import { useSettingsPage } from '@/features/settings/useSettingsPage'

export default function SettingsPage() {
  const preview = useDevLoading()
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
      {errorMessage && <ErrorNotification message={errorMessage} onClose={clearErrorMessage} />}
      <Header user={user} loading={isLoadingUser} />
      <div className="settings-page">
        <div className="settings-page-card">
          <h1>Your account</h1>
          <p>
            <strong>Display name:</strong>{' '}
            <ContentSkeleton loading={isLoadingUser || preview}>{displayName}</ContentSkeleton>
          </p>
          <p>
            <strong>Spotify ID:</strong>{' '}
            <ContentSkeleton loading={isLoadingUser || preview}>{spotifyId}</ContentSkeleton>
          </p>
          <p>
            <strong>GPT enrichments remaining:</strong>{' '}
            <ContentSkeleton loading={isLoadingUser || preview}>{gptUsagesLeft}</ContentSkeleton>
          </p>
        </div>
        <div className="settings-page-card">
          <h2>Share link</h2>
          <p className="settings-share-status">
            <ContentSkeleton loading={shareLinkLoading || preview}>
              {shareLink
                ? 'Your public artist list is available at the link below.'
                : "You don't have an active share link. Create one from the home page."}
            </ContentSkeleton>
          </p>
          <p>
            <strong>Share code:</strong>{' '}
            <ContentSkeleton loading={shareLinkLoading || preview}>
              {shareLink?.shareCode || 'Not created'}
            </ContentSkeleton>
          </p>
          <p>
            <strong>Expires:</strong>{' '}
            <ContentSkeleton loading={shareLinkLoading || preview}>
              {shareLink ? new Date(shareLink.expiresAt).toLocaleDateString() : 'Not scheduled'}
            </ContentSkeleton>
          </p>
          <p className="settings-share-url">
            <strong>URL:</strong>{' '}
            <ContentSkeleton loading={shareLinkLoading || preview}>
              {shareLink ? (
                <a href={`/share/${shareLink.shareCode}`} target="_blank" rel="noopener noreferrer">
                  {typeof window !== 'undefined'
                    ? `${window.location.origin}/share/${shareLink.shareCode}`
                    : `/share/${shareLink.shareCode}`}
                </a>
              ) : (
                'Create a share link from the home page.'
              )}
            </ContentSkeleton>
          </p>
          <button
            className="danger-button"
            onClick={handleDeleteShareLink}
            disabled={!shareLink || shareLinkLoading || isProcessing || preview}
            style={{ marginTop: '1rem' }}
          >
            Delete share link
          </button>
        </div>

        <div className="settings-page-actions">
          <button onClick={handleLogout} disabled={isProcessing || isLoadingUser || preview}>
            Log out
          </button>
          <button
            className="danger-button"
            onClick={handleDeleteAccount}
            disabled={isProcessing || isLoadingUser || preview}
          >
            Delete account
          </button>
        </div>
        <p className="settings-page-warning">
          Deleting your account removes your saved preferences. Artists and genres that have already
          been enriched stay available for the community.
        </p>
      </div>
    </div>
  )
}
