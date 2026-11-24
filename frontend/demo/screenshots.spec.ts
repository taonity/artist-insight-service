import { expect, test } from '@playwright/test'
import { createScreenshotCapture } from '@taonity/demo-screenshots'
import { readFile } from 'node:fs/promises'

const capture = createScreenshotCapture(expect)
const portrait = 'header img[src="/demo/avatars/portrait-mira-vell.webp"]'

const scenes = [
  { name: 'main', path: '/' },
  { name: 'settings', path: '/settings' },
  { name: 'share', path: '' },
] as const

for (const scene of scenes) {
  test(`capture ${scene.name} as owner`, async ({ page, baseURL }, testInfo) => {
    const phone = testInfo.project.name.startsWith('phone')
    const failures: string[] = []
    page.on('pageerror', (error) => failures.push(error.message))
    page.on('response', (response) => {
      const url = new URL(response.url())
      if (url.origin !== baseURL || !url.pathname.startsWith('/api/') || response.ok()) return
      if (url.pathname === '/api/share' && response.status() === 404) return
      failures.push(`${response.status()} ${url.pathname}`)
    })
    const shared: { path: string; ownerName: string; artistCount: number } | null = scene.name === 'share'
      ? JSON.parse(await readFile('test-results/demo-share.json', 'utf8'))
      : null
    await page.goto(shared?.path ?? scene.path)
    const theme = testInfo.project.name.endsWith('-dark') ? 'dark' : 'light'
    await expect(page.locator('html')).toHaveAttribute('data-theme', theme)

    await expect(page.locator('header').getByText('Mira Vell', { exact: true })).toBeVisible()
    await expect(page.locator(portrait)).toBeVisible()
    await expect(page.getByRole('button', { name: 'Admin', exact: true })).toBeVisible()
    if (shared) {
      await expect(page.getByRole('heading', { name: `${shared.ownerName}'s Followed Artists`, exact: true })).toBeVisible()
      await expect(page.locator('.share-owner-info')).toContainText(`${shared.artistCount} artists`)
      await expect(page.locator('.share-owner-avatar img')).toHaveAttribute('src', '/demo/avatars/portrait-rumi-cade.webp')
      await expect(page.getByRole('button', { name: 'Enrich followings', exact: true })).toHaveCount(0)
      if (phone) {
        await expect(page.getByRole('navigation', { name: 'Pagination above artists' })).toContainText('1–10 of 36')
      } else {
        await expect(page.locator('.ag-row')).toHaveCount(shared.artistCount)
      }
    } else {
      if (scene.name === 'main') {
        await expect(page.getByText('GPT enrichment available', { exact: true })).toBeVisible()
        const perPage = phone ? 10 : 20
        await expect(
          page.getByRole('navigation', { name: 'Pagination above artists' }),
        ).toContainText(`1–${perPage} of 36`)
      } else {
        await expect(page.getByRole('heading', { name: 'Settings', exact: true })).toBeVisible()
        await expect(page.getByRole('combobox', { name: 'Theme', exact: true })).toHaveValue('system')
        await expect(page.getByText('stub-owner', { exact: true })).toBeVisible()
      }
    }

    await expect(
      page.locator('.content-skeleton-active:visible, .admin-slot-loading:visible'),
    ).toHaveCount(0)
    await expect(page.locator('.error-notification:visible, .admin-error:visible')).toHaveCount(0)
    expect(await page.evaluate(() => document.documentElement.scrollWidth)).toBeLessThanOrEqual(
      page.viewportSize()!.width,
    )
    await page.locator('img[loading="lazy"]').evaluateAll((images) => {
      for (const image of images) (image as HTMLImageElement).loading = 'eager'
    })
    await page.addStyleTag({
      content: `
        html { scrollbar-gutter: auto !important; scrollbar-width: none !important; }
        html::-webkit-scrollbar { display: none !important; }
        body { background-attachment: scroll; }
        aside[aria-label="Local developer tools"] { display: none !important; }
        @media (min-width: 1100px) {
          .app-main .header {
            box-sizing: border-box;
            width: 100%;
            max-width: var(--page-content-max-width);
            margin-inline: auto;
            padding-inline: var(--page-content-gutter);
          }
        }
      `,
    })
    await expect(page.locator('aside[aria-label="Local developer tools"]')).toBeHidden()
    const layout = await page.evaluate(() => ({
      gutter: innerWidth - document.documentElement.clientWidth,
      backdropBottom: document.body.getBoundingClientRect().bottom + scrollY,
      backdrop: getComputedStyle(document.body).backgroundImage,
      page: document.documentElement.scrollHeight,
    }))
    expect(layout.gutter).toBe(0)
    expect(layout.backdropBottom).toBeGreaterThanOrEqual(layout.page - 1)
    expect(layout.backdrop).toContain('linear-gradient(')

    if (scene.name === 'main') {
      const enriched = page.locator('.genre.enriched').first()
      await expect(enriched).toBeVisible()
      await expect(enriched).toHaveText(/✦$/)
      await expect(enriched).toHaveCSS('background-color', 'rgba(0, 0, 0, 0)')
      if (phone) {
        await expect(page.getByText('Genres (AI enriched)', { exact: true })).toHaveCount(0)
        const hasWrappedGenres = await page
          .locator('.artist-record-genre-text')
          .evaluateAll((lists) =>
            lists.some(
              (list) =>
                list.getBoundingClientRect().height >
                parseFloat(getComputedStyle(list).lineHeight) + 1,
            ),
          )
        expect(hasWrappedGenres).toBe(true)
      } else {
        const rowHeights = await page
          .locator('.followings-table tbody tr')
          .evaluateAll((rows) => rows.map((row) => Math.round(row.getBoundingClientRect().height)))
        const genreHeights = await page
          .locator('.followings-table td[data-column="genres"] .followings-cell-text')
          .evaluateAll((cells) =>
            cells.map((cell) => Math.round(cell.getBoundingClientRect().height)),
          )
        expect(new Set(rowHeights)).toEqual(new Set([60]))
        expect(Math.max(...genreHeights)).toBe(40)
      }
    }

    const content = scene.name === 'main' ? '.main-content' : '.header'
    const clip = phone
      ? undefined
      : await page.locator(content).evaluate((element) => {
          const bounds = element.getBoundingClientRect()
          return {
            x: Math.round(bounds.left + scrollX),
            y: 0,
            width: Math.round(bounds.width),
            height: Math.max(
              document.documentElement.scrollHeight,
              document.body.scrollHeight,
              innerHeight,
            ),
          }
        })
    await capture(page, testInfo, scene.name, { clip })
    expect(failures).toEqual([])
  })
}
