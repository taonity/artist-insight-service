import Link from 'next/link'

const Footer = () => {
  const currentYear = new Date().getFullYear()

  return (
    <footer className="site-footer">
      <div className="footer-inner">
        <div className="footer-branding">
          <h2>Artist Insight</h2>
          <p className="footer-description">
            A free, community-driven tool for exploring the artists you love. Artist Insight is open
            source and built by developers who care about music fans.
          </p>
          <p className="footer-mobile-description">Independent, open-source music discovery.</p>
        </div>
        <div className="footer-links">
          <h3>Project</h3>
          <ul>
            <li>
              <a
                href="https://github.com/taonity/artist-insight-service"
                target="_blank"
                rel="noopener noreferrer"
              >
                Source code on GitHub
              </a>
            </li>
            <li>
              <Link href="/donate">Support the project</Link>
            </li>
          </ul>
        </div>
        <div className="footer-links footer-contact">
          <h3>Stay in touch</h3>
          <ul>
            <li>
              <a href="mailto:artiom.diulgher@gmail.com" title="artiom.diulgher@gmail.com">
                <span className="footer-email-address">artiom.diulgher@gmail.com</span>
                <span className="footer-email-label">Email the developer</span>
              </a>
            </li>
            <li className="footer-contact-note">
              <span>No ads. No trackers. Just music insights.</span>
            </li>
          </ul>
        </div>
        <div className="footer-links footer-legal">
          <h3>Legal</h3>
          <ul>
            <li>
              <Link href="/privacy">Privacy Policy</Link>
            </li>
            <li>
              <Link href="/terms">Terms of Use</Link>
            </li>
          </ul>
        </div>
      </div>
      <div className="footer-meta">
        <p>
          &copy; {currentYear} Artist Insight. Built independently using the Spotify API. Artist
          Insight is not affiliated with Spotify.
        </p>
        {process.env.BUILD_TIME && (
          <p
            className="footer-build"
            style={{ fontSize: '0.85em', opacity: 0.7, marginTop: '0.5rem' }}
          >
            Build: {new Date(process.env.BUILD_TIME).toLocaleString()} UTC
          </p>
        )}
      </div>
    </footer>
  )
}

export default Footer
