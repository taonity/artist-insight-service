import type { ReactNode } from 'react'

export default function ContentSkeleton({
  loading,
  children,
  className = '',
}: {
  loading: boolean
  children: ReactNode
  className?: string
}) {
  return (
    <span
      className={`content-skeleton ${loading ? 'content-skeleton-active' : ''} ${className}`}
      aria-hidden={loading || undefined}
    >
      <span className="content-skeleton-content">{children}</span>
      {loading && <span className="content-skeleton-fill" />}
    </span>
  )
}
