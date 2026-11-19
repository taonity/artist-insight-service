import '../styles/globals.css'
import '../styles/responsive.css'
import type { Metadata } from 'next'
import { Righteous } from 'next/font/google'
import CookieBanner from '@/components/layout/CookieBanner'
import DevTools from '@/features/dev/DevTools'
import Footer from '@/components/layout/Footer'

const righteous = Righteous({
  weight: '400',
  subsets: ['latin'],
  variable: '--font-righteous',
})

export const metadata: Metadata = {
  title: 'Artist Insight',
}

export default function RootLayout({ children }: { children: React.ReactNode }) {
  return (
    <html lang="en">
      <body className={righteous.variable}>
        <div className="app-wrapper">
          <main className="app-main">{children}</main>
          <Footer />
        </div>
        <CookieBanner />
        <DevTools />
      </body>
    </html>
  )
}
