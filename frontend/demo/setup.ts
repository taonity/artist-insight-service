import { chromium, expect } from '@playwright/test'
import { mkdir } from 'node:fs/promises'

export default async function setup() {
  const site = 'http://127.0.0.1:3008'
  const browser = await chromium.launch()
  try {
    const context = await browser.newContext({ baseURL: site })
    const config = await context.request.get('/api/config')
    expect(config.ok()).toBe(true)
    expect(await config.json()).toMatchObject({ profile: 'demo' })

    const page = await context.newPage()
    await page.goto('http://127.0.0.1:8088/oauth2/authorization/stub-owner')
    await expect(page).toHaveURL(`${site}/`)
    await expect(page.getByText('Mira Vell', { exact: true })).toBeVisible()
    await expect(
      page.locator('header img[src="/demo/avatars/portrait-mira-vell.webp"]'),
    ).toBeVisible()
    await expect(page.getByRole('button', { name: 'Admin', exact: true })).toBeVisible()
    const access = await context.request.get('/api/console/access/me')
    expect(access.ok()).toBe(true)
    expect(await access.json()).toMatchObject({ role: 'OWNER', isOwner: true })

    await page.getByRole('button', { name: 'Got it', exact: true }).click()
    await mkdir('test-results', { recursive: true })
    await context.storageState({ path: 'test-results/demo-owner.json' })
  } finally {
    await browser.close()
  }
}
