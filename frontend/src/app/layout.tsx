import '../styles/theme.css'
import '../styles/globals.css'
import '../styles/responsive.css'
import type { Metadata, Viewport } from 'next'
import { Righteous } from 'next/font/google'
import CookieBanner from '@/components/layout/CookieBanner'
import DevTools from '@/features/dev/DevTools'
import Footer from '@/components/layout/Footer'
import ThemeProvider from '@/components/theme/ThemeProvider'
import { THEME_COLORS, themeInitScript } from '@/components/theme/theme'

const righteous = Righteous({
  weight: '400',
  subsets: ['latin'],
  variable: '--font-righteous',
})

export const metadata: Metadata = {
  title: 'Artist Insight',
  applicationName: 'Artist Insight',
  description: 'Share the artists you follow on Spotify and explore your music taste.',
  icons: { apple: '/icons/apple-touch-icon.png' },
  appleWebApp: { capable: true, title: 'Artist Insight', statusBarStyle: 'default' },
}

export const viewport: Viewport = {
  themeColor: THEME_COLORS.light,
}

export default function RootLayout({ children }: { children: React.ReactNode }) {
  return (
    <html lang="en" suppressHydrationWarning>
      <head>
        <script id="theme-init" dangerouslySetInnerHTML={{ __html: themeInitScript }} />
      </head>
      <body className={righteous.variable}>
        <ThemeProvider>
          <div className="app-wrapper">
            <main className="app-main">{children}</main>
            <Footer />
          </div>
          <CookieBanner />
          <DevTools />
        </ThemeProvider>
      </body>
    </html>
  )
}
