import type { Metadata } from 'next'

export const metadata: Metadata = {
  title: 'Privacy Policy | Artist Insight',
  description: 'Understand how Artist Insight collects, uses, and safeguards your Spotify data.',
}

const PrivacyPolicy = () => {
  const effectiveDate = new Date('2025-11-03')

  return (
    <div className="legal-page">
      <h1>Privacy Policy</h1>
      <p className="legal-effective-date">Effective date: {effectiveDate.toLocaleDateString()}</p>

      <section>
        <h2>Who we are</h2>
        <p>
          Artist Insight is an open-source, non-commercial tool that enriches the list of artists you follow on Spotify
          with community-sourced genres and insights. You can contact the maintainer at{' '}
          <a href="mailto:artiom.diulgher@gmail.com">artiom.diulgher@gmail.com</a> with privacy questions or requests.
        </p>
      </section>

      <section>
        <h2>Information we collect</h2>
        <p>When you sign in with Spotify we request the following information from the Spotify Web API:</p>
        <ul>
          <li>Spotify account identifier, display name, and avatar.</li>
          <li>Your list of followed artists and the genres Spotify associates with them.</li>
          <li>OAuth access and refresh tokens that allow the app to act on your behalf.</li>
        </ul>
        <p>
          We also record community-generated enrichments (such as additional genres) that you choose to add to artists.
          These enrichments are shared back with the community but are not tied publicly to your Spotify account.
        </p>
      </section>

      <section>
        <h2>How we use your information</h2>
        <ul>
          <li>To display your Spotify profile, followed artists, and enriched data inside the application.</li>
          <li>To enrich artist information by calling OpenAI services when you request additional genres.</li>
          <li>To monitor GPT usage so that we can allocate fair access to enrichment requests.</li>
        </ul>
        <p>
          We do not sell or share your personal data with advertisers. Aggregated, anonymised insights about artists may
          be visible to other community members.
        </p>
      </section>

      <section>
        <h2>Storage and retention</h2>
        <p>
          Spotify profile identifiers, display names, and masked tokens are stored in a secure database. When you delete
          your Artist Insight account we remove your user record, associated OAuth tokens, and your personal
          relationships to artists. Community enrichments you contributed remain available so that other users continue
          to benefit from them, but they are no longer linked to your Spotify identity.
        </p>
      </section>

      <section>
        <h2>Data sharing and processors</h2>
        <p>
          We use Spotify to authenticate you and fetch your library data, and OpenAI to help generate genre enrichment
          suggestions when you explicitly request them. Both providers process data only to perform those functions.
        </p>
      </section>

      <section>
        <h2>Cookies and similar technologies</h2>
        <p>
          Artist Insight sets essential cookies to keep you signed in, protect your session against cross-site request
          forgery, and remember that you acknowledged this notice. We do not use analytics, advertising, or tracking
          cookies. You can clear your browser cookies to remove non-essential data at any time.
        </p>
      </section>

      <section>
        <h2>Your rights and choices</h2>
        <ul>
          <li>You can disconnect Artist Insight from your Spotify account at any time in Spotify&apos;s account dashboard.</li>
          <li>Use the in-app deletion controls to remove your Artist Insight account and associated personal data.</li>
          <li>
            Contact us at <a href="mailto:artiom.diulgher@gmail.com">artiom.diulgher@gmail.com</a> to request data
            access, correction, or full deletion.
          </li>
        </ul>
      </section>

      <section>
        <h2>Policy updates</h2>
        <p>
          If we make material changes to this policy we will update the effective date and notify active users via email
          or an in-app message. Continued use of Artist Insight after an update means you accept the revised policy.
        </p>
      </section>
    </div>
  )
}

export default PrivacyPolicy
