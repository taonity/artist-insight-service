const Footer = () => {
  const currentYear = new Date().getFullYear();
  return (
    <footer className="site-footer">
      <div className="footer-inner">
        <div className="footer-branding">
          <h2>Artist Insight</h2>
          <p>
            A free, community-driven tool for exploring the artists you love.
            Artist Insight is open source and built by developers who care about
            music fans.
          </p>
        </div>
        <div className="footer-links">
          <h3>Project</h3>
          <ul>
            <li>
              <a
                href="https://github.com/artist-insight"
                target="_blank"
                rel="noopener noreferrer"
              >
                Source code on GitHub
              </a>
            </li>
            <li>
              <a href="/donate">Support the project</a>
            </li>
          </ul>
        </div>
        <div className="footer-links">
          <h3>Stay in touch</h3>
          <ul>
            <li>
              <a href="mailto:artiom.diulgher@gmail.com">artiom.diulgher@gmail.com</a>
            </li>
            <li>
              <span>No ads. No trackers. Just music insights.</span>
            </li>
          </ul>
        </div>
      </div>
      <div className="footer-meta">
        <p>
          &copy; {currentYear} Artist Insight. Built independently using the
          Spotify API. Artist Insight is not affiliated with Spotify.
        </p>
      </div>
    </footer>
  );
};

export default Footer;
