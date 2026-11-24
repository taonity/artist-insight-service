import { chromium, expect } from '@playwright/test'
import { mkdir, writeFile } from 'node:fs/promises'

export default async function setup() {
  const site = 'http://127.0.0.1:3008'
  const browser = await chromium.launch()
  try {
    const context = await browser.newContext({ baseURL: site })
    const config = await context.request.get('/api/config')
    expect(config.ok()).toBe(true)
    const runtimeConfig = await config.json()
    expect(runtimeConfig).toMatchObject({ profile: 'demo' })

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

    const sharedContext = await browser.newContext({ baseURL: site })
    try {
      const sharedPage = await sharedContext.newPage()
      await sharedPage.goto('http://127.0.0.1:8088/oauth2/authorization/stub-user')
      await expect(sharedPage).toHaveURL(`${site}/`)
      await expect(sharedPage.locator('header').getByText('Rumi Cade', { exact: true })).toBeVisible()
      await expect(sharedPage.getByRole('button', { name: 'Share link', exact: true })).toBeEnabled()
      const csrf = (await sharedContext.cookies(site)).find(cookie => cookie.name === runtimeConfig.csrfCookieName)
      expect(csrf).toBeDefined()
      const response = await sharedContext.request.post('/api/share', {
        headers: { 'X-XSRF-TOKEN': decodeURIComponent(csrf!.value) },
      })
      expect(response.ok()).toBe(true)
      const { shareCode } = await response.json()
      expect(shareCode).toMatch(/^[a-zA-Z0-9]{8}$/)
      const sharedResponse = await context.request.get(`/api/share/${shareCode}`)
      expect(sharedResponse.ok()).toBe(true)
      const shared = await sharedResponse.json()
      expect(shared.owner.displayName).toBe('Rumi Cade')
      expect(shared.artists).toHaveLength(36)
      await writeFile('test-results/demo-share.json', JSON.stringify({
        path: `/share/${shareCode}`,
        ownerName: shared.owner.displayName,
        artistCount: shared.artists.length,
      }))
    } finally {
      await sharedContext.close()
    }
  } finally {
    await browser.close()
  }
}
