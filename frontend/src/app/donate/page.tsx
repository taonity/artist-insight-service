'use client'

import KoFiButton from "../../components/KoFiButton"
import Header from "@/components/Header"
import { useUser } from "@/hooks/useUser"
import ErrorNotification from '@/components/ErrorNotification'

import { useEffect, useState } from 'react'


export default function Donate() {
  const [errorMessage, setErrorMessage] = useState<string | null>(null)
  const [copied, setCopied] = useState(false)
  const user = useUser(setErrorMessage);
  const isLoadingUser = !user;

  useEffect(() => {
    if (!copied) return

    const timer = setTimeout(() => setCopied(false), 2500)
    return () => clearTimeout(timer)
  }, [copied])

  const handleCopy = async () => {
    if (!user) return
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
      <Header user={user} loading={isLoadingUser} />
      <main className="donate-content">
        <section className="user-id-section">
          <div className="user-id-header">
            <h2>Your supporter ID</h2>
            <p>Put this ID in &quot;Your message&quot; field in Ko-fi and the service will be able to top up your GPT usages account</p>
            <p>If you have not received GPT usages after donation, please reach the developer in email artiom.diulgher@gmail.com with your supporter ID</p>
          </div>
          <div className={`user-id-box${isLoadingUser ? ' user-id-box--loading' : ''}`} role="group" aria-label="Supporter ID">
            {isLoadingUser ? (
              <span className="user-id-value user-id-value--loading" aria-hidden="true" />
            ) : (
              <span className="user-id-value">{user.privateUserObject.id}</span>
            )}
            <button
              type="button"
              className="copy-button"
              onClick={handleCopy}
              aria-label="Copy supporter ID"
              disabled={isLoadingUser}
            >
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
            <p>Donate the developer to make him deliver new features</p>
            <p>1 USD = 10 GPT usages</p>
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
