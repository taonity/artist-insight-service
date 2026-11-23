'use client'

import ArtistRecords from '@/features/artists/components/ArtistRecords'
import { useDevLoading } from '@/features/dev/DevTools'
import { useCompactLayout } from '@/hooks/useCompactLayout'
import type { EnrichableArtistObject } from '@/types/followings'
import FollowingsDesktopTable from './FollowingsDesktopTable'

interface Props {
  enrichableArtistObjects: EnrichableArtistObject[]
  loading: boolean
  refreshing: boolean
  onRefresh: () => void
}

export default function ArtistList({
  enrichableArtistObjects,
  loading,
  refreshing,
  onRefresh,
}: Props) {
  const compact = useCompactLayout()
  const showLoading = useDevLoading() || loading

  if (compact) return <ArtistRecords artists={enrichableArtistObjects} loading={showLoading} />

  return (
    <FollowingsDesktopTable
      artists={enrichableArtistObjects}
      loading={showLoading}
      refreshing={refreshing}
      onRefresh={onRefresh}
    />
  )
}
