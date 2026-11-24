# Demo screenshots

Run the following from `frontend/` with Java 17+, Maven, and Node.js 24+ installed. Keep ports 3008, 8088, 8100, and 8101 free.

```bash
npm ci
npx playwright install chromium
npm run demo:screenshots
```

Open `screenshots/index.html` at the repository root. The gallery contains exactly three pages: Main, Settings, and Share. Each page has desktop and phone captures in light and dark mode (12 screenshots total). Every capture signs in as the fictional owner, Mira Vell. Share shows Rumi Cade's followed artists while Mira remains signed in; setup creates that share link in a separate browser context using Rumi's demo login. No paid enrichment is triggered. Desktop captures crop to the app content width. The capture stack uses an in-memory H2 database and local integration stubs.

The launcher uses Maven's recorded artifact version, rather than file timestamps, to select the built backend JAR. It preserves the JAR's root metadata when extracting the application for WireMock, including the Spring Boot build information.

Run `npm run demo:capture` to capture again from the existing build. To capture an already running demo on ports 3008 and 8088, set `ARTIST_INSIGHT_DEMO_RUNNING=1` before running that command.

Run **Demo Screenshots** manually in GitHub Actions or let **Release** call it after production deployment succeeds. The workflow uploads the gallery and browser report. Successful captures on `main` publish `screenshots.zip` in a `portfolio-screenshots-<commit-SHA>` prerelease, which the portfolio imports separately.

Maven CI jobs load `.mvn/ci-settings.xml` through `MAVEN_ARGS` before resolving build extensions. This routes Central's default ID and the aliases declared by this project and its dependencies (`central`, `mavenCentral`, `maven-central`, and `maven_central`) through Google's [Maven Central mirror](https://docs.cloud.google.com/docs/buildpacks/java#google_cloud_hosted_maven_central_mirror), avoiding the direct Central endpoint's HTTP 429 responses. Dependency cache keys include the Maven extension and mirror settings. Local Maven settings remain unchanged.
