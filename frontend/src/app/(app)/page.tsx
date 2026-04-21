'use client'

import { CSVLink } from 'react-csv'
import { AdvisoryCards, ArtistList, GptUsageBlock } from '@/features/followings/components'
import ErrorNotification from '@/components/feedback/ErrorNotification'
import Loading from '@/components/feedback/Loading'
import Header from '@/components/layout/Header'
import { useFollowingsPage } from '@/features/followings/useFollowingsPage'

export default function Home() {
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
      {errorMessage && (
        <ErrorNotification message={errorMessage} onClose={clearErrorMessage} />
      )}
      <Header user={user} />
      <div className="main-content">
        {loading ? (
          <Loading items={enrichableArtistObjects.length || 10} />
        ) : (
          <>
            {enriching ? (
              <div className="gpt-usage skeleton-gpt-usage">
                <div className="skeleton-line" style={{ width: 140, height: 18.9 }} />
              </div>
            ) : (
              <GptUsageBlock count={gptUsagesLeft} />
            )}
            {enrichableArtistObjects.length > 0 && (
              <div className="actions">
                <button type="button" onClick={loadEnrichedFollowings} disabled={enriching}>
                  {enriching ? 'Enriching…' : 'Enrich followings'}
                </button>
                <CSVLink data={csvData} filename={'exported-artists.csv'} className="button">
                  Download CSV
                </CSVLink>
                <button type="button" onClick={handleShare} disabled={shareLoading || enriching}>
                  {shareLoading ? 'Generating…' : hasExistingShareLink ? 'Update Share Link' : 'Share'}
                </button>
              </div>
            )}
            {shareCopiedNotification && <div className="share-copied-notification">Link copied!</div>}
            <AdvisoryCards advisories={advisories} />
            {enriching ? (
              <Loading items={enrichableArtistObjects.length || 10} tableOnly />
            ) : (
              <ArtistList enrichableArtistObjects={enrichableArtistObjects} />
            )}
          </>
        )}
      </div>
    </div>
  )
}