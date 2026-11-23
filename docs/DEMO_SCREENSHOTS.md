# Demo screenshots

Run the following from `frontend/` with Java 17+, Maven, and Node.js 24+ installed. Keep ports 3008, 8088, 8100, and 8101 free.

```bash
npm ci
npx playwright install chromium
npm run demo:screenshots
```

Open `screenshots/index.html` at the repository root. The gallery covers Followings, the account page, and the owner console's Users, access requests, runtime settings, and audit log. Desktop captures crop to the app content width. Every capture signs in as the fictional owner, Mira Vell. The capture stack uses an in-memory H2 database and local integration stubs.

Run `npm run demo:capture` to capture again from the existing build. To capture an already running demo on ports 3008 and 8088, set `ARTIST_INSIGHT_DEMO_RUNNING=1` before running that command.

Run **Demo Screenshots** manually in GitHub Actions or let **Release** call it after production deployment succeeds. The workflow uploads the gallery and browser report. Successful captures on `main` publish `screenshots.zip` in a `portfolio-screenshots-<commit-SHA>` prerelease, which the portfolio imports separately.
