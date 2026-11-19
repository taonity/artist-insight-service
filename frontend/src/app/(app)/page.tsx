'use client'

import { CSVLink } from 'react-csv'
import { AdvisoryCards, ArtistList, GptUsageBlock } from '@/features/followings/components'
import ErrorNotification from '@/components/feedback/ErrorNotification'
import Loading from '@/components/feedback/Loading'
import Header from '@/components/layout/Header'
import { useDevLoading } from '@/features/dev/DevTools'
import ContentSkeleton from '@/components/feedback/ContentSkeleton'
import { useFollowingsPage } from '@/features/followings/useFollowingsPage'

export default function Home() {
  const preview = useDevLoading()
  const {
    user,
    enrichableArtistObjects,
    advisories,
    loading,
    enriching,
    gptUsagesLeft,
    errorMessage,
    shareLoading,
    shareCopiedNotification,
    hasExistingShareLink,
    csvData,
    clearErrorMessage,
    loadEnrichedFollowings,
    handleShare,
  } = useFollowingsPage()

  return (
    <div>
      {errorMessage && <ErrorNotification message={errorMessage} onClose={clearErrorMessage} />}
      <Header user={user} />
      <div className="main-content">
        <ContentSkeleton loading={loading || preview} className="usage-skeleton">
          <GptUsageBlock count={gptUsagesLeft} />
        </ContentSkeleton>
        {(loading || enrichableArtistObjects.length > 0) && (
          <div className="actions">
            <CSVLink
              data={csvData}
              filename="exported-artists.csv"
              aria-label="Download CSV"
              className="button"
              aria-disabled={loading || enriching || preview}
              onClick={() => !loading && !enriching && !preview}
            >
              Export CSV
            </CSVLink>
            <button
              type="button"
              onClick={handleShare}
              title={hasExistingShareLink ? 'Update share link' : 'Create share link'}
              disabled={loading || shareLoading || enriching || preview}
            >
              {shareLoading ? 'Generating...' : 'Share link'}
            </button>
            <button
              className="action-enrich"
              aria-label="Enrich followings"
              title="Fill missing genres with AI using your GPT credits"
              type="button"
              onClick={loadEnrichedFollowings}
              disabled={loading || enriching || preview}
            >
              {enriching ? 'Enriching...' : 'Enrich'}
            </button>
          </div>
        )}
        {shareCopiedNotification && <div className="share-copied-notification">Link copied!</div>}
        <AdvisoryCards advisories={advisories} />
        {loading || enriching ? (
          <Loading items={enrichableArtistObjects.length || 10} tableOnly />
        ) : (
          <ArtistList enrichableArtistObjects={enrichableArtistObjects} />
        )}
      </div>
    </div>
  )
}
