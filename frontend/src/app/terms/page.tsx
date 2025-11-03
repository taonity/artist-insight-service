import type { Metadata } from 'next'

export const metadata: Metadata = {
  title: 'Terms of Use | Artist Insight',
  description: 'Understand the rules for using the Artist Insight service.',
}

const TermsOfUse = () => {
  return (
    <div className="legal-page">
      <h1>Terms of Use</h1>
      <p className="legal-effective-date">Effective date: {new Date('2024-01-01').toLocaleDateString()}</p>

      <section>
        <h2>Acceptance of terms</h2>
        <p>
          By accessing or using Artist Insight you agree to these Terms of Use and our Privacy Policy. If you do not
          agree, please do not use the service.
        </p>
      </section>

      <section>
        <h2>Eligibility</h2>
        <p>
          Artist Insight is offered for personal, non-commercial use. You must have a valid Spotify account and be able
          to lawfully connect third-party applications to it. You are responsible for maintaining the confidentiality of
          your Spotify credentials.
        </p>
      </section>

      <section>
        <h2>Use of the service</h2>
        <ul>
          <li>Only request data for your own Spotify account and respect Spotify&apos;s developer terms.</li>
          <li>Do not attempt to reverse engineer, disrupt, or overload the service or Spotify&apos;s APIs.</li>
          <li>Avoid submitting unlawful, infringing, or abusive content when contributing enrichments.</li>
        </ul>
      </section>

      <section>
        <h2>Community contributions</h2>
        <p>
          When you add enrichments or other metadata you grant Artist Insight a non-exclusive license to store,
          distribute, and display that information to other users for the purpose of improving artist insights.
        </p>
      </section>

      <section>
        <h2>Open-source status</h2>
        <p>
          Artist Insight is an open-source project provided &quot;as is&quot; without warranties of any kind. Contributions are
          welcome under the project&apos;s repository license.
        </p>
      </section>

      <section>
        <h2>Termination</h2>
        <p>
          We may suspend or terminate access if you misuse the service, violate these terms, or breach Spotify&apos;s
          policies. You may disconnect at any time via your Spotify account settings or by deleting your Artist Insight
          profile.
        </p>
      </section>

      <section>
        <h2>Limitation of liability</h2>
        <p>
          Artist Insight is provided without warranties or guarantees. To the fullest extent permitted by law, the
          maintainers are not liable for indirect, incidental, or consequential damages arising from your use of the
          service.
        </p>
      </section>

      <section>
        <h2>Changes to these terms</h2>
        <p>
          We may update these terms to reflect new features or regulatory requirements. Continued use of Artist Insight
          after updates become effective constitutes acceptance of the revised terms.
        </p>
      </section>

      <section>
        <h2>Contact</h2>
        <p>
          Questions about these terms can be sent to{' '}
          <a href="mailto:artiom.diulgher@gmail.com">artiom.diulgher@gmail.com</a>.
        </p>
      </section>
    </div>
  )
}

export default TermsOfUse
