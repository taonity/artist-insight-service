import Link from 'next/link'

export default function NotFound() {
  return (
    <div className="not-found-container">
      <h1>404</h1>
      <p>We couldn't find that page.</p>
      <Link href="/" className="button">
        Go home
      </Link>
    </div>
  )
}
