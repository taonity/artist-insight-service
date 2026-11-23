import { defineScreenshotConfig } from '@taonity/demo-screenshots'

export default defineScreenshotConfig({
  configDir: __dirname,
  title: 'Artist Insight owner demo',
  screenshotDir: '../screenshots',
  globalSetup: './demo/setup.ts',
  deviceScaleFactor: 2,
  use: {
    baseURL: 'http://127.0.0.1:3008',
    storageState: './test-results/demo-owner.json',
  },
  webServer:
    process.env.ARTIST_INSIGHT_DEMO_RUNNING === '1'
      ? undefined
      : [
          {
            command: 'node scripts/demo.mjs backend',
            url: 'http://127.0.0.1:8088/actuator/health/readiness',
            timeout: 120_000,
            reuseExistingServer: false,
            gracefulShutdown: { signal: 'SIGTERM', timeout: 10_000 },
          },
          {
            command: 'node scripts/demo.mjs frontend',
            url: 'http://127.0.0.1:3008/api/config',
            timeout: 60_000,
            reuseExistingServer: false,
            gracefulShutdown: { signal: 'SIGTERM', timeout: 10_000 },
          },
        ],
})
