'use client'
import { useCallback, useEffect, useMemo, useState } from 'react'
import Link from 'next/link'
import AdminTable, { type Column } from './AdminTable'
import { adminRequest, type Access, type Role, type Setting, type UserRow } from './api'
import ReleaseInfo from '@/features/info/ReleaseInfo'
import './admin.css'
type RecordRow = Record<string, string>
const requestColumns: Column<RecordRow>[] = [
  { key: 'email', label: 'Email', sortKey: 'email', defaultWidth: 240 },
  { key: 'message', label: 'Message', defaultWidth: 320 },
  { key: 'status', label: 'Email delivery', sortKey: 'status', defaultWidth: 150 },
  { key: 'createdAt', label: 'Submitted', sortKey: 'createdAt', defaultWidth: 220 },
]
const auditColumns: Column<RecordRow>[] = [
  { key: 'occurredAt', label: 'Time', sortKey: 'occurredAt', defaultWidth: 220 },
  { key: 'action', label: 'Action', sortKey: 'action', defaultWidth: 160 },
  { key: 'actorSpotifyId', label: 'Actor', sortKey: 'actorSpotifyId', defaultWidth: 220 },
  { key: 'targetType', label: 'Type', sortKey: 'targetType', defaultWidth: 120 },
  { key: 'targetId', label: 'Target', sortKey: 'targetId', defaultWidth: 240 },
  { key: 'beforeValue', label: 'Before', defaultWidth: 140 },
  { key: 'afterValue', label: 'After', defaultWidth: 140 },
]
const tabs = ['users', 'requests', 'settings', 'audit', 'about'] as const
type Tab = (typeof tabs)[number]
export default function AdminConsole() {
  const [access, setAccess] = useState<Access | null>(null)
  const [error, setError] = useState('')
  const [tab, setTab] = useState<Tab>('users')
  const [refresh, setRefresh] = useState(0)
  const [visited, setVisited] = useState<Set<Tab>>(() => new Set(['users']))
  useEffect(() => {
    void adminRequest<Access>('/access/me')
      .then(setAccess)
      .catch((e) => setError(e.message))
  }, [])
  useEffect(() => {
    try {
      const saved = localStorage.getItem('artist-insight.admin.tab')
      if (tabs.includes(saved as Tab)) {
        setTab(saved as Tab)
        setVisited((v) => new Set([...v, saved as Tab]))
      }
    } catch {
      /* Optional persistence */
    }
  }, [])
  function selectTab(next: Tab) {
    setVisited((v) => new Set([...v, next]))
    setTab(next)
    setRefresh((v) => v + 1)
    try {
      localStorage.setItem('artist-insight.admin.tab', next)
    } catch {
      /* Optional persistence */
    }
  }
  const changeRole = useCallback(async (user: UserRow, role: Role) => {
    if (
      user.role === role ||
      !window.confirm(`Change ${user.displayName}'s console role from ${user.role} to ${role}?`)
    )
      return
    try {
      await adminRequest(`/users/${encodeURIComponent(user.spotifyId)}/role`, 'PUT', { role })
      setRefresh((v) => v + 1)
    } catch (e) {
      setError(e instanceof Error ? e.message : 'Role change failed')
    }
  }, [])
  const userColumns = useMemo<Column<UserRow>[]>(
    () => [
      { key: 'spotifyId', label: 'Spotify ID', sortKey: 'spotifyId', defaultWidth: 240 },
      { key: 'displayName', label: 'Name', sortKey: 'displayName', defaultWidth: 200 },
      { key: 'gptUsagesLeft', label: 'GPT credits', sortKey: 'gptUsagesLeft', defaultWidth: 120 },
      {
        key: 'role',
        label: 'Console role',
        sortKey: 'consoleRole',
        defaultWidth: 160,
        render: (user) =>
          access?.isOwner && user.spotifyId !== access.spotifyId ? (
            <select
              aria-label={`Console role for ${user.displayName}`}
              value={user.role}
              onChange={(e) => void changeRole(user, e.target.value as Role)}
            >
              {(['NONE', 'ADMIN', 'OWNER'] as const).map((role) => (
                <option key={role}>{role}</option>
              ))}
            </select>
          ) : (
            user.role
          ),
      },
    ],
    [access, changeRole],
  )
  return (
    <div className="admin-page">
      <header>
        <Link href="/">Back to Artist Insight</Link>
        <h1>Administration</h1>
        {access && <span>{access.role}</span>}
      </header>
      {error && (
        <p role="alert">
          {error}
          <button onClick={() => setError('')} aria-label="Dismiss error">
            ?
          </button>
        </p>
      )}
      {!access ? (
        <p role="status">{error ? 'Unable to load access information.' : 'Checking access...'}</p>
      ) : !access.isAdmin ? (
        <p>
          You do not have administrator access. Your Spotify account can continue using Artist
          Insight.
        </p>
      ) : (
        <>
          <nav aria-label="Admin sections">
            <select
              aria-label="Admin section"
              value={tab}
              onChange={(e) => selectTab(e.target.value as Tab)}
            >
              {tabs.map((t) => (
                <option key={t}>{t}</option>
              ))}
            </select>
            <div className="admin-tabs">
              {tabs.map((t) => (
                <button
                  key={t}
                  aria-current={tab === t ? 'page' : undefined}
                  onClick={() => selectTab(t)}
                >
                  {t}
                </button>
              ))}
            </div>
          </nav>
          <h2>
            {
              {
                users: 'Users',
                requests: 'Spotify development access',
                settings: 'Runtime settings',
                audit: 'Audit history',
                about: 'Release information',
              }[tab]
            }
          </h2>
          <div hidden={tab !== 'users'}>
            {visited.has('users') && (
              <AdminTable
                key="users"
                endpoint="/users"
                rowKey="spotifyId"
                columns={userColumns}
                defaultSort="spotifyId"
                refreshKey={refresh}
              />
            )}
          </div>
          <div hidden={tab !== 'requests'}>
            {visited.has('requests') && (
              <>
                <p>
                  Email delivery status is shown here. Spotify development access is granted
                  separately in the Spotify developer dashboard.
                </p>
                <AdminTable
                  key="requests"
                  endpoint="/development-access-requests"
                  rowKey="id"
                  columns={requestColumns}
                  defaultSort="createdAt"
                  refreshKey={refresh}
                />
              </>
            )}
          </div>
          <div hidden={tab !== 'settings'}>
            {visited.has('settings') && <Settings canEdit={access.isOwner} refresh={refresh} />}
          </div>
          <div hidden={tab !== 'audit'}>
            {visited.has('audit') && (
              <AdminTable
                key="audit"
                endpoint="/audit-logs"
                rowKey="id"
                columns={auditColumns}
                defaultSort="occurredAt"
                refreshKey={refresh}
                audit
              />
            )}
          </div>
          {tab === 'about' && <ReleaseInfo />}
        </>
      )}
    </div>
  )
}
function Settings({ canEdit, refresh }: { canEdit: boolean; refresh: number }) {
  const [fields, setFields] = useState<Setting[] | null>(null)
  const [error, setError] = useState('')
  const [notice, setNotice] = useState('')
  const [busy, setBusy] = useState(false)
  useEffect(() => {
    let active = true
    void adminRequest<Setting[]>('/config')
      .then((f) => {
        if (active) setFields(f)
      })
      .catch((e) => {
        if (active) setError(e.message)
      })
    return () => {
      active = false
    }
  }, [refresh])
  async function mutate(path: string, method: string, body?: unknown) {
    setBusy(true)
    setError('')
    setNotice('')
    try {
      setFields(await adminRequest<Setting[]>(path, method, body))
      setNotice('Settings saved.')
    } catch (e) {
      setError(e instanceof Error ? e.message : 'Settings update failed')
    } finally {
      setBusy(false)
    }
  }
  return (
    <section>
      <p>
        Only owners can change settings. New-user credits and share lifetime affect future
        operations; existing balances and link expiry dates stay as recorded.
      </p>
      {error && <p role="alert">{error}</p>}
      {notice && <p role="status">{notice}</p>}
      {!fields && !error && <p role="status">Loading settings...</p>}
      {fields?.map((field) => (
        <form
          key={`${field.key}-${field.value}`}
          className="admin-setting"
          onSubmit={(e) => {
            e.preventDefault()
            const value = new FormData(e.currentTarget).get('value') as string
            void mutate('/config', 'PUT', {
              values: { [field.key]: field.type === 'CRON' ? value : Number(value) },
            })
          }}
        >
          <label>
            {field.label}
            <input
              name="value"
              type={field.type === 'CRON' ? 'text' : 'number'}
              defaultValue={field.value}
              min={field.min ?? undefined}
              max={field.max ?? undefined}
              step={field.type === 'DECIMAL' ? 'any' : '1'}
              required
              disabled={!canEdit || busy}
            />
          </label>
          <small>
            Default: {String(field.defaultValue)} | {field.overridden ? 'Override' : 'Default'}
          </small>
          <button disabled={!canEdit || busy}>Save</button>
          <button
            type="button"
            disabled={!canEdit || !field.overridden || busy}
            onClick={() => void mutate(`/config/${encodeURIComponent(field.key)}`, 'DELETE')}
          >
            Reset
          </button>
        </form>
      ))}
    </section>
  )
}
