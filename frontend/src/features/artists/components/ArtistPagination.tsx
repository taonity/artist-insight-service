'use client'

import { useState, type Ref } from 'react'

export const ARTIST_PAGE_SIZES = [20, 50, 100] as const
export const COMPACT_ARTIST_PAGE_SIZES = [10, ...ARTIST_PAGE_SIZES] as const
export type ArtistPageSize = (typeof COMPACT_ARTIST_PAGE_SIZES)[number]

interface Props {
  page: number
  size: ArtistPageSize
  count: number
  loading: boolean
  position: 'top' | 'bottom'
  compact?: boolean
  navigationRef?: Ref<HTMLElement>
  onPageChange: (page: number) => void
  onSizeChange: (size: ArtistPageSize) => void
}

export default function ArtistPagination({
  page,
  size,
  count,
  loading,
  position,
  compact = false,
  navigationRef,
  onPageChange,
  onSizeChange,
}: Props) {
  const [draft, setDraft] = useState<string | null>(null)
  const sizes = compact ? COMPACT_ARTIST_PAGE_SIZES : ARTIST_PAGE_SIZES
  const pageCount = Math.max(1, Math.ceil(count / size))
  const first = count ? page * size + 1 : 0
  const last = Math.min((page + 1) * size, count)

  return (
    <nav
      ref={navigationRef}
      aria-label={`Pagination ${position === 'top' ? 'above' : 'below'} artists`}
      tabIndex={-1}
      className={compact ? 'artist-record-pagination' : 'followings-pagination'}
    >
      <span className="followings-page-range" role={position === 'top' ? 'status' : undefined}>
        {loading
          ? 'Loading…'
          : `${first.toLocaleString()}–${last.toLocaleString()} of ${count.toLocaleString()}`}
      </span>
      <label>
        <span className={compact ? 'app-sr-only' : undefined}>Rows per page</span>
        <select
          title={compact ? 'Rows per page' : undefined}
          value={size}
          disabled={loading}
          onChange={(event) => {
            const selected = sizes.find((option) => String(option) === event.target.value)
            if (selected) onSizeChange(selected)
          }}
        >
          {sizes.map((option) => (
            <option key={option}>{option}</option>
          ))}
        </select>
      </label>
      <button
        type="button"
        aria-label={compact ? 'Previous artists page' : undefined}
        disabled={loading || page === 0}
        onClick={() => onPageChange(page - 1)}
      >
        {compact ? (
          <span className="artist-pagination-chevron" aria-hidden="true">
            ‹
          </span>
        ) : (
          'Previous'
        )}
      </button>
      <form
        onSubmit={(event) => {
          event.preventDefault()
          const requested = Number(draft ?? page + 1)
          setDraft(null)
          if (
            Number.isInteger(requested) &&
            requested >= 1 &&
            requested <= pageCount &&
            requested !== page + 1
          ) {
            onPageChange(requested - 1)
          }
        }}
      >
        <label>
          <span className={compact ? 'app-sr-only' : undefined}>Page</span>
          <input
            type="number"
            inputMode="numeric"
            aria-label="Page number"
            min={1}
            max={pageCount}
            value={draft ?? String(page + 1)}
            disabled={loading || !count}
            onChange={(event) => setDraft(event.target.value)}
            onBlur={() => setDraft(null)}
          />
        </label>
        <span aria-label={`${pageCount} pages`}>/ {pageCount}</span>
      </form>
      <button
        type="button"
        aria-label={compact ? 'Next artists page' : undefined}
        disabled={loading || page + 1 >= pageCount}
        onClick={() => onPageChange(page + 1)}
      >
        {compact ? (
          <span className="artist-pagination-chevron" aria-hidden="true">
            ›
          </span>
        ) : (
          'Next'
        )}
      </button>
    </nav>
  )
}
