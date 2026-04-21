'use client'

import { useState, useEffect } from 'react'
import { useParams } from 'next/navigation'
import Image from 'next/image'
import ErrorNotification from '@/components/feedback/ErrorNotification'
import Loading from '@/components/feedback/Loading'
import Header from '@/components/layout/Header'
import { fetchSharedArtists } from '@/features/share/api'
import { SharedArtistList } from '@/features/share/components'
import { useUser } from '@/hooks/useUser'
import type { SharedArtistsData } from '@/types/share'
import { logError } from '@/lib/logger'

export default function SharePage() {
  const params = useParams()
  const shareCode = params.shareCode as string
  const [errorMessage, setErrorMessage] = useState<string | null>(null)
  const [loading, setLoading] = useState(true)
  const [shareData, setShareData] = useState<SharedArtistsData | null>(null)
  const [notFound, setNotFound] = useState(false)
  const [expired, setExpired] = useState(false)

  const user = useUser({ silent: true })

  useEffect(() => {
    let isActive = true

    async function loadSharedArtists() {
      try {
        const result = await fetchSharedArtists(shareCode)

        if (!isActive) {
          return
        }

        if (result.status === 'not-found') {
          setNotFound(true)
          setLoading(false)
          return
        }

        if (result.status === 'expired') {
          setExpired(true)
          setLoading(false)
          return
        }

        setShareData(result.data)
      } catch (error) {
        if (isActive) {
          logError('SharePage', 'Failed to load shared artists', error)
          setErrorMessage('Unable to load shared artists. Please try again.')
        }
      } finally {
        if (isActive) {
          setLoading(false)
        }
      }
    }

    void loadSharedArtists()

    return () => {
      isActive = false
    }
  }, [shareCode])

  const isVisitor = !user

  if (notFound) {
    return (
      <div>
        <Header user={null} visitorMode={true} />
        <div className="share-page">
          <div className="share-error-card">
            <h1>Share link not found</h1>
            <p>This share link doesn&apos;t exist or has been deleted.</p>
          </div>
        </div>
      </div>
    )
  }

  if (expired) {
    return (
      <div>
        <Header user={null} visitorMode={true} />
        <div className="share-page">
          <div className="share-error-card">
            <h1>Share link expired</h1>
            <p>This share link has expired and is no longer available.</p>
          </div>
        </div>
      </div>
    )
  }

  return (
    <div>
      {errorMessage && (
        <ErrorNotification message={errorMessage} onClose={() => setErrorMessage(null)} />
      )}
      <Header user={user} loading={loading && !isVisitor} visitorMode={isVisitor} />

      <div className="share-page">
        {loading ? (
          <Loading items={10} sharePage />
        ) : shareData && (
          <>
            <div className="share-owner-info">
              <Image
                src={shareData.owner.avatarUrl || '/default-user-pfp.png'}
                alt={shareData.owner.displayName}
                width={64}
                height={64}
                className="share-owner-avatar"
              />
              <div className="share-owner-details">
                <h1>{shareData.owner.displayName}&apos;s Followed Artists</h1>
                <p>{shareData.artists.length} artists</p>
              </div>
            </div>

            <SharedArtistList artists={shareData.artists} />
          </>
        )}
      </div>
    </div>
  )
}