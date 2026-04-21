export interface ArtistImage {
  url: string
}

export interface ArtistFollowers {
  total: number
}

export interface ArtistExternalUrls {
  spotify: string
}

export interface Artist {
  id: string
  name: string
  images?: ArtistImage[]
  genres?: string[]
  externalUrls: ArtistExternalUrls
  followers: ArtistFollowers
  popularity: number
}