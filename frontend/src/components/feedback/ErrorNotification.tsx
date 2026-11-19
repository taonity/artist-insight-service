'use client'

type ErrorNotificationProps = {
  message: string
  onClose?: () => void
}

export default function ErrorNotification({ message, onClose }: ErrorNotificationProps) {
  return (
    <div className="error-notification" role="alert" aria-live="assertive" aria-atomic="true">
      <span>{message}</span>
      {onClose && (
        <button onClick={onClose} aria-label="Close notification">
          ×
        </button>
      )}
    </div>
  )
}
