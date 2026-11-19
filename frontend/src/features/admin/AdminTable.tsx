'use client'

import { useCallback, useEffect, useRef, useState, type ReactNode } from 'react'
import { useDevLoading } from '@/features/dev/DevTools'
import { adminRequest, type Page } from './api'
export interface Column<T> {
  key: string
  label: string
  sortKey?: string
  defaultWidth: number
  render?: (row: T) => ReactNode
}
export default function AdminTable<T extends object>({
  endpoint,
  columns,
  rowKey,
  defaultSort,
  refreshKey,
  audit = false,
}: {
  endpoint: string
  columns: Column<T>[]
  rowKey: keyof T
  defaultSort: string
  refreshKey: number
  audit?: boolean
}) {
  const forceLoading = useDevLoading()
  const [data, setData] = useState<Page<T> | null>(null)
  const [busy, setBusy] = useState(true)
  const [error, setError] = useState('')
  const [query, setQuery] = useState('')
  const [search, setSearch] = useState('')
  const [page, setPage] = useState(0)
  const [size, setSize] = useState(50)
  const [sort, setSort] = useState(defaultSort)
  const [direction, setDirection] = useState(
    defaultSort === 'occurredAt' || defaultSort === 'createdAt' ? 'desc' : 'asc',
  )
  const [startDate, setStartDate] = useState('')
  const [endDate, setEndDate] = useState('')
  const [field, setField] = useState('all')
  const [exact, setExact] = useState(false)
  const [visible, setVisible] = useState(columns.map((c) => c.key))
  const [widths, setWidths] = useState<Record<string, number>>({})
  const [preferencesReady, setPreferencesReady] = useState(false)
  const [manualRefresh, setManualRefresh] = useState(0)
  const serial = useRef(0)
  const storageKey = `artist-insight.admin.table.${endpoint}`
  useEffect(() => {
    try {
      const stored = JSON.parse(localStorage.getItem(storageKey) || 'null')
      if (Array.isArray(stored?.visible)) {
        const valid = stored.visible.filter(
          (key: unknown): key is string =>
            typeof key === 'string' && columns.some((c) => c.key === key),
        )
        if (valid.length) setVisible(valid)
      }
      if (stored?.widths && typeof stored.widths === 'object')
        setWidths(
          Object.fromEntries(
            Object.entries(stored.widths).filter(
              ([, v]) => typeof v === 'number' && v >= 64 && v <= 800,
            ),
          ) as Record<string, number>,
        )
    } catch {
      /* Storage may be unavailable. */
    }
    setPreferencesReady(true)
  }, [storageKey, columns])
  useEffect(() => {
    if (preferencesReady) {
      try {
        localStorage.setItem(storageKey, JSON.stringify({ visible, widths }))
      } catch {
        /* Storage may be unavailable. */
      }
    }
  }, [storageKey, visible, widths, preferencesReady])
  const load = useCallback(async () => {
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
  }, [audit, direction, endDate, endpoint, exact, field, page, search, size, sort, startDate])
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
    handle.setPointerCapture(event.pointerId)
    const move = (e: PointerEvent) =>
      setWidths((prev) => ({
        ...prev,
        [key]: Math.min(800, Math.max(64, initial + e.clientX - start)),
      }))
    const stop = () => {
      handle.removeEventListener('pointermove', move)
      handle.removeEventListener('pointerup', stop)
      handle.removeEventListener('pointercancel', stop)
    }
    handle.addEventListener('pointermove', move)
    handle.addEventListener('pointerup', stop)
    handle.addEventListener('pointercancel', stop)
  }
  const shown = columns.filter((c) => visible.includes(c.key))
  return (
    <section aria-busy={busy}>
      <form
        className="admin-toolbar"
        onSubmit={(e) => {
          e.preventDefault()
          setSearch(query)
          setPage(0)
          setManualRefresh((v) => v + 1)
        }}
      >
        <label>
          Search <input value={query} maxLength={512} onChange={(e) => setQuery(e.target.value)} />
        </label>
        {audit && (
          <>
            <label>
              Field{' '}
              <select
                value={field}
                onChange={(e) => {
                  setField(e.target.value)
                  setPage(0)
                }}
              >
                <option value="all">All</option>
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
            <label>
              <input
                type="checkbox"
                checked={exact}
                onChange={(e) => {
                  setExact(e.target.checked)
                  setPage(0)
                }}
              />{' '}
              Exact
            </label>
          </>
        )}
        <button type="submit">Search</button>
        <button type="button" onClick={() => setManualRefresh((v) => v + 1)} disabled={busy}>
          Refresh
        </button>
        <details>
          <summary>Columns</summary>
          {columns.map((c) => (
            <label key={c.key}>
              <input
                type="checkbox"
                checked={visible.includes(c.key)}
                disabled={visible.length === 1 && visible.includes(c.key)}
                onChange={() =>
                  setVisible((v) =>
                    v.includes(c.key) ? v.filter((k) => k !== c.key) : [...v, c.key],
                  )
                }
              />
              {c.label}
            </label>
          ))}
          <button
            type="button"
            onClick={() => {
              setWidths({})
              setVisible(columns.map((c) => c.key))
            }}
          >
            Reset columns
          </button>
        </details>
      </form>
      {audit && (
        <div className="admin-toolbar">
          <span>Dates (UTC)</span>
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
            />
          </label>
          <button
            onClick={() => {
              setStartDate('')
              setEndDate('')
              setPage(0)
            }}
          >
            All time
          </button>
        </div>
      )}
      {error && <p role="alert">{error}</p>}
      <div className="admin-table-scroll">
        <table>
          <thead>
            <tr>
              {shown.map((c) => (
                <th
                  key={c.key}
                  style={{ width: widths[c.key] || c.defaultWidth }}
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
                      onClick={() => {
                        setSort(c.sortKey!)
                        setDirection(sort === c.sortKey && direction === 'asc' ? 'desc' : 'asc')
                        setPage(0)
                      }}
                    >
                      {c.label}
                      {sort === c.sortKey ? (direction === 'asc' ? ' (asc)' : ' (desc)') : ''}
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
                        setWidths((w) => ({
                          ...w,
                          [c.key]: Math.min(
                            800,
                            Math.max(
                              64,
                              (w[c.key] || c.defaultWidth) + (e.key === 'ArrowRight' ? 16 : -16),
                            ),
                          ),
                        }))
                      }
                    }}
                  />
                </th>
              ))}
            </tr>
          </thead>
          <tbody>
            {forceLoading || (!data && busy)
              ? Array.from({ length: 6 }, (_, i) => (
                  <tr key={i}>
                    {shown.map((c) => (
                      <td key={c.key}>
                        <span className="admin-skeleton">Loading...</span>
                      </td>
                    ))}
                  </tr>
                ))
              : data?.content.map((row) => (
                  <tr key={String(row[rowKey])}>
                    {shown.map((c) => (
                      <td key={c.key} data-label={c.label}>
                        <div
                          className="admin-cell"
                          title={String((row as Record<string, unknown>)[c.key] ?? '')}
                        >
                          {c.render
                            ? c.render(row)
                            : String((row as Record<string, unknown>)[c.key] ?? '-')}
                        </div>
                      </td>
                    ))}
                  </tr>
                ))}
          </tbody>
        </table>
      </div>
      {data && data.content.length === 0 && <p>No matching records.</p>}
      <div className="admin-toolbar">
        <span role="status">
          {busy ? 'Refreshing...' : `${data?.totalElements ?? 0} records`} | Page {page + 1} of{' '}
          {Math.max(1, data?.totalPages || 0)}
        </span>
        <button disabled={busy || page === 0} onClick={() => setPage((v) => v - 1)}>
          Previous
        </button>
        <button
          disabled={busy || page + 1 >= (data?.totalPages || 0)}
          onClick={() => setPage((v) => v + 1)}
        >
          Next
        </button>
        <label>
          Rows{' '}
          <select
            value={size}
            onChange={(e) => {
              setSize(Number(e.target.value))
              setPage(0)
            }}
          >
            {[10, 25, 50, 100].map((s) => (
              <option key={s}>{s}</option>
            ))}
          </select>
        </label>
      </div>
    </section>
  )
}
