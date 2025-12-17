'use client'

import { useState, useEffect } from 'react'
import { useParams } from 'next/navigation'
import Header from '@/components/Header'
import ErrorNotification from '@/components/ErrorNotification'
import Loading from '@/components/Loading'
import SharedArtistList from '@/components/SharedArtistList'
import { useUser } from '@/hooks/useUser'
import { logError } from '@/utils/logger'
import Image from 'next/image'

interface Artist {
  id: string
  name: string
  images?: { url: string }[]
  genres?: string[]
  externalUrls: { spotify: string }
  followers: { total: number }
  popularity: number
}

interface ShareOwner {
  displayName: string
  avatarUrl: string | null
}

interface SharedArtistsData {
  owner: ShareOwner
  artists: Artist[]
  mergedGenres: string[]
}

export default function SharePage() {
  const params = useParams()
  const shareCode = params.shareCode as string
  const [errorMessage, setErrorMessage] = useState<string | null>(null)
  const [loading, setLoading] = useState(true)
  const [shareData, setShareData] = useState<SharedArtistsData | null>(null)
  const [notFound, setNotFound] = useState(false)
  const [expired, setExpired] = useState(false)

  // Try to get user - will be null for unauthenticated visitors
  const user = useUser(() => {}, true) // silent mode - don't redirect on 401

  useEffect(() => {
    loadSharedArtists()
  }, [shareCode])

  const loadSharedArtists = async () => {
    try {
      const res = await fetch(`/api/share/${shareCode}`)
      
      if (res.status === 404) {
        setNotFound(true)
        setLoading(false)
        return
      }

      if (res.status === 410) {
        setExpired(true)
        setLoading(false)
        return
      }

      if (!res.ok) {
        throw new Error(`Failed to load shared artists: ${res.status}`)
      }

      const data = await res.json()
      setShareData(data)
    } catch (err) {
      logError('SharePage', 'Failed to load shared artists', err)
      setErrorMessage('Unable to load shared artists. Please try again.')
    } finally {
      setLoading(false)
    }
  }

  const isVisitor = !user

  if (notFound) {
    return (
      <div>
        <Header user={null} visitorMode={true} />
        <div className="share-page">
          <div className="share-error-card">
            <h1>Share link not found</h1>
            <p>This share link doesn't exist or has been deleted.</p>
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
                <h1>{shareData.owner.displayName}'s Followed Artists</h1>
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
