import React from 'react'

interface Props {
  items?: number
}

const Loading: React.FC<Props> = ({ items = 10 }) => {
  return (
    <ul className="artist-list">
      {Array.from({ length: items }).map((_, idx) => (
        <li key={idx} className="artist-item">
          <div className="artist-image skeleton-circle" />
          <div className="artist-details">
            <div className="skeleton-line short" />
            <div className="skeleton-line" />
          </div>
        </li>
      ))}
    </ul>
  )
}

export default Loading
