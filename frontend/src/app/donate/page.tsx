'use client'

import KoFiButton from "../../components/KoFiButton"
import Header from "@/components/Header"
import { useUser } from "@/hooks/useUser"
import ErrorNotification from '@/components/ErrorNotification'
import Loading from '@/components/Loading'

import { useEffect, useState } from 'react'


export default function Donate() {
  const [errorMessage, setErrorMessage] = useState<string | null>(null)
  const [copied, setCopied] = useState(false)
  const user = useUser(setErrorMessage);

  useEffect(() => {
    if (!copied) return

    const timer = setTimeout(() => setCopied(false), 2500)
    return () => clearTimeout(timer)
  }, [copied])

  if (!user) return <Loading />

  const handleCopy = async () => {
    try {
      await navigator.clipboard.writeText(user.privateUserObject.id)
      setCopied(true)
    } catch (err) {
      setErrorMessage('We could not copy your supporter ID. Please copy it manually.')
    }
  }

  return (
    <div className="donate-page">
      {errorMessage && (
        <ErrorNotification message={errorMessage} onClose={() => setErrorMessage(null)} />
      )}
      <Header user={user} />
      <main className="donate-content">
        <section className="donate-hero">
          <h1>Fuel the Artist Insight groove</h1>
          <p>
            Every donation helps us surface fresh artists, craft better insights, and keep your release
            radar ahead of the crowd.
          </p>
        </section>

        <section className="user-id-section">
          <div className="user-id-header">
            <h2>Your supporter ID</h2>
            <p>Include this ID with your donation so we can shout you out with a personalized thank you.</p>
          </div>
          <div className="user-id-box" role="group" aria-label="Supporter ID">
            <span className="user-id-value">{user.privateUserObject.id}</span>
            <button type="button" className="copy-button" onClick={handleCopy} aria-label="Copy supporter ID">
              Copy
            </button>
          </div>
          <span className="copy-success" role="status" aria-live="polite">
            {copied ? 'Supporter ID copied to clipboard!' : '\u00A0'}
          </span>
        </section>

        <section className="support-grid" aria-label="Ways to support">
          <article className="support-card kofi-card">
            <h3>Buy us a Ko-fi</h3>
            <p>Keep the playlists spinning by treating the team to a fresh brew.</p>
            <KoFiButton username="N4N11KVW3E" label="Support me on Ko-fi" />
          </article>
          <article className="support-card placeholder-card">
            <h3>Spread the word</h3>
            <p>Share Artist Insight with friends who crave curated discoveries.</p>
          </article>
          <article className="support-card placeholder-card">
            <h3>Stay in tune</h3>
            <p>Follow us on social to catch upcoming features and drop feedback.</p>
          </article>
        </section>
      </main>
    </div>
  )
}
