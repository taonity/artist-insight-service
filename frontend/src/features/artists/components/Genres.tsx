'use client'

import React from 'react'

interface Props {
  genres: string[]
  enriched: boolean
}

const Genres: React.FC<Props> = ({ genres, enriched }) => {
  return (
    <span className={enriched ? 'genre enriched' : 'genre'}>
      {enriched && <span className="app-sr-only">Enriched genres </span>}
      {genres.join(', ')}
      {enriched && (
        <span className="genre-enriched-marker" aria-hidden="true">
          {'\u00a0✦'}
        </span>
      )}
    </span>
  )
}

export default Genres
