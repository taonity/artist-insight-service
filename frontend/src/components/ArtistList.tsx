import React from 'react'
import Image from 'next/image'
import Genres from '../components/Genres'

interface Artist {
  id: string
  name: string
  images?: { url: string }[]
  genres?: string[]
}

export interface EnrichableArtistObject {
  artistObject: Artist
  genreEnriched: boolean
}

interface Props {
  enrichableArtistObjects: EnrichableArtistObject[]
}

const ArtistList: React.FC<Props> = ({ enrichableArtistObjects }) => {
  return (
    <ul className="artist-list">
      {enrichableArtistObjects.map((enrichableArtistObject) => {
        const artist = enrichableArtistObject.artistObject;
        return (
          <li key={artist.id} className="artist-item">
            {artist.images && artist.images.length > 0 && (
              <Image
                src={artist.images[0].url}
                alt={artist.name}
                width={48}
                height={48}
                className="artist-image"
              />
            )}
            <div className="artist-details">
              <strong>{artist.name}</strong>
              {artist.genres && artist.genres.length > 0 && (
                <Genres genres={artist.genres} enriched={enrichableArtistObject.genreEnriched} />
              )}
            </div>
          </li>
        );
      })}
    </ul>
  )
}

export default ArtistList
