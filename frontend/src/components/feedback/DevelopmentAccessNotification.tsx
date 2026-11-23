'use client'

import { useState } from 'react'
import DevelopmentAccessModal from '@/components/feedback/DevelopmentAccessModal'
import ErrorNotification from '@/components/feedback/ErrorNotification'
import NotificationDismissButton from './NotificationDismissButton'
import './DevelopmentAccessNotification.css'

interface DevelopmentAccessNotificationProps {
  message: string
  onClose?: () => void
}

export default function DevelopmentAccessNotification({
  message,
  onClose,
}: DevelopmentAccessNotificationProps) {
  const [isModalOpen, setIsModalOpen] = useState(false)
  const [successMessage, setSuccessMessage] = useState<string | null>(null)
  const [errorModalMessage, setErrorModalMessage] = useState<string | null>(null)

  const handleContactClick = () => {
    setIsModalOpen(true)
  }

  const handleModalSuccess = (value: string) => {
    setSuccessMessage(value)
  }

  const handleModalError = (value: string) => {
    setErrorModalMessage(value)
    setTimeout(() => setErrorModalMessage(null), 5000)
  }

  if (successMessage) {
    return (
      <div className="dev-access-notification success">
        <div className="notification-content">
          <span>{successMessage}</span>
        </div>
        {onClose && <NotificationDismissButton onClick={onClose} />}
      </div>
    )
  }

  return (
    <>
      {errorModalMessage && (
        <ErrorNotification message={errorModalMessage} onClose={() => setErrorModalMessage(null)} />
      )}

      <div className="dev-access-notification warning">
        <div className="notification-content">
          <span>{message}</span>
        </div>
        <div className="notification-actions">
          <button onClick={handleContactClick} className="notification-request-btn">
            Request Access
          </button>
          {onClose && <NotificationDismissButton onClick={onClose} />}
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
