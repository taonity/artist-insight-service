import type { NextRequest } from 'next/server'

export const BACKEND_URL = process.env.BACKEND_URL || ''

export async function fetchFromBackend(
  req: NextRequest,
  path: string,
  init: RequestInit = {}
) {
  const headers = new Headers(init.headers)
  const cookie = req.headers.get('cookie')
  if (cookie) {
    headers.set('cookie', cookie)
  }
  const controller = new AbortController()
  const timeout = setTimeout(() => controller.abort(), 6000)

  try {
    return await fetch(`${BACKEND_URL}${path}`, {
      ...init,
      headers,
      signal: controller.signal,
    })
  } finally {
    clearTimeout(timeout)
  }
}
