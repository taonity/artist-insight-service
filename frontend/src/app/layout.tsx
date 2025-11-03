import '../styles/globals.css'
import type { Metadata } from 'next'
import Footer from '@/components/Footer'
import CookieBanner from '@/components/CookieBanner'

export const metadata: Metadata = {
  title: 'Artist Insight',
}

export default function RootLayout({
  children,
}: {
  children: React.ReactNode,
}) {
  return (
    <html lang="en">
      <body>
        <div className="app-wrapper">
          <main className="app-main">{children}</main>
          <Footer />
        </div>
        <CookieBanner />
      </body>
    </html>
  )
}
