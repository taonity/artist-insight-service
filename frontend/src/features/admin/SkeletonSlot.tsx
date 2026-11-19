import type { ReactNode } from 'react'
export default function SkeletonSlot({
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
      className={`admin-slot ${loading ? 'admin-slot-loading' : ''} ${className}`}
      aria-hidden={loading || undefined}
    >
      <span className="admin-slot-content">{children}</span>
      {loading && <span className="admin-slot-fill" />}
    </span>
  )
}
