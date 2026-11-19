'use client'
import { useEffect, useState } from 'react'
import { fetchWithTimeout } from '@/lib/clientApi'
type Info = {
  app?: { name?: string; version?: string }
  build?: { version?: string; time?: string }
  git?: { commit?: string | { id?: string | { full?: string } } }
  runtime?: { uptime?: number }
}
function revision(info: Info): string {
  const c = info.git?.commit
  return typeof c === 'string' ? c : typeof c?.id === 'string' ? c.id : c?.id?.full || 'unknown'
}
export default function ReleaseInfo() {
  const [sources, setSources] = useState<Array<{ name: string; info: Info | null }> | null>(null)
  useEffect(() => {
    let active = true
    void Promise.all(
      (
        [
          ['Frontend', '/api/actuator/info'],
          ['Backend', '/api/actuator/backend'],
        ] as const
      ).map(async ([name, url]) => {
        try {
          const res = await fetchWithTimeout(url, { timeoutMs: 8000 })
          return { name, info: res.ok ? ((await res.json()) as Info) : null }
        } catch {
          return { name, info: null }
        }
      }),
    ).then((data) => {
      if (active) setSources(data)
    })
    return () => {
      active = false
    }
  }, [])
  if (!sources) return <p role="status">Loading release information...</p>
  return (
    <div>
      {sources.map(({ name, info }) => (
        <section key={name}>
          <h3>{name}</h3>
          {info ? (
            <dl>
              <dt>Version</dt>
              <dd>{info.app?.version || info.build?.version || 'unknown'}</dd>
              <dt>Revision</dt>
              <dd>{revision(info).slice(0, 7)}</dd>
              <dt>Built</dt>
              <dd>{info.build?.time || 'unknown'}</dd>
              {info.runtime?.uptime !== undefined && (
                <>
                  <dt>Uptime</dt>
                  <dd>{Math.floor(info.runtime.uptime)} seconds</dd>
                </>
              )}
            </dl>
          ) : (
            <p>Release information unavailable.</p>
          )}
        </section>
      ))}
    </div>
  )
}
