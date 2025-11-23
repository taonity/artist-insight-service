let configCache: { csrfCookieName: string; publicBackendUrl: string } | null = null

export async function getRuntimeConfig() {
  if (configCache) {
    return configCache
  }
  try {
    const response = await fetch('/api/config')
    configCache = await response.json()
      console.log(configCache)
    return configCache!
  } catch (error) {
    console.error('Failed to load runtime config:', error)
    return {
      csrfCookieName: '',
      publicBackendUrl: '',
    }
  }
}

