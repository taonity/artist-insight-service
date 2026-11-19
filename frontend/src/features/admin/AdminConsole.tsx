'use client'

import { useCallback, useEffect, useMemo, useState } from 'react'
import Image from 'next/image'
import Link from 'next/link'
import AdminTable, { type Column } from './AdminTable'
import SkeletonSlot from './SkeletonSlot'
import { adminRequest, type Access, type Role, type Setting, type UserRow } from './api'
import { useDevLoading } from '@/features/dev/DevTools'
import AdminReleaseInfo from '@/features/info/ReleaseInfo'
import './admin.css'

type RecordRow = Record<string, string>
const tabs = ['users', 'requests', 'settings', 'audit', 'about'] as const
type Tab = (typeof tabs)[number]
const sections: Record<Tab, { label: string; title: string; description: string }> = {
  users: {
    label: 'Listeners',
    title: 'Listeners',
    description: 'Account details, enrichment credits and console access.',
  },
  requests: {
    label: 'Access requests',
    title: 'Spotify access requests',
    description:
      'Review requests and email delivery. Grant development access in the Spotify developer dashboard.',
  },
  settings: {
    label: 'Settings',
    title: 'Runtime settings',
    description:
      'Update application policies. Changes to allowances and share lifetimes apply to future operations.',
  },
  audit: {
    label: 'Audit log',
    title: 'Audit log',
    description: 'A history of console permissions and configuration changes.',
  },
  about: {
    label: 'About',
    title: 'Release information',
    description: 'The versions currently running behind Artist Insight.',
  },
}
function Profile({
  name,
  avatar,
  detail,
}: {
  name: string
  avatar?: string | null
  detail?: string
}) {
  return (
    <span className="admin-profile">
      <span className="admin-avatar">
        {avatar ? (
          <Image src={avatar} alt="" width={32} height={32} unoptimized />
        ) : (
          <span>
            {name
              .split(/[ .@-]+/)
              .slice(0, 2)
              .map((p) => p[0])
              .join('')
              .toUpperCase()}
          </span>
        )}
      </span>
      <span className="admin-profile-copy">
        <span className="admin-profile-name">{name}</span>
        {detail && <span className="admin-profile-detail">{detail}</span>}
      </span>
    </span>
  )
}
function Status({ value }: { value: string }) {
  return (
    <span className={`admin-status admin-status-${value.toLowerCase()}`}>
      {{ SENT: 'Sent', PENDING: 'Pending', FAILED: 'Failed' }[value] || value}
    </span>
  )
}
const requestColumns: Column<RecordRow>[] = [
  {
    key: 'email',
    mobilePrimary: true,
    label: 'Requester',
    sortKey: 'email',
    defaultWidth: 300,
    render: (row) => <Profile name={row.email || ''} />,
    placeholder: <Profile name="Listener Example" />,
  },
  {
    key: 'message',
    mobileWide: true,
    label: 'Message',
    defaultWidth: 470,
    placeholder: 'I would like to explore the genres in my followings.',
  },
  {
    key: 'status',
    label: 'Email delivery',
    sortKey: 'status',
    defaultWidth: 145,
    render: (row) => <Status value={row.status || 'PENDING'} />,
    placeholder: <Status value="PENDING" />,
  },
  {
    key: 'createdAt',
    label: 'Submitted',
    sortKey: 'createdAt',
    defaultWidth: 170,
    render: (row) => <time dateTime={row.createdAt}>{formatTime(row.createdAt)}</time>,
    placeholder: '19 Nov 2026, 12:30',
  },
]
const auditColumns: Column<RecordRow>[] = [
  {
    key: 'occurredAt',
    label: 'Time (UTC)',
    sortKey: 'occurredAt',
    defaultWidth: 175,
    render: (row) => <time dateTime={row.occurredAt}>{formatTime(row.occurredAt)}</time>,
    placeholder: '19 Nov 2026, 12:30',
  },
  {
    key: 'action',
    label: 'Action',
    sortKey: 'action',
    defaultWidth: 155,
    render: (row) => (
      <span className="admin-action">{(row.action || '').toLowerCase().replaceAll('_', ' ')}</span>
    ),
    placeholder: 'Change role',
  },
  {
    key: 'actorSpotifyId',
    label: 'Actor',
    sortKey: 'actorSpotifyId',
    defaultWidth: 220,
    mobileWide: true,
  },
  { key: 'targetType', label: 'Type', sortKey: 'targetType', defaultWidth: 95 },
  { key: 'targetId', label: 'Target', sortKey: 'targetId', defaultWidth: 255 },
  { key: 'beforeValue', label: 'Before', defaultWidth: 110 },
  { key: 'afterValue', label: 'After', defaultWidth: 110 },
]
function formatTime(value?: string) {
  return value
    ? new Intl.DateTimeFormat('en-GB', {
        day: '2-digit',
        month: 'short',
        year: 'numeric',
        hour: '2-digit',
        minute: '2-digit',
        timeZone: 'UTC',
      }).format(new Date(value))
    : '-'
}

