'use client'

import { useMemo, useRef, useState } from 'react'
import Image from 'next/image'
import ContentSkeleton from '@/components/feedback/ContentSkeleton'
import Genres from '@/features/artists/components/Genres'
import type { EnrichableArtistObject } from '@/types/followings'
import ArtistTableOverflow from './ArtistTableOverflow'
import ArtistPagination, {
  ARTIST_PAGE_SIZES,
  type ArtistPageSize,
} from '@/features/artists/components/ArtistPagination'
import {
  ARTIST_TABLE_COLUMNS,
  useArtistTableColumns,
  type ArtistColumnKey,
} from './useArtistTableColumns'
import './followings-table.css'

type SortColumn = Exclude<ArtistColumnKey, 'avatar'>
type SearchScope = SortColumn | 'all'

const searchableColumns = ARTIST_TABLE_COLUMNS.filter((column) => column.key !== 'avatar')
const collator = new Intl.Collator('en', { numeric: true, sensitivity: 'base' })

const columnValues = {
  name: (row: EnrichableArtistObject) => row.artistObject.name,
  genres: (row: EnrichableArtistObject) => row.artistObject.genres?.join(', ') ?? '',
  followers: (row: EnrichableArtistObject) => row.artistObject.followers.total,
  popularity: (row: EnrichableArtistObject) => row.artistObject.popularity,
}

const skeletonLabels: Record<ArtistColumnKey, string> = {
  avatar: '',
  name: 'Artist name',
  genres: 'Indie pop, electronic',
  followers: '12,000',
  popularity: '84',
}

function ArtistCell({ row, column }: { row: EnrichableArtistObject; column: ArtistColumnKey }) {
  const artist = row.artistObject
  if (column === 'avatar') {
    return (
      <a
        className="followings-artist-avatar"
        href={artist.externalUrls.spotify}
        target="_blank"
        rel="noopener noreferrer"
        aria-label={`Open ${artist.name} on Spotify`}
      >
        <Image
          src={artist.images?.[0]?.url ?? '/default-artist-pfp.jpeg'}
          alt=""
          width={40}
          height={40}
        />
      </a>
    )
  }

  const value = columnValues[column](row)
  if (column === 'genres') {
    const genres = artist.genres ?? []
    if (!genres.length) return <span className="genres-unavailable">-</span>
    return (
      <ArtistTableOverflow text={String(value)}>
        <Genres genres={genres} enriched={row.genreEnriched} />
      </ArtistTableOverflow>
    )
  }

  const text = typeof value === 'number' ? value.toLocaleString() : value
  return <ArtistTableOverflow text={text}>{text}</ArtistTableOverflow>
}

interface Props {
  artists: EnrichableArtistObject[]
  loading: boolean
  refreshing: boolean
  onRefresh: () => void
}

