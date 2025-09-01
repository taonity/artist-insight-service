'use client'

import React from 'react'

interface Props {
  genres: String[]
  enriched: boolean
}

const Genres: React.FC<Props> = ({ genres, enriched }) => {
  return (
    <div className={`genres`}>
      <span className={`genre ${enriched ? 'enriched' : ''}`}>
        {genres.join(', ') + (enriched ? ' ✨' : '')}
      </span>
    </div>
  )
}

export default Genres
