import Image from 'next/image'
import ContentSkeleton from '@/components/feedback/ContentSkeleton'
import type { ShareOwner } from '@/types/share'

export default function ShareOwnerInfo({
  owner,
  count = 0,
  loading = false,
}: {
  owner?: ShareOwner
  count?: number
  loading?: boolean
}) {
  return (
    <div className="share-owner-info">
      <ContentSkeleton loading={loading} className="share-owner-avatar">
        <Image
          src={owner?.avatarUrl || '/default-user-pfp.png'}
          alt=""
          width={64}
          height={64}
          unoptimized
        />
      </ContentSkeleton>
      <div className="share-owner-details">
        <h1>
          <ContentSkeleton loading={loading}>
            {owner ? `${owner.displayName}'s Followed Artists` : 'Shared followed artists'}
          </ContentSkeleton>
        </h1>
        <p>
          <ContentSkeleton loading={loading}>{count} artists</ContentSkeleton>
        </p>
      </div>
    </div>
  )
}