export default function FollowingsDesktopTable({ artists, loading, refreshing, onRefresh }: Props) {
  const [query, setQuery] = useState('')
  const [scope, setScope] = useState<SearchScope>('all')
  const [exact, setExact] = useState(false)
  const [sort, setSort] = useState<SortColumn>('name')
  const [ascending, setAscending] = useState(true)
  const [page, setPage] = useState(0)
  const [size, setSize] = useState<ArtistPageSize>(ARTIST_PAGE_SIZES[0])
  const topPagination = useRef<HTMLElement>(null)
  const columnLayout = useArtistTableColumns()
  const keyword = query.trim().toLowerCase()

  const filtered = useMemo(() => {
    const matches = (value: string) =>
      exact ? value.toLowerCase() === keyword : value.toLowerCase().includes(keyword)
    const fields = searchableColumns.filter(({ key }) => scope === 'all' || scope === key)
    return artists
      .filter(
        (row) =>
          !keyword ||
          fields.some(({ key }) => {
            if (key === 'genres') return row.artistObject.genres?.some(matches)
            const value = columnValues[key](row)
            return (
              matches(String(value)) ||
              (typeof value === 'number' && matches(value.toLocaleString()))
            )
          }),
      )
      .sort((left, right) => {
        const a = columnValues[sort](left)
        const b = columnValues[sort](right)
        const direction = ascending ? 1 : -1
        const comparison =
          typeof a === 'number' && typeof b === 'number'
            ? a - b
            : collator.compare(String(a), String(b))
        return (
          direction * comparison || collator.compare(left.artistObject.id, right.artistObject.id)
        )
      })
  }, [artists, ascending, exact, keyword, scope, sort])

  const currentPage = Math.min(page, Math.max(0, Math.ceil(filtered.length / size) - 1))
  if (currentPage !== page) setPage(currentPage)
  const rows = filtered.slice(currentPage * size, (currentPage + 1) * size)

  function changeSort(column: SortColumn) {
    setAscending(column === sort ? !ascending : true)
    setSort(column)
    setPage(0)
  }

  function returnToTop() {
    topPagination.current?.focus({ preventScroll: true })
    topPagination.current?.scrollIntoView({ block: 'start' })
  }

  function changeSize(next: ArtistPageSize) {
    setSize(next)
    setPage(0)
  }

  const pagination = { page: currentPage, size, count: filtered.length, loading }

  return (
    <section
      className="followings-table"
      aria-label="Followed artists"
      aria-busy={loading || refreshing}
    >
      <div className="followings-table-toolbar">
        <label>
          Search in
          <select
            aria-label="Search scope"
            value={scope}
            disabled={loading}
            onChange={(event) => {
              setScope(event.target.value as SearchScope)
              setPage(0)
            }}
          >
            <option value="all">All columns</option>
            {searchableColumns.map(({ key, label }) => (
              <option key={key} value={key}>
                {label}
              </option>
            ))}
          </select>
        </label>
        <input
          type="search"
          aria-label="Search all artists"
          placeholder="Search artists…"
          value={query}
          disabled={loading}
          onChange={(event) => {
            setQuery(event.target.value)
            setPage(0)
          }}
        />
        <label>
          <input
            type="checkbox"
            checked={exact}
            disabled={loading || !keyword}
            onChange={(event) => {
              setExact(event.target.checked)
              setPage(0)
            }}
          />
          Exact match
        </label>
        <div className="followings-table-actions">
          <details
            className="followings-column-menu"
            onBlur={(event) => {
              if (!event.currentTarget.contains(event.relatedTarget))
                event.currentTarget.open = false
            }}
            onKeyDown={(event) => {
              if (event.key !== 'Escape' || !event.currentTarget.open) return
              event.currentTarget.open = false
              event.currentTarget.querySelector('summary')?.focus()
            }}
          >
            <summary aria-label="Choose columns">Columns</summary>
            <div className="followings-column-options">
              {ARTIST_TABLE_COLUMNS.map(({ key, label }) => {
                const checked = columnLayout.visible.includes(key)
                return (
                  <label key={key}>
                    <input
                      type="checkbox"
                      aria-label={`Show ${label} column`}
                      checked={checked}
                      disabled={checked && columnLayout.visible.length === 1}
                      onChange={() => columnLayout.toggleColumn(key)}
                    />
                    {label}
                  </label>
                )
              })}
              <button
                type="button"
                disabled={!columnLayout.hasCustomWidths}
                onClick={columnLayout.resetWidths}
              >
                Reset column widths
              </button>
            </div>
          </details>
          <button
            type="button"
            disabled={loading || refreshing}
            onClick={onRefresh}
            aria-label="Refresh data"
          >
            {refreshing ? 'Refreshing…' : 'Refresh'}
          </button>
        </div>
      </div>

      <ArtistPagination
        {...pagination}
        position="top"
        navigationRef={topPagination}
        onPageChange={setPage}
        onSizeChange={changeSize}
      />

      <div className="followings-table-frame" data-resizing={columnLayout.resizing || undefined}>
        <table aria-label="Followed artists">
          <colgroup>
            {columnLayout.columns.map(({ key }) => (
              <col
                key={key}
                style={{ width: `${(columnLayout.widths[key] / columnLayout.totalWidth) * 100}%` }}
              />
            ))}
          </colgroup>
          <thead>
            <tr>
              {columnLayout.columns.map(({ key, label }, index) => {
                const nextKey = columnLayout.columns[index + 1]?.key
                return (
                  <th
                    key={key}
                    scope="col"
                    data-column={key}
                    aria-sort={sort === key ? (ascending ? 'ascending' : 'descending') : undefined}
                  >
                    {key === 'avatar' ? (
                      <span className="followings-column-label">{label}</span>
                    ) : (
                      <button
                        type="button"
                        className="followings-column-sort"
                        disabled={loading}
                        onClick={() => changeSort(key)}
                      >
                        <span>{label}</span>
                        {sort === key && <span aria-hidden="true">{ascending ? '↑' : '↓'}</span>}
                      </button>
                    )}
                    {nextKey && (
                      <div
                        className="followings-column-resizer"
                        role="separator"
                        tabIndex={0}
                        aria-label={`Resize ${label} column`}
                        aria-orientation="vertical"
                        aria-valuemin={columnLayout.minWidth}
                        aria-valuemax={
                          columnLayout.widths[key] +
                          columnLayout.widths[nextKey] -
                          columnLayout.minWidth
                        }
                        aria-valuenow={columnLayout.widths[key]}
                        title="Drag or use the arrow keys to resize. Press Home to reset."
                        onPointerDown={columnLayout.startResize}
                        onPointerMove={(event) => columnLayout.moveResize(event, key, nextKey)}
                        onPointerUp={columnLayout.finishResize}
                        onPointerCancel={columnLayout.finishResize}
                        onLostPointerCapture={columnLayout.finishResize}
                        onDoubleClick={() => columnLayout.resetBoundary(key, nextKey)}
                        onKeyDown={(event) => {
                          if (!['Home', 'ArrowLeft', 'ArrowRight'].includes(event.key)) return
                          event.preventDefault()
                          if (event.key === 'Home') columnLayout.resetBoundary(key, nextKey)
                          else
                            columnLayout.resizeBy(
                              key,
                              nextKey,
                              event.key === 'ArrowLeft' ? -16 : 16,
                            )
                        }}
                      />
                    )}
                  </th>
                )
              })}
            </tr>
          </thead>
          <tbody>
            {loading
              ? Array.from({ length: 10 }, (_, index) => (
                  <tr key={index} aria-hidden="true">
                    {columnLayout.columns.map(({ key }) => (
                      <td key={key}>
                        <ContentSkeleton
                          loading
                          className={key === 'avatar' ? 'followings-skeleton-avatar' : ''}
                        >
                          {skeletonLabels[key]}
                        </ContentSkeleton>
                      </td>
                    ))}
                  </tr>
                ))
              : rows.map((row) => (
                  <tr key={row.artistObject.id}>
                    {columnLayout.columns.map(({ key }) => (
                      <td key={key} data-column={key}>
                        <ArtistCell row={row} column={key} />
                      </td>
                    ))}
                  </tr>
                ))}
            {!loading && !rows.length && (
              <tr>
                <td className="followings-table-empty" colSpan={columnLayout.columns.length}>
                  {keyword ? 'No artists match your search.' : 'No followed artists to show yet.'}
                </td>
              </tr>
            )}
          </tbody>
        </table>
      </div>

      <ArtistPagination
        {...pagination}
        position="bottom"
        onPageChange={(next) => {
          setPage(next)
          returnToTop()
        }}
        onSizeChange={(next) => {
          changeSize(next)
          returnToTop()
        }}
      />
    </section>
  )
}
