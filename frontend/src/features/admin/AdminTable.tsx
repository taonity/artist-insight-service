'use client'

import { useCallback, useEffect, useRef, useState, type ReactNode } from 'react'
import { useDevLoading } from '@/features/dev/DevTools'
import { adminRequest, type Page } from './api'
import SkeletonSlot from './SkeletonSlot'

export interface Column<T> {
  key: string
  label: string
  sortKey?: string
  defaultWidth: number
  align?: 'left' | 'right'
  render?: (row: T) => ReactNode
  placeholder?: ReactNode
  mobileWide?: boolean
  mobilePrimary?: boolean
}
interface Props<T> {
  endpoint: string
  columns: Column<T>[]
  rowKey: keyof T
  defaultSort: string
  refreshKey: number
  enabled?: boolean
  audit?: boolean
  searchLabel?: string
}
export default function AdminTable<T extends object>({
  endpoint,
  columns,
  rowKey,
  defaultSort,
  refreshKey,
  enabled = true,
  audit = false,
  searchLabel = 'Search records',
}: Props<T>) {
  const preview = useDevLoading()
  const [data, setData] = useState<Page<T> | null>(null)
  const [busy, setBusy] = useState(true)
  const [error, setError] = useState('')
  const [query, setQuery] = useState('')
  const [search, setSearch] = useState('')
  const [page, setPage] = useState(0)
  const [size, setSize] = useState(10)
  const [sort, setSort] = useState(defaultSort)
  const [direction, setDirection] = useState(
    defaultSort === 'occurredAt' || defaultSort === 'createdAt' ? 'desc' : 'asc',
  )
  const [startDate, setStartDate] = useState('')
  const [endDate, setEndDate] = useState('')
  const [field, setField] = useState('all')
  const [exact, setExact] = useState(false)
  const [widths, setWidths] = useState<Record<string, number>>({})
  const [manualRefresh, setManualRefresh] = useState(0)
  const serial = useRef(0)
  const storageKey = `artist-insight.admin.widths.${endpoint}`
  const loading = preview || !enabled || (data === null && busy)
  useEffect(() => {
    try {
      const stored = JSON.parse(localStorage.getItem(storageKey) || '{}')
      if (stored && typeof stored === 'object')
        setWidths(
          Object.fromEntries(
            Object.entries(stored).filter(([, v]) => typeof v === 'number' && v >= 64 && v <= 800),
          ) as Record<string, number>,
        )
    } catch {
      /* Column sizes are optional preferences. */
    }
  }, [storageKey])
  function saveWidths(next: Record<string, number>) {
    setWidths(next)
    try {
      localStorage.setItem(storageKey, JSON.stringify(next))
    } catch {
      /* Optional storage. */
    }
  }
  const load = useCallback(async () => {
    if (!enabled) return
    const request = ++serial.current
    if (startDate && endDate && startDate > endDate) {
      setError('Start date must not be after end date.')
      setBusy(false)
      return
    }
    setBusy(true)
    setError('')
    const params = new URLSearchParams({
      page: String(page),
      size: String(size),
      sort,
      direction,
      q: search,
    })
    if (audit) {
      params.set('field', field)
      params.set('exact', String(exact))
      if (startDate) params.set('startDate', startDate)
      if (endDate) params.set('endDate', endDate)
    }
    try {
      const result = await adminRequest<Page<T>>(`${endpoint}?${params}`)
      if (request === serial.current) setData(result)
    } catch (e) {
      if (request === serial.current)
        setError(e instanceof Error ? e.message : 'Unable to load data.')
    } finally {
      if (request === serial.current) setBusy(false)
    }
  }, [
    audit,
    direction,
    enabled,
    endDate,
    endpoint,
    exact,
    field,
    page,
    search,
    size,
    sort,
    startDate,
  ])
  useEffect(() => {
    void load()
    const current = serial
    return () => {
      current.current += 1
    }
  }, [load, refreshKey, manualRefresh])
  function resize(event: React.PointerEvent, key: string, initial: number) {
    const start = event.clientX
    const handle = event.currentTarget as HTMLElement
    let next = widths
    handle.setPointerCapture(event.pointerId)
    const move = (e: PointerEvent) => {
      next = { ...widths, [key]: Math.min(800, Math.max(64, initial + e.clientX - start)) }
      setWidths(next)
    }
    const stop = () => {
      saveWidths(next)
      handle.removeEventListener('pointermove', move)
      handle.removeEventListener('pointerup', stop)
      handle.removeEventListener('pointercancel', stop)
    }
    handle.addEventListener('pointermove', move)
    handle.addEventListener('pointerup', stop)
    handle.addEventListener('pointercancel', stop)
  }
  const rowCount = data?.size ?? size
  const rows = Array.from({ length: rowCount }, (_, index) => data?.content[index] ?? null)
  const empty = data !== null && data.content.length === 0
  return (
    <section className="admin-data" aria-busy={busy || loading}>
      <div className="admin-toolbar">
        <form
          className="admin-search-form"
          onSubmit={(e) => {
            e.preventDefault()
            setSearch(query)
            setPage(0)
            setManualRefresh((v) => v + 1)
          }}
        >
          <label className="admin-search">
            <span className="admin-sr-only">Search</span>
            <svg width="15" height="15" viewBox="0 0 20 20" fill="none" aria-hidden="true">
              <circle cx="8.5" cy="8.5" r="5.5" stroke="currentColor" strokeWidth="1.5" />
              <path d="m13 13 4 4" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" />
            </svg>
            <input
              type="search"
              placeholder={searchLabel}
              value={query}
              maxLength={512}
              onChange={(e) => setQuery(e.target.value)}
              disabled={!enabled || preview}
            />
          </label>
          {audit && (
            <>
              <label className="admin-inline-label">
                Field{' '}
                <select
                  aria-label="Search field"
                  value={field}
                  onChange={(e) => {
                    setField(e.target.value)
                    setPage(0)
                  }}
                  disabled={!enabled || preview}
                >
                  <option value="all">All fields</option>
                  {columns
                    .filter((c) =>
                      ['action', 'targetType', 'targetId', 'actorSpotifyId'].includes(c.key),
                    )
                    .map((c) => (
                      <option key={c.key} value={c.key}>
                        {c.label}
                      </option>
                    ))}
                </select>
              </label>
              <label className="admin-check">
                <input
                  type="checkbox"
                  checked={exact}
                  onChange={(e) => {
                    setExact(e.target.checked)
                    setPage(0)
                  }}
                  disabled={!enabled || preview}
                />
                Exact match
              </label>
            </>
          )}
          <button className="admin-button" type="submit" disabled={!enabled || preview}>
            Search
          </button>
        </form>
        <button
          className="admin-button admin-refresh"
          aria-label="Refresh"
          type="button"
          onClick={() => setManualRefresh((v) => v + 1)}
          disabled={busy || !enabled || preview}
        >
          <svg
            className={busy ? 'admin-spinning' : ''}
            width="14"
            height="14"
            viewBox="0 0 20 20"
            fill="none"
            aria-hidden="true"
          >
            <path
              d="M16 6a7 7 0 1 0 1 7M16 2v5h-5"
              stroke="currentColor"
              strokeWidth="1.5"
              strokeLinecap="round"
              strokeLinejoin="round"
            />
          </svg>
          <span className="admin-refresh-label">Refresh</span>
        </button>
      </div>
      {audit && (
        <div className="admin-datebar">
          <span>
            Period <small>UTC</small>
          </span>
          <label>
            From{' '}
            <input
              type="date"
              value={startDate}
              max={endDate || '9999-12-31'}
              onChange={(e) => {
                setStartDate(e.target.value)
                setPage(0)
              }}
              disabled={!enabled || preview}
            />
          </label>
          <label>
            Through{' '}
            <input
              type="date"
              value={endDate}
              min={startDate || '0001-01-01'}
              max="9999-12-31"
              onChange={(e) => {
                setEndDate(e.target.value)
                setPage(0)
              }}
              disabled={!enabled || preview}
            />
          </label>
          <button
            className="admin-text-button"
            onClick={() => {
              setStartDate('')
              setEndDate('')
              setPage(0)
            }}
            disabled={!startDate && !endDate}
          >
            Clear
          </button>
        </div>
      )}
      {error && (
        <p className="admin-error" role="alert">
          {error}
        </p>
      )}
      <div className="admin-mobile-sort">
        <label>
          Sort by
          <select
            aria-label="Sort records by"
            value={sort}
            disabled={!enabled || preview}
            onChange={(e) => {
              setSort(e.target.value)
              setPage(0)
            }}
          >
            {columns
              .filter((c) => c.sortKey)
              .map((c) => (
                <option key={c.key} value={c.sortKey}>
                  {c.label}
                </option>
              ))}
          </select>
        </label>
        <button
          className="admin-button"
          disabled={!enabled || preview}
          aria-label={`Sort ${direction === 'asc' ? 'descending' : 'ascending'}`}
          onClick={() => {
            setDirection((v) => (v === 'asc' ? 'desc' : 'asc'))
            setPage(0)
          }}
        >
          {direction === 'asc' ? 'Ascending' : 'Descending'}
        </button>
      </div>
      <div className="admin-table-scroll">
        <table className="admin-table">
          <colgroup>
            {columns.map((c) => (
              <col key={c.key} style={{ width: widths[c.key] || c.defaultWidth }} />
            ))}
          </colgroup>
          <thead>
            <tr>
              {columns.map((c) => (
                <th
                  key={c.key}
                  className={c.align === 'right' ? 'admin-align-right' : undefined}
                  aria-sort={
                    sort === c.sortKey
                      ? direction === 'asc'
                        ? 'ascending'
                        : 'descending'
                      : undefined
                  }
                >
                  {c.sortKey ? (
                    <button
                      className="admin-sort"
                      onClick={() => {
                        setSort(c.sortKey!)
                        setDirection(sort === c.sortKey && direction === 'asc' ? 'desc' : 'asc')
                        setPage(0)
                      }}
                    >
                      {c.label}
                      <span className="admin-sort-indicator" aria-hidden="true">
                        {sort === c.sortKey ? (direction === 'asc' ? '↑' : '↓') : ''}
                      </span>
                    </button>
                  ) : (
                    c.label
                  )}
                  <span
                    role="separator"
                    aria-label={`Resize ${c.label}`}
                    aria-orientation="vertical"
                    aria-valuenow={widths[c.key] || c.defaultWidth}
                    aria-valuemin={64}
                    aria-valuemax={800}
                    tabIndex={0}
                    className="admin-resize"
                    onPointerDown={(e) => resize(e, c.key, widths[c.key] || c.defaultWidth)}
                    onKeyDown={(e) => {
                      if (e.key === 'ArrowRight' || e.key === 'ArrowLeft') {
                        e.preventDefault()
                        saveWidths({
                          ...widths,
                          [c.key]: Math.min(
                            800,
                            Math.max(
                              64,
                              (widths[c.key] || c.defaultWidth) +
                                (e.key === 'ArrowRight' ? 16 : -16),
                            ),
                          ),
                        })
                      }
                    }}
                  />
                </th>
              ))}
            </tr>
          </thead>
          <tbody>
            {empty ? (
              <tr className="admin-empty-row">
                <td colSpan={columns.length} style={{ height: rowCount * 56 }}>
                  <SkeletonSlot loading={loading}>No matching records.</SkeletonSlot>
                </td>
              </tr>
            ) : (
              rows.map((row, index) => (
                <tr
                  key={row ? String(row[rowKey]) : `empty-${index}`}
                  className={`admin-record-row ${!row && data !== null ? 'admin-blank-row' : ''}`}
                  aria-hidden={(!row && data !== null) || undefined}
                >
                  {columns.map((c) => (
                    <td
                      key={c.key}
                      data-label={c.label}
                      data-column={c.key}
                      data-wide={c.mobileWide || undefined}
                      data-primary={c.mobilePrimary || undefined}
                      className={c.align === 'right' ? 'admin-align-right' : undefined}
                    >
                      <div
                        className={`admin-cell ${c.align === 'right' ? 'admin-cell-right' : ''}`}
                      >
                        <SkeletonSlot loading={loading && (row !== null || data === null)}>
                          {row ? (
                            c.render ? (
                              c.render(row)
                            ) : (
                              <span
                                className="admin-value"
                                title={String((row as Record<string, unknown>)[c.key] ?? '')}
                              >
                                {String((row as Record<string, unknown>)[c.key] ?? '-')}
                              </span>
                            )
                          ) : loading && data === null ? (
                            (c.placeholder ?? (
                              <span className="admin-placeholder-text">{c.label}</span>
                            ))
                          ) : (
                            <span>&nbsp;</span>
                          )}
                        </SkeletonSlot>
                      </div>
                    </td>
                  ))}
                </tr>
              ))
            )}
          </tbody>
        </table>
      </div>
      <div className="admin-pagination">
        <span className="admin-record-count" role="status">
          <SkeletonSlot loading={loading}>{data?.totalElements ?? 120} records</SkeletonSlot>
        </span>
        <div className="admin-page-controls">
          <label>
            Rows{' '}
            <select
              value={size}
              onChange={(e) => {
                setSize(Number(e.target.value))
                setPage(0)
              }}
              disabled={busy || !enabled || preview}
            >
              {[10, 25, 50, 100].map((s) => (
                <option key={s}>{s}</option>
              ))}
            </select>
          </label>
          <span className="admin-page-number">
            <SkeletonSlot loading={loading}>
              Page {page + 1} of {Math.max(1, data?.totalPages ?? 12)}
            </SkeletonSlot>
          </span>
          <button
            className="admin-button"
            aria-label="Previous page"
            disabled={busy || page === 0 || !enabled || preview}
            onClick={() => setPage((v) => v - 1)}
          >
            <svg
              className="admin-page-chevron"
              width="16"
              height="16"
              viewBox="0 0 20 20"
              fill="none"
              aria-hidden="true"
            >
              <path
                d="m12 4-6 6 6 6"
                stroke="currentColor"
                strokeWidth="1.5"
                strokeLinecap="round"
                strokeLinejoin="round"
              />
            </svg>
            <span className="admin-page-button-label">Previous</span>
          </button>
          <button
            className="admin-button"
            aria-label="Next page"
            disabled={busy || page + 1 >= (data?.totalPages ?? 0) || !enabled || preview}
            onClick={() => setPage((v) => v + 1)}
          >
            <span className="admin-page-button-label">Next</span>
            <svg
              className="admin-page-chevron"
              width="16"
              height="16"
              viewBox="0 0 20 20"
              fill="none"
              aria-hidden="true"
            >
              <path
                d="m8 4 6 6-6 6"
                stroke="currentColor"
                strokeWidth="1.5"
                strokeLinecap="round"
                strokeLinejoin="round"
              />
            </svg>
          </button>
        </div>
      </div>
    </section>
  )
}
