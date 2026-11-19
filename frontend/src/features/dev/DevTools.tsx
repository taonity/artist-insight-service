'use client'
import { useEffect, useState, useSyncExternalStore } from 'react'
import { getRuntimeConfig } from '@/lib/runtimeConfig'
let preview = false
const listeners = new Set<() => void>()
function subscribe(listener: () => void) {
  listeners.add(listener)
  return () => {
    listeners.delete(listener)
  }
}
export function useDevLoading() {
  return useSyncExternalStore(
    subscribe,
    () => preview,
    () => false,
  )
}
export default function DevTools() {
  const [logins, setLogins] = useState<Array<{ registrationId: string; label: string }>>([])
  const [backendUrl, setBackendUrl] = useState('')
  const [open, setOpen] = useState(false)
  const loading = useDevLoading()
  useEffect(() => {
    let active = true
    void getRuntimeConfig()
      .then(async (config) => {
        if (config.profile !== 'local' && config.profile !== 'demo') return
        const response = await fetch('/api/dev/stub-users', { cache: 'no-store' })
        if (!response.ok) return
        const users = await response.json()
        if (active) {
          setLogins(users)
          setBackendUrl(config.publicBackendUrl)
        }
      })
      .catch(() => {})
    return () => {
      active = false
    }
  }, [])
  if (!logins.length) return null
  return (
    <aside
      style={{
        position: 'fixed',
        bottom: 12,
        right: 12,
        zIndex: 1000,
        background: open ? '#18251d' : 'transparent',
        padding: open ? 12 : 0,
        border: open ? '1px solid #31523b' : 'none',
        borderRadius: 8,
        color: 'white',
        maxWidth: 300,
        width: open ? 'min(300px, calc(100vw - 24px))' : 44,
        boxSizing: 'border-box',
        maxHeight: 'calc(100dvh - 24px)',
        overflowY: 'auto',
      }}
      aria-label="Local developer tools"
    >
      <button
        onClick={() => setOpen((v) => !v)}
        aria-expanded={open}
        aria-label="Developer tools"
        title="Developer tools"
        style={{
          width: 44,
          height: 44,
          minHeight: 44,
          padding: 10,
          borderRadius: 8,
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          background: '#18251d',
          border: '1px solid #31523b',
          color: '#91d9a7',
        }}
      >
        <svg
          width="22"
          height="22"
          viewBox="0 0 24 24"
          fill="none"
          stroke="currentColor"
          strokeWidth="1.5"
          strokeLinecap="round"
          aria-hidden="true"
        >
          <path d="M4 7h6m4 0h6M4 17h10m4 0h2" />
          <circle cx="12" cy="7" r="2" />
          <circle cx="16" cy="17" r="2" />
        </svg>
      </button>
      {open && (
        <div>
          <p>Stub Spotify login</p>
          {logins.map((login) => (
            <p key={login.registrationId}>
              <a
                href={`${backendUrl}/oauth2/authorization/${encodeURIComponent(login.registrationId)}`}
              >
                {login.label}
              </a>
            </p>
          ))}
          <p>Admin fixtures require the demo-data profile, or assignment by the Owner.</p>
          <label>
            <input
              type="checkbox"
              checked={loading}
              onChange={(e) => {
                preview = e.target.checked
                listeners.forEach((l) => l())
              }}
            />{' '}
            Preview loading states
          </label>
        </div>
      )}
    </aside>
  )
}
