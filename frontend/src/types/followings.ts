import type { Artist } from '@/types/artist'

export interface EnrichableArtistObject {
  artistObject: Artist
  genreEnriched: boolean
}