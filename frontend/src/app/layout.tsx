import '../styles/globals.css'
import type { Metadata } from 'next'

export const metadata: Metadata = {
  title: 'Artist Insight',
}

const currentYear = new Date().getFullYear()

export default function RootLayout({
  children,
}: {
  children: React.ReactNode
}) {
  return (
    <html lang="en">
      <body>
        <div className="app-shell">
          <main className="app-main">{children}</main>
          <footer className="site-footer">
            <div className="footer-content">
              <div className="footer-brand">
                <h2>Artist Insight</h2>
                <p>
                  A free, open-source companion for exploring your Spotify
                  insights and celebrating the artists you love.
                </p>
              </div>
              <div className="footer-columns">
                <div className="footer-column">
                  <span className="footer-heading">Project</span>
                  <a
                    href="https://github.com/artist-insight/artist-insight-service"
                    target="_blank"
                    rel="noreferrer noopener"
                  >
                    View on GitHub
                  </a>
                  <a href="/donate">Support the project</a>
                </div>
                <div className="footer-column">
                  <span className="footer-heading">Community</span>
                  <a
                    href="https://github.com/artist-insight/artist-insight-service/issues"
                    target="_blank"
                    rel="noreferrer noopener"
                  >
                    Report an issue
                  </a>
                  <a
                    href="https://github.com/artist-insight/artist-insight-service/discussions"
                    target="_blank"
                    rel="noreferrer noopener"
                  >
                    Join the discussion
                  </a>
                </div>
                <div className="footer-column">
                  <span className="footer-heading">Stay Informed</span>
                  <a
                    href="https://status.spotify.com/"
                    target="_blank"
                    rel="noreferrer noopener"
                  >
                    Spotify status
                  </a>
                  <a
                    href="https://newsroom.spotify.com/"
                    target="_blank"
                    rel="noreferrer noopener"
                  >
                    Spotify news
                  </a>
                </div>
              </div>
              <div className="footer-meta">
                <span>
                  &copy; {currentYear} Artist Insight. Built with love for the
                  community.
                </span>
                <span>
                  Licensed under MIT · Free for everyone to use and remix.
                </span>
              </div>
            </div>
          </footer>
        </div>
      </body>
    </html>
  )
}
