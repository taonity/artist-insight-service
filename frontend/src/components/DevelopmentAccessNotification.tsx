'use client'

import { useState } from 'react'
import DevelopmentAccessModal from './DevelopmentAccessModal'
import ErrorNotification from './ErrorNotification'
import './DevelopmentAccessNotification.css'

interface DevelopmentAccessNotificationProps {
  message: string
  onClose?: () => void
}

export default function DevelopmentAccessNotification({ 
  message, 
  onClose 
}: DevelopmentAccessNotificationProps) {
  const [isModalOpen, setIsModalOpen] = useState(false)
  const [successMessage, setSuccessMessage] = useState<string | null>(null)
  const [errorModalMessage, setErrorModalMessage] = useState<string | null>(null)

  const handleContactClick = () => {
    setIsModalOpen(true)
  }

  const handleModalSuccess = (message: string) => {
    setSuccessMessage(message)
  }

  const handleModalError = (message: string) => {
    setErrorModalMessage(message)
    setTimeout(() => setErrorModalMessage(null), 5000)
  }

  if (successMessage) {
    return (
      <div className="dev-access-notification success">
        <div className="notification-content">
          <span>{successMessage}</span>
        </div>
        {onClose && (
          <button 
            onClick={onClose}
            className="notification-close-btn"
            aria-label="Close notification"
          >
            ×
          </button>
        )}
      </div>
    )
  }

  return (
    <>
      {errorModalMessage && (
        <ErrorNotification 
          message={errorModalMessage} 
          onClose={() => setErrorModalMessage(null)} 
        />
      )}
      
      <div className="dev-access-notification warning">
        <div className="notification-content">
          <span>{message}</span>
        </div>
        <div className="notification-actions">
          <button 
            onClick={handleContactClick}
            className="notification-request-btn"
          >
            Request Access
          </button>
          {onClose && (
            <button 
              onClick={onClose}
              className="notification-close-btn"
              aria-label="Close notification"
            >
              ×
            </button>
          )}
        </div>
      </div>

      <DevelopmentAccessModal
        isOpen={isModalOpen}
        onClose={() => setIsModalOpen(false)}
        onSuccess={handleModalSuccess}
        onError={handleModalError}
      />
    </>
  )
}