'use client'

import Image from 'next/image'
import { useEffect, useState } from 'react'
import { usePathname, useRouter } from 'next/navigation'
import ContentSkeleton from '@/components/feedback/ContentSkeleton'
import { useDevLoading } from '@/features/dev/DevTools'
import type { User } from '@/types/user'

interface Props {
  user: User | null
  loading?: boolean
  visitorMode?: boolean
}

const Header: React.FC<Props> = ({ user, loading = false, visitorMode = false }) => {
  const preview = useDevLoading()
  const [canAdmin, setCanAdmin] = useState(false)
  useEffect(() => {
    if (!user || visitorMode) return
    let active = true
    void fetch('/api/console/access/me', { cache: 'no-store' })
      .then((r) => (r.ok ? r.json() : null))
      .then((a) => {
        if (active) setCanAdmin(a?.isAdmin === true)
      })
      .catch(() => {
        if (active) setCanAdmin(false)
      })
    return () => {
      active = false
    }
  }, [user, visitorMode])
  const pathname = usePathname()
  const router = useRouter()
  const isLoading = preview || (!visitorMode && (loading || !user))
  const displayName = visitorMode ? 'Visitor' : (user?.privateUserObject.displayName ?? '')
  const avatarUrl = visitorMode
    ? '/default-user-pfp.png'
    : (user?.privateUserObject.images?.[0]?.url ?? '/default-user-pfp.png')

  return (
    <header className="header">
      <div className={`user-info${isLoading ? ' user-info--loading' : ''}`}>
        <ContentSkeleton loading={isLoading} className="header-avatar-slot">
          <Image
            src={avatarUrl}
            alt=""
            width={48}
            height={48}
            className="artist-image"
            unoptimized
          />
        </ContentSkeleton>
        <div className="header-display-slot">
          <ContentSkeleton loading={isLoading}>
            <span className="header-identity">
              <span className="header-identity-label">
                {visitorMode ? 'Viewing as' : 'Logged in as'}
              </span>
              <span className="header-identity-name">{displayName || 'Listener'}</span>
            </span>
          </ContentSkeleton>
        </div>
      </div>
      <nav className="header-actions" aria-label="Account navigation">
        {visitorMode ? (
          <button type="button" onClick={() => router.push('/login')}>
            Log in
          </button>
        ) : (
          <>
            {canAdmin && (
              <button type="button" onClick={() => router.push('/admin')}>
                Admin
              </button>
            )}
            {pathname === '/' ? (
              <button
                className="header-donate-button"
                type="button"
                onClick={() => router.push('/donate')}
              >
                Donate
                <svg
                  width="16"
                  height="16"
                  viewBox="0 0 24 24"
                  fill="none"
                  stroke="currentColor"
                  strokeWidth="1.8"
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  aria-hidden="true"
                  focusable="false"
                >
                  <path d="M20.84 4.61a5.5 5.5 0 0 0-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 0 0-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 0 0 0-7.78Z" />
                </svg>
              </button>
            ) : (
              <button type="button" onClick={() => router.push('/')}>
                Home
              </button>
            )}
            <button type="button" onClick={() => router.push('/settings')}>
              Settings
            </button>
          </>
        )}
      </nav>
    </header>
  )
}

export default Header
