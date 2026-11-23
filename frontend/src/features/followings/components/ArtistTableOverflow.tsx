'use client'

import { useEffect, useId, useRef, useState, type ReactNode } from 'react'

function isClipped(element: HTMLElement) {
  return (
    element.scrollWidth > element.clientWidth + 1 || element.scrollHeight > element.clientHeight + 1
  )
}

export default function ArtistTableOverflow({
  text,
  children,
}: {
  text: string
  children: ReactNode
}) {
  const id = useId()
  const ref = useRef<HTMLDivElement>(null)
  const [clipped, setClipped] = useState(false)
  const [preview, setPreview] = useState<{ left: number; top?: number; bottom?: number } | null>(
    null,
  )

  useEffect(() => {
    const element = ref.current
    if (!element) return
    const observer = new ResizeObserver(() => setClipped(isClipped(element)))
    observer.observe(element)
    return () => observer.disconnect()
  }, [text])

  useEffect(() => {
    if (!preview) return
    const close = () => setPreview(null)
    window.addEventListener('scroll', close, true)
    window.addEventListener('resize', close)
    return () => {
      window.removeEventListener('scroll', close, true)
      window.removeEventListener('resize', close)
    }
  }, [preview])

  function showPreview() {
    const element = ref.current
    if (!element || !isClipped(element)) return
    const bounds = element.getBoundingClientRect()
    setPreview({
      left: Math.max(8, Math.min(bounds.left, window.innerWidth - 336)),
      ...(bounds.top > 140
        ? { bottom: window.innerHeight - bounds.top + 6 }
        : { top: bounds.bottom + 6 }),
    })
  }

  return (
    <>
      <div
        ref={ref}
        className="followings-cell-text"
        tabIndex={clipped ? 0 : undefined}
        aria-describedby={preview && clipped ? id : undefined}
        onMouseEnter={showPreview}
        onMouseLeave={() => setPreview(null)}
        onFocus={showPreview}
        onBlur={() => setPreview(null)}
        onKeyDown={(event) => {
          if (event.key === 'Escape') setPreview(null)
        }}
      >
        {children}
      </div>
      {preview && clipped && (
        <span id={id} role="tooltip" className="followings-overflow-preview" style={preview}>
          {text}
        </span>
      )}
    </>
  )
}
