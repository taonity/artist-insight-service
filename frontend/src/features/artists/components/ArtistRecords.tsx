'use client'

import { useMemo, useRef, useState } from 'react'
import Image from 'next/image'
import ContentSkeleton from '@/components/feedback/ContentSkeleton'
import type { Artist } from '@/types/artist'

export interface ArtistRecord {
  artistObject: Artist
  genreEnriched?: boolean
  enrichedGenres?: string[]
}
type Sort = 'name' | 'followers' | 'popularity'
const pageSize = 10
const placeholder: ArtistRecord = {
  artistObject: {
    id: '',
    name: 'Artist name',
    genres: ['Indie pop', 'Electronic'],
    followers: { total: 12000 },
    popularity: 64,
    externalUrls: { spotify: '#' },
  },
}

export default function ArtistRecords({
  artists = [],
  loading = false,
}: {
  artists?: ArtistRecord[]
  loading?: boolean
}) {
  const [query, setQuery] = useState('')
  const [sort, setSort] = useState<Sort>('name')
  const [descending, setDescending] = useState(false)
  const [page, setPage] = useState(0)
  const region = useRef<HTMLElement>(null)
  function changePage(next: number) {
    setPage(next)
    region.current?.focus({ preventScroll: true })
    region.current?.scrollIntoView({ block: 'start' })
  }
  const filtered = useMemo(() => {
    const search = query.trim().toLocaleLowerCase()
    return artists
      .filter((row) =>
        [
          row.artistObject.name,
          ...(row.artistObject.genres ?? []),
          ...(row.enrichedGenres ?? []),
        ].some((value) => value.toLocaleLowerCase().includes(search)),
      )
      .sort((a, b) => {
        const x = a.artistObject,
          y = b.artistObject
        const order =
          sort === 'name'
            ? x.name.localeCompare(y.name)
            : sort === 'followers'
              ? x.followers.total - y.followers.total
              : x.popularity - y.popularity
        return (descending ? -order : order) || x.id.localeCompare(y.id)
      })
  }, [artists, query, sort, descending])
  const pages = Math.max(1, Math.ceil(filtered.length / pageSize))
  const current = Math.min(page, pages - 1)
  const rows =
    artists.length === 0 && loading
      ? Array.from({ length: pageSize }, () => placeholder)
      : filtered.slice(current * pageSize, (current + 1) * pageSize)
  return (
    <section
      ref={region}
      tabIndex={-1}
      className="artist-records"
      aria-label="Followed artists"
      aria-busy={loading}
    >
      <div className="artist-record-controls">
        <label className="artist-record-search">
          <span className="app-sr-only">Search artists or genres</span>
          <input
            type="search"
            placeholder="Search artists or genres..."
            value={query}
            disabled={loading}
            onChange={(event) => {
              setQuery(event.target.value)
              setPage(0)
            }}
          />
        </label>
        <div className="artist-record-sort" role="group" aria-label="Sort artists">
          {(
            [
              ['name', 'Name'],
              ['followers', 'Followers'],
              ['popularity', 'Popularity'],
            ] as const
          ).map(([key, label]) => (
            <button
              key={key}
              type="button"
              aria-label={`Sort by ${key}`}
              aria-pressed={sort === key}
              disabled={loading}
              onClick={() => {
                setSort(key)
                setPage(0)
              }}
            >
              {label}
            </button>
          ))}
          <button
            type="button"
            className="artist-sort-direction"
            aria-label={`Sort ${descending ? 'ascending' : 'descending'}`}
            title={descending ? 'Descending' : 'Ascending'}
            disabled={loading}
            onClick={() => {
              setDescending((value) => !value)
              setPage(0)
            }}
          >
            <svg
              width="18"
              height="18"
              viewBox="0 0 20 20"
              fill="none"
              aria-hidden="true"
              style={{ transform: descending ? 'rotate(180deg)' : undefined }}
            >
              <path
                d="M5 16V4m-3 3 3-3 3 3m3-1h6m-6 4h4m-4 4h2"
                stroke="currentColor"
                strokeWidth="1.5"
                strokeLinecap="round"
                strokeLinejoin="round"
              />
            </svg>
          </button>
        </div>
      </div>
      <div className="artist-record-count" role="status">
        <ContentSkeleton loading={loading}>
          {filtered.length} {filtered.length === 1 ? 'artist' : 'artists'}
          {query.trim() ? ' found' : ''}
        </ContentSkeleton>
      </div>
      <ul className="artist-record-list">
        {rows.map((row, index) => {
          const artist = row.artistObject
          const sourceGenres = artist.genres ?? []
          const aiGenres = row.enrichedGenres ?? []
          const genres = [...new Set([...sourceGenres, ...aiGenres])]
          return (
            <li className="artist-record" key={artist.id || `placeholder-${index}`}>
              <div className="artist-record-identity">
                <ContentSkeleton loading={loading} className="artist-record-avatar">
                  <Image
                    src={artist.images?.[0]?.url || '/default-artist-pfp.jpeg'}
                    alt=""
                    width={44}
                    height={44}
                    unoptimized
                  />
                </ContentSkeleton>
                <div className="artist-record-name">
                  <ContentSkeleton loading={loading}>
                    <a
                      href={artist.externalUrls.spotify}
                      target="_blank"
                      rel="noopener noreferrer"
                      tabIndex={loading ? -1 : 0}
                    >
                      {artist.name}
                    </a>
                  </ContentSkeleton>
                </div>
              </div>
              <div className="artist-record-genres">
                <span className="artist-record-label">
                  Genres{row.genreEnriched ? ' (AI enriched)' : ''}
                </span>
                <ContentSkeleton loading={loading}>
                  <span
                    className={`artist-record-genre-text ${row.genreEnriched ? 'artist-record-ai-genres' : ''}`}
                  >
                    {genres.length ? (
                      <>
                        {sourceGenres.join(', ')}
                        {aiGenres.length > 0 && (
                          <span className="artist-record-ai-genres">
                            {sourceGenres.length > 0 ? '; ' : ''}AI: {aiGenres.join(', ')}
                          </span>
                        )}
                      </>
                    ) : (
                      'No genres listed'
                    )}
                  </span>
                </ContentSkeleton>
              </div>
              <dl className="artist-record-stats">
                <div>
                  <dt>Followers</dt>
                  <dd>
                    <ContentSkeleton loading={loading}>
                      {artist.followers.total.toLocaleString('en-US')}
                    </ContentSkeleton>
                  </dd>
                </div>
                <div>
                  <dt>Popularity</dt>
                  <dd>
                    <ContentSkeleton loading={loading}>
                      {artist.popularity}
                      <span className="artist-record-stat-unit"> / 100</span>
                    </ContentSkeleton>
                  </dd>
                </div>
              </dl>
            </li>
          )
        })}
        {!loading && rows.length === 0 && (
          <li className="artist-record-empty">
            {query ? 'No artists match your search.' : 'No followed artists to show yet.'}
          </li>
        )}
      </ul>
      <nav className="artist-record-pagination" aria-label="Artist pages">
        <span>
          <ContentSkeleton loading={loading}>
            Page {current + 1} of {pages}
          </ContentSkeleton>
        </span>
        <button
          type="button"
          aria-label="Previous artists page"
          disabled={loading || current === 0}
          onClick={() => changePage(current - 1)}
        >
          Previous
        </button>
        <button
          type="button"
          aria-label="Next artists page"
          disabled={loading || current + 1 >= pages}
          onClick={() => changePage(current + 1)}
        >
          Next
        </button>
      </nav>
    </section>
  )
}