export default function AdminConsole() {
  const preview = useDevLoading()
  const [access, setAccess] = useState<Access | null>(null)
  const [error, setError] = useState('')
  const [tab, setTab] = useState<Tab>('users')
  const [visited, setVisited] = useState<Set<Tab>>(() => new Set(['users']))
  const [refresh, setRefresh] = useState<Record<Tab, number>>({
    users: 0,
    requests: 0,
    settings: 0,
    audit: 0,
    about: 0,
  })
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
    } catch {}
  }, [])
  function selectTab(next: Tab) {
    setVisited((v) => new Set([...v, next]))
    setTab(next)
    setRefresh((v) => ({ ...v, [next]: v[next] + 1 }))
    try {
      localStorage.setItem('artist-insight.admin.tab', next)
    } catch {}
  }
  const changeRole = useCallback(async (user: UserRow, role: Role) => {
    if (
      user.role === role ||
      !window.confirm(`Change ${user.displayName}'s console role from ${user.role} to ${role}?`)
    )
      return
    try {
      await adminRequest(`/users/${encodeURIComponent(user.spotifyId)}/role`, 'PUT', { role })
      setRefresh((v) => ({ ...v, users: v.users + 1, audit: v.audit + 1 }))
    } catch (e) {
      setError(e instanceof Error ? e.message : 'Role change failed')
    }
  }, [])
  const userColumns = useMemo<Column<UserRow>[]>(
    () => [
      {
        key: 'displayName',
        mobilePrimary: true,
        label: 'Listener',
        sortKey: 'displayName',
        defaultWidth: 330,
        render: (user) => (
          <Profile name={user.displayName} avatar={user.avatarUrl} detail={user.spotifyId} />
        ),
        placeholder: <Profile name="Listener Example" detail="demo-listener-000" />,
      },
      {
        key: 'spotifyId',
        label: 'Spotify ID',
        sortKey: 'spotifyId',
        defaultWidth: 280,
        render: (user) => <span className="admin-id">{user.spotifyId}</span>,
        placeholder: 'demo-listener-000',
      },
      {
        key: 'gptUsagesLeft',
        label: 'GPT credits',
        sortKey: 'gptUsagesLeft',
        defaultWidth: 150,
        align: 'right',
        render: (user) => (
          <span
            className={`admin-credits ${user.gptUsagesLeft === 0 ? 'admin-credits-empty' : ''}`}
          >
            {user.gptUsagesLeft.toLocaleString()}
          </span>
        ),
        placeholder: '120',
      },
      {
        key: 'role',
        label: 'Console access',
        sortKey: 'consoleRole',
        defaultWidth: 180,
        render: (user) =>
          access?.isOwner && user.spotifyId !== access.spotifyId ? (
            <select
              className="admin-role-select"
              aria-label={`Console role for ${user.displayName}`}
              value={user.role}
              disabled={preview}
              onChange={(e) => void changeRole(user, e.target.value as Role)}
            >
              {(['NONE', 'ADMIN', 'OWNER'] as const).map((role) => (
                <option key={role} value={role}>
                  {role === 'NONE' ? 'Listener' : role === 'ADMIN' ? 'Admin' : 'Owner'}
                </option>
              ))}
            </select>
          ) : (
            <span className={`admin-role-pill admin-role-${user.role.toLowerCase()}`}>
              {user.role === 'NONE' ? 'Listener' : user.role === 'ADMIN' ? 'Admin' : 'Owner'}
            </span>
          ),
        placeholder: <span className="admin-role-pill">Admin</span>,
      },
    ],
    [access, changeRole, preview],
  )
  const pending = access === null
  const allowed = access?.isAdmin === true
  const section = sections[tab]
  return (
    <div className="admin-page">
      <header className="admin-header">
        <div>
          <Link className="admin-back" href="/">
            Artist Insight <span aria-hidden="true">/</span> Console
          </Link>
          <h1>Administration</h1>
        </div>
        <SkeletonSlot loading={pending || preview} className="admin-role-slot">
          <span className="admin-owner-badge">{access?.role === 'ADMIN' ? 'Admin' : 'Owner'}</span>
        </SkeletonSlot>
      </header>
      {error && (
        <div className="admin-error" role="alert">
          {error}
          <button className="admin-text-button" onClick={() => setError('')}>
            Dismiss
          </button>
        </div>
      )}
      {access && !allowed ? (
        <p>
          You do not have administrator access. Your Spotify account can continue using Artist
          Insight.
        </p>
      ) : (
        <>
          <nav className="admin-navigation" aria-label="Admin sections">
            <label className="admin-mobile-navigation">
              <span className="admin-sr-only">Admin section</span>
              <select value={tab} onChange={(e) => selectTab(e.target.value as Tab)}>
                {tabs.map((t) => (
                  <option key={t} value={t}>
                    {sections[t].label}
                  </option>
                ))}
              </select>
            </label>
            <div className="admin-tabs" role="tablist" aria-label="Console sections">
              {tabs.map((t) => (
                <button
                  key={t}
                  id={`admin-tab-${t}`}
                  role="tab"
                  aria-selected={tab === t}
                  aria-controls={`admin-panel-${t}`}
                  tabIndex={tab === t ? 0 : -1}
                  onClick={() => selectTab(t)}
                  onKeyDown={(e) => {
                    let i = tabs.indexOf(t)
                    if (e.key === 'ArrowRight') i = (i + 1) % tabs.length
                    else if (e.key === 'ArrowLeft') i = (i + tabs.length - 1) % tabs.length
                    else if (e.key === 'Home') i = 0
                    else if (e.key === 'End') i = tabs.length - 1
                    else return
                    e.preventDefault()
                    const next = tabs[i]!
                    selectTab(next)
                    document.getElementById(`admin-tab-${next}`)?.focus()
                  }}
                >
                  {sections[t].label}
                </button>
              ))}
            </div>
          </nav>
          <div className="admin-section-heading">
            <h2>{section.title}</h2>
            <p>{section.description}</p>
          </div>
          <div
            id="admin-panel-users"
            role="tabpanel"
            aria-labelledby="admin-tab-users"
            hidden={tab !== 'users'}
          >
            {visited.has('users') && (
              <AdminTable
                endpoint="/users"
                rowKey="spotifyId"
                columns={userColumns}
                defaultSort="displayName"
                refreshKey={refresh.users}
                enabled={allowed && tab === 'users'}
                searchLabel="Search listeners..."
              />
            )}
          </div>
          <div
            id="admin-panel-requests"
            role="tabpanel"
            aria-labelledby="admin-tab-requests"
            hidden={tab !== 'requests'}
          >
            {visited.has('requests') && (
              <AdminTable
                endpoint="/development-access-requests"
                rowKey="id"
                columns={requestColumns}
                defaultSort="createdAt"
                refreshKey={refresh.requests}
                enabled={allowed && tab === 'requests'}
                searchLabel="Search requests..."
              />
            )}
          </div>
          <div
            id="admin-panel-settings"
            role="tabpanel"
            aria-labelledby="admin-tab-settings"
            hidden={tab !== 'settings'}
          >
            {visited.has('settings') && (
              <Settings
                enabled={allowed && tab === 'settings'}
                canEdit={access?.isOwner === true}
                refresh={refresh.settings}
              />
            )}
          </div>
          <div
            id="admin-panel-audit"
            role="tabpanel"
            aria-labelledby="admin-tab-audit"
            hidden={tab !== 'audit'}
          >
            {visited.has('audit') && (
              <AdminTable
                endpoint="/audit-logs"
                rowKey="id"
                columns={auditColumns}
                defaultSort="occurredAt"
                refreshKey={refresh.audit}
                enabled={allowed && tab === 'audit'}
                searchLabel="Search audit history..."
                audit
              />
            )}
          </div>
          <div
            id="admin-panel-about"
            role="tabpanel"
            aria-labelledby="admin-tab-about"
            hidden={tab !== 'about'}
          >
            {visited.has('about') && <AdminReleaseInfo enabled={allowed && tab === 'about'} />}
          </div>
        </>
      )}
    </div>
  )
}
const placeholders: Setting[] = [
  ['app.initial-user-gpt-usages', 'New user GPT credits', 'INT', 10],
  ['app.donation-credit-price', 'Donation amount per credit', 'DECIMAL', 0.1],
  ['app.share-lifetime-days', 'Share lifetime (days)', 'INT', 30],
  ['app.retention.audit-days', 'Audit retention (days)', 'INT', 90],
  ['app.retention.audit-cron', 'Audit cleanup cron (UTC)', 'CRON', '0 30 3 * * *'],
  ['app.console.max-page-size', 'Maximum admin page size', 'INT', 100],
].map(([key, label, type, value]) => ({
  key: String(key),
  label: String(label),
  type: String(type),
  value: value!,
  defaultValue: value!,
  overridden: false,
  min: null,
  max: null,
}))
function Settings({
  enabled,
  canEdit,
  refresh,
}: {
  enabled: boolean
  canEdit: boolean
  refresh: number
}) {
  const preview = useDevLoading()
  const [fields, setFields] = useState<Setting[] | null>(null)
  const [error, setError] = useState('')
  const [notice, setNotice] = useState('')
  const [busy, setBusy] = useState(false)
  useEffect(() => {
    if (!enabled) return
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
  }, [enabled, refresh])
  const loading = preview || fields === null
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
    <section className="admin-settings" aria-busy={loading || busy}>
      <div className="admin-setting-notice" role={error ? 'alert' : 'status'}>
        {error || notice || '\u00a0'}
      </div>
      <div className="admin-settings-list">
        {(fields ?? placeholders).map((field) => (
          <form
            key={`${field.key}-${field.value}`}
            className="admin-setting-row"
            onSubmit={(e) => {
              e.preventDefault()
              const value = new FormData(e.currentTarget).get('value') as string
              void mutate('/config', 'PUT', {
                values: { [field.key]: field.type === 'CRON' ? value : Number(value) },
              })
            }}
          >
            <div className="admin-setting-copy">
              <label htmlFor={`setting-${field.key}`}>
                <SkeletonSlot loading={loading}>{field.label}</SkeletonSlot>
              </label>
              <small>
                <SkeletonSlot loading={loading}>
                  Default: {String(field.defaultValue)}
                  {field.overridden ? ' (overridden)' : ''}
                </SkeletonSlot>
              </small>
            </div>
            <div className="admin-setting-editor">
              <div className={`admin-input-slot ${loading ? 'admin-input-loading' : ''}`}>
                <input
                  id={`setting-${field.key}`}
                  name="value"
                  type={field.type === 'CRON' ? 'text' : 'number'}
                  defaultValue={field.value}
                  min={field.min ?? undefined}
                  max={field.max ?? undefined}
                  step={field.type === 'DECIMAL' ? 'any' : '1'}
                  required
                  disabled={!canEdit || busy || loading}
                />
                {loading && <span className="admin-slot-fill" />}
              </div>
              <div className="admin-setting-actions">
                <button
                  className="admin-button admin-button-primary"
                  disabled={!canEdit || busy || loading}
                >
                  Save
                </button>
                <button
                  className="admin-button"
                  type="button"
                  disabled={!canEdit || !field.overridden || busy || loading}
                  onClick={() => void mutate(`/config/${encodeURIComponent(field.key)}`, 'DELETE')}
                >
                  Reset
                </button>
              </div>
            </div>
          </form>
        ))}
      </div>
      <p className="admin-settings-footnote">
        Only owners can change policies. Existing credit balances and link expiry dates stay as
        recorded.
      </p>
    </section>
  )
}
