'use client'

import Link from 'next/link'
import { useEffect, useState } from 'react'

const CONSENT_STORAGE_KEY = 'artist-insight-cookie-consent'

const CookieBanner = () => {
  const [isOpen, setIsOpen] = useState(false)

  useEffect(() => {
    try {
      const consent = window.localStorage.getItem(CONSENT_STORAGE_KEY)
      if (!consent) {
        setIsOpen(true)
      }
    } catch {
      setIsOpen(true)
    }
  }, [])

  const handleAccept = () => {
    try {
      window.localStorage.setItem(CONSENT_STORAGE_KEY, 'accepted')
    } catch {
      // ignore storage errors – consent cookie below is the source of truth
    }
    document.cookie = `cookie-consent=accepted; path=/; max-age=31536000`
    setIsOpen(false)
  }

  if (!isOpen) {
    return null
  }

  return (
    <div className="cookie-banner" role="dialog" aria-live="polite" aria-label="Cookie notice">
      <div className="cookie-content">
        <p>
          Artist Insight uses essential cookies so you can stay signed in securely and
          keep your session protected. No advertising or analytics cookies are used.
          Learn more in our{' '}
          <Link href="/privacy" className="cookie-link">
            Privacy Policy
          </Link>
          .
        </p>
        <button type="button" onClick={handleAccept} className="cookie-button">
          Got it
        </button>
      </div>
    </div>
  )
}

export default CookieBanner
