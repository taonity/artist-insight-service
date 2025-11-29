'use client'

import { useState, useEffect } from 'react'
import { getRuntimeConfig } from '@/lib/runtimeConfig'
import './DevelopmentAccessModal.css'

interface DevelopmentAccessModalProps {
  isOpen: boolean
  onClose: () => void
  onSuccess?: (message: string) => void
  onError?: (message: string) => void
}

function getCookie(name: string) {
  if (typeof document === 'undefined') {
    return null
  }
  const match = document.cookie.match(new RegExp('(^| )' + name + '=([^;]+)'))
  return match ? decodeURIComponent(match[2]) : null
}

export default function DevelopmentAccessModal({
  isOpen,
  onClose,
  onSuccess,
  onError
}: DevelopmentAccessModalProps) {
  const [email, setEmail] = useState('')
  const [message, setMessage] = useState('')
  const [isSubmitting, setIsSubmitting] = useState(false)
  const [errors, setErrors] = useState<{ [key: string]: string }>({})
  const [csrfCookieName, setCsrfCookieName] = useState('XSRF-TOKEN')

  useEffect(() => {
    getRuntimeConfig().then(config => setCsrfCookieName(config.csrfCookieName))
  }, [])

  const resetForm = () => {
    setEmail('')
    setMessage('')
    setErrors({})
  }

  const handleClose = () => {
    resetForm()
    onClose()
  }

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    setIsSubmitting(true)
    setErrors({})

    const xsrfToken = getCookie(csrfCookieName)
    if (!xsrfToken) {
      onError?.('Unable to verify your request. Please refresh the page and try again.')
      setIsSubmitting(false)
      return
    }

    try {
      const response = await fetch('/api/development-access-request', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'X-XSRF-TOKEN': xsrfToken,
        },
        credentials: 'include',
        body: JSON.stringify({ email, message })
      })

      if (response.ok) {
        onSuccess?.('Your request has been submitted successfully.')
        handleClose()
      } else {
        const data = await response.json()
        if (data.errors) {
          setErrors(data.errors)
        } else {
          onError?.('Failed to submit your request. Please try again later.')
        }
      }
    } catch (err) {
      onError?.('Network error. Please try again.')
    } finally {
      setIsSubmitting(false)
    }
  }

  if (!isOpen) return null

  return (
    <div className="modal-overlay" onClick={handleClose}>
      <div className="modal-content" onClick={(e) => e.stopPropagation()}>
        <div className="modal-header">
          <h2>Request Development Access</h2>
          <button className="modal-close" onClick={handleClose} aria-label="Close modal">
            ×
          </button>
        </div>
        
        <div className="modal-body">
          <p>Your Spotify account is not currently authorized for this application. Please provide your contact information to request development access.</p>
          
          <form onSubmit={handleSubmit}>
            <div className="form-group">
              <label htmlFor="email">Email Address *</label>
              <input
                type="email"
                id="email"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                className={errors.email ? 'error' : ''}
                required
                disabled={isSubmitting}
                placeholder="your.email@example.com"
              />
              {errors.email && <span className="error-text">{errors.email}</span>}
            </div>

            <div className="form-group">
              <label htmlFor="message">Additional Information *</label>
              <textarea
                id="message"
                value={message}
                onChange={(e) => setMessage(e.target.value)}
                className={errors.message ? 'error' : ''}
                disabled={isSubmitting}
                placeholder="Please explain why you need access to this application or provide any additional details about your use case."
                rows={4}
                maxLength={2000}
                required
              />
              {errors.message && <span className="error-text">{errors.message}</span>}
              <small className="char-count">{message.length}/2000</small>
            </div>

            <div className="modal-actions">
              <button
                type="button"
                className="btn btn-secondary"
                onClick={handleClose}
                disabled={isSubmitting}
              >
                Cancel
              </button>
              <button
                type="submit"
                className="btn btn-primary"
                disabled={isSubmitting || !email.trim() || !message.trim()}
              >
                {isSubmitting ? 'Submitting...' : 'Request Access'}
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>
  )
}