'use client'
import { useEffect, useState } from 'react'
import { fetchWithTimeout } from '@/lib/clientApi'
import { useDevLoading } from '@/features/dev/DevTools'
import SkeletonSlot from '@/features/admin/SkeletonSlot'
type Info = {
  app?: { version?: string }
  build?: { version?: string; time?: string }
  git?: { commit?: string | { id?: string | { full?: string } } }
  runtime?: { uptime?: number }
}
const sources = [
  ['Frontend', '/api/actuator/info'],
  ['Backend', '/api/actuator/backend'],
] as const
export default function AdminReleaseInfo({ enabled }: { enabled: boolean }) {
  const preview = useDevLoading()
  const [data, setData] = useState<Array<Info | null> | null>(null)
  useEffect(() => {
    if (!enabled) return
    let active = true
    void Promise.all(
      sources.map(async ([, url]) => {
        try {
          const r = await fetchWithTimeout(url, { timeoutMs: 8000 })
          return r.ok ? ((await r.json()) as Info) : null
        } catch {
          return null
        }
      }),
    ).then((value) => {
      if (active) setData(value)
    })
    return () => {
      active = false
    }
  }, [enabled])
  const loading = preview || data === null
  return (
    <div className="admin-release-grid">
      {sources.map(([label], index) => {
        const info = data?.[index]
        const c = info?.git?.commit
        const commit = typeof c === 'string' ? c : typeof c?.id === 'string' ? c.id : c?.id?.full
        const rows = [
          ['Version', info?.app?.version || info?.build?.version || 'Unavailable'],
          ['Revision', commit?.slice(0, 7) || 'Unavailable'],
          [
            'Built',
            info?.build?.time
              ? new Date(info.build.time).toLocaleString('en-GB', { timeZone: 'UTC' }) + ' UTC'
              : 'Unavailable',
          ],
          [
            'Uptime',
            info?.runtime?.uptime !== undefined
              ? `${Math.floor(info.runtime.uptime)} seconds`
              : 'Not reported',
          ],
        ]
        return (
          <section className="admin-release-card" key={label}>
            <header>
              <h3>{label}</h3>
              <span
                className="admin-release-dot"
                aria-label={loading ? 'Loading' : info ? 'Available' : 'Unavailable'}
              />
            </header>
            <dl>
              {rows.map(([key, value]) => (
                <div key={key}>
                  <dt>{key}</dt>
                  <dd>
                    <SkeletonSlot loading={loading}>{value}</SkeletonSlot>
                  </dd>
                </div>
              ))}
            </dl>
          </section>
        )
      })}
    </div>
  )
}
