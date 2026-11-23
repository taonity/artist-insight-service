'use client'

import { useMemo, useRef, useState, useSyncExternalStore, type PointerEvent } from 'react'

export const ARTIST_TABLE_COLUMNS = [
  { key: 'avatar', label: 'Avatar', width: 90 },
  { key: 'name', label: 'Name', width: 290 },
  { key: 'genres', label: 'Genres', width: 580 },
  { key: 'followers', label: 'Followers', width: 120 },
  { key: 'popularity', label: 'Popularity', width: 120 },
] as const

export type ArtistColumnKey = (typeof ARTIST_TABLE_COLUMNS)[number]['key']
type Widths = Record<ArtistColumnKey, number>
interface Preferences {
  visible: ArtistColumnKey[]
  widths: Widths
}

const MIN_WIDTH = 64
const STORAGE_KEY = 'artist-insight.followings.table-columns'
const CHANGE_EVENT = `${STORAGE_KEY}.change`
const defaultPreferences: Preferences = {
  visible: ARTIST_TABLE_COLUMNS.map(({ key }) => key),
  widths: Object.fromEntries(ARTIST_TABLE_COLUMNS.map(({ key, width }) => [key, width])) as Widths,
}

function subscribe(onChange: () => void) {
  window.addEventListener('storage', onChange)
  window.addEventListener(CHANGE_EVENT, onChange)
  return () => {
    window.removeEventListener('storage', onChange)
    window.removeEventListener(CHANGE_EVENT, onChange)
  }
}

function readStorage() {
  try {
    return localStorage.getItem(STORAGE_KEY)
  } catch {
    return null
  }
}

function restorePreferences(snapshot: string | null): Preferences {
  if (!snapshot) return defaultPreferences
  try {
    const stored = JSON.parse(snapshot) as Partial<Preferences>
    const visible = defaultPreferences.visible.filter((key) => stored.visible?.includes(key))
    const widths = { ...defaultPreferences.widths }
    for (const key of defaultPreferences.visible) {
      const width = stored.widths?.[key]
      if (typeof width === 'number' && width >= MIN_WIDTH && width <= 1200) widths[key] = width
    }
    return { widths, visible: visible.length ? visible : defaultPreferences.visible }
  } catch {
    return defaultPreferences
  }
}

function resizeBoundary(
  widths: Widths,
  key: ArtistColumnKey,
  nextKey: ArtistColumnKey,
  delta: number,
): Widths {
  const change = Math.max(
    MIN_WIDTH - widths[key],
    Math.min(widths[nextKey] - MIN_WIDTH, Math.round(delta)),
  )
  return { ...widths, [key]: widths[key] + change, [nextKey]: widths[nextKey] - change }
}

export function useArtistTableColumns() {
  const snapshot = useSyncExternalStore(subscribe, readStorage, () => null)
  const stored = useMemo(() => restorePreferences(snapshot), [snapshot])
  const [unsaved, setUnsaved] = useState<Preferences | null>(null)
  const [previewWidths, setPreviewWidths] = useState<Widths | null>(null)
  const drag = useRef<{
    pointerId: number
    start: number
    unitsPerPixel: number
    original: Widths
    draft: Widths
  } | null>(null)
  const { visible, widths: savedWidths } = unsaved ?? stored
  const widths = previewWidths ?? savedWidths
  const columns = ARTIST_TABLE_COLUMNS.filter(({ key }) => visible.includes(key))
  const totalWidth = columns.reduce((total, { key }) => total + widths[key], 0)

  function save(preferences: Preferences) {
    try {
      localStorage.setItem(STORAGE_KEY, JSON.stringify(preferences))
      setUnsaved(null)
      window.dispatchEvent(new Event(CHANGE_EVENT))
    } catch {
      setUnsaved(preferences)
    }
  }

  function toggleColumn(key: ArtistColumnKey) {
    const next = visible.includes(key)
      ? visible.filter((column) => column !== key)
      : [...visible, key]
    if (next.length) save({ visible: next, widths: savedWidths })
  }

  function startResize(event: PointerEvent<HTMLDivElement>) {
    if (event.button !== 0 || drag.current) return
    const tableWidth = event.currentTarget.closest('table')?.getBoundingClientRect().width
    if (!tableWidth) return
    event.preventDefault()
    event.currentTarget.setPointerCapture(event.pointerId)
    drag.current = {
      pointerId: event.pointerId,
      start: event.clientX,
      unitsPerPixel: totalWidth / tableWidth,
      original: savedWidths,
      draft: savedWidths,
    }
  }

  function moveResize(
    event: PointerEvent<HTMLDivElement>,
    key: ArtistColumnKey,
    nextKey: ArtistColumnKey,
  ) {
    const active = drag.current
    if (!active || active.pointerId !== event.pointerId) return
    active.draft = resizeBoundary(
      active.original,
      key,
      nextKey,
      (event.clientX - active.start) * active.unitsPerPixel,
    )
    setPreviewWidths(active.draft)
  }

  function finishResize(event: PointerEvent<HTMLDivElement>) {
    const active = drag.current
    if (!active || active.pointerId !== event.pointerId) return
    drag.current = null
    setPreviewWidths(null)
    save({ visible, widths: active.draft })
  }

  return {
    columns,
    visible,
    widths,
    totalWidth,
    minWidth: MIN_WIDTH,
    resizing: previewWidths !== null,
    toggleColumn,
    startResize,
    moveResize,
    finishResize,
    resizeBy: (key: ArtistColumnKey, nextKey: ArtistColumnKey, delta: number) =>
      save({ visible, widths: resizeBoundary(savedWidths, key, nextKey, delta) }),
    resetBoundary: (key: ArtistColumnKey, nextKey: ArtistColumnKey) =>
      save({
        visible,
        widths: resizeBoundary(
          savedWidths,
          key,
          nextKey,
          defaultPreferences.widths[key] - savedWidths[key],
        ),
      }),
    resetWidths: () => save({ visible, widths: defaultPreferences.widths }),
    hasCustomWidths: ARTIST_TABLE_COLUMNS.some(({ key, width }) => savedWidths[key] !== width),
  }
}
