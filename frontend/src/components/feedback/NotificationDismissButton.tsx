type Props = {
  label?: string
  onClick: () => void
}

export default function NotificationDismissButton({ label = 'Close notification', onClick }: Props) {
  return (
    <button type="button" className="notification-dismiss" aria-label={label} onClick={onClick}>
      <svg
        width="16"
        height="16"
        viewBox="0 0 16 16"
        fill="none"
        stroke="currentColor"
        strokeWidth="1.5"
        strokeLinecap="round"
        aria-hidden="true"
      >
        <path d="m4 4 8 8m0-8-8 8" />
      </svg>
    </button>
  )
}
