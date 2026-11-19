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
        if (config.profile !== 'local') return
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
        background: '#252a32',
        padding: 12,
        border: '1px solid #888',
        borderRadius: 8,
        color: 'white',
        maxWidth: 300,
      }}
      aria-label="Local developer tools"
    >
      <button onClick={() => setOpen((v) => !v)} aria-expanded={open}>
        Developer tools
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
            Preview table loading
          </label>
        </div>
      )}
    </aside>
  )
}
