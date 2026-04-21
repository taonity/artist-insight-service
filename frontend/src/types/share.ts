import type { Artist } from '@/types/artist'

export interface SharedArtist {
  artistObject: Artist
  enrichedGenres: string[]
}

export interface ShareOwner {
  displayName: string
  avatarUrl: string | null
}

export interface SharedArtistsData {
  owner: ShareOwner
  artists: SharedArtist[]
}