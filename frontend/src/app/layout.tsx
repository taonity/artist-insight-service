import '../styles/globals.css'
import type { Metadata } from 'next'
import Footer from '@/components/Footer'
import MetricsCollector from '@/components/MetricsCollector'

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
        <MetricsCollector />
        <div className="app-wrapper">
          <main className="app-main">{children}</main>
          <Footer />
        </div>
      </body>
    </html>
  )
}
