import '../styles/theme.css'
import '../styles/globals.css'
import '../styles/responsive.css'
import type { Metadata, Viewport } from 'next'
import { Righteous } from 'next/font/google'
import { connection } from 'next/server'
import CookieBanner from '@/components/layout/CookieBanner'
import DevTools from '@/features/dev/DevTools'
import Footer from '@/components/layout/Footer'
import ThemeProvider from '@/components/theme/ThemeProvider'
import { THEME_COLORS, themeInitScript } from '@/components/theme/theme'
import { getAppIcons } from '@/lib/appIcons'

const righteous = Righteous({
  weight: '400',
  subsets: ['latin'],
  variable: '--font-righteous',
})

export async function generateMetadata(): Promise<Metadata> {
  await connection()
  const icons = getAppIcons()
  return {
    title: 'Artist Insight',
    applicationName: 'Artist Insight',
    description: 'Share the artists you follow on Spotify and explore your music taste.',
    icons: {
      icon: [{ url: icons.favicon, sizes: '16x16 32x32 48x48' }],
      apple: [{ url: icons.appleTouchIcon, sizes: '180x180', type: 'image/png' }],
    },
    appleWebApp: { capable: true, title: 'Artist Insight', statusBarStyle: 'default' },
  }
}

export const viewport: Viewport = {
  themeColor: [
    { media: '(prefers-color-scheme: light)', color: THEME_COLORS.light },
    { media: '(prefers-color-scheme: dark)', color: THEME_COLORS.dark },
  ],
  colorScheme: 'light dark',
  viewportFit: 'cover',
}

export default function RootLayout({ children }: { children: React.ReactNode }) {
  return (
    <html lang="en" suppressHydrationWarning>
      <head>
        <link rel="manifest" href="/manifest.webmanifest" suppressHydrationWarning />
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
