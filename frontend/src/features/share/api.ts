import type { SharedArtistsData } from '@/types/share'

export async function fetchSharedArtists(shareCode: string) {
  const response = await fetch(`/api/share/${shareCode}`)

  if (response.status === 404) {
    return { status: 'not-found' as const }
  }

  if (response.status === 410) {
    return { status: 'expired' as const }
  }

  if (!response.ok) {
    throw new Error(`Failed to load shared artists: ${response.status}`)
  }

  return {
    status: 'ok' as const,
    data: (await response.json()) as SharedArtistsData,
  }
}