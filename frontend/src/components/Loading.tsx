'use client'

import React from 'react'

interface Props {
  items?: number
}

const Loading: React.FC<Props> = ({ items = 10 }) => {
  return (
    <>
      <div className="gpt-usage skeleton-gpt-usage">
        <div className="skeleton-line" style={{ width: 140, height: 18.9 }} />
      </div>
      <div className="actions">
        <div className="skeleton-pill" style={{ width: 140 }} />
        <div className="skeleton-pill" style={{ width: 120 }} />
      </div>
      <div className="loading-table">
        <div className="loading-table-header">
          <div className="loading-table-cell" style={{ width: 90 }}>
            <div className="skeleton-line" style={{ width: 50 }} />
          </div>
          <div className="loading-table-cell" style={{ flex: 1 }}>
            <div className="skeleton-line" style={{ width: 40 }} />
          </div>
          <div className="loading-table-cell" style={{ flex: 2 }}>
            <div className="skeleton-line" style={{ width: 50 }} />
          </div>
          <div className="loading-table-cell" style={{ width: 120 }}>
            <div className="skeleton-line" style={{ width: 70 }} />
          </div>
          <div className="loading-table-cell" style={{ width: 120 }}>
            <div className="skeleton-line" style={{ width: 70 }} />
          </div>
        </div>
        {Array.from({ length: items }).map((_, idx) => (
          <div key={idx} className="loading-table-row">
            <div className="loading-table-cell" style={{ width: 90 }}>
              <div className="skeleton-circle small" />
            </div>
            <div className="loading-table-cell" style={{ flex: 1 }}>
              <div className="skeleton-line short" />
            </div>
            <div className="loading-table-cell" style={{ flex: 2 }}>
              <div className="skeleton-line" />
            </div>
            <div className="loading-table-cell" style={{ width: 120 }}>
              <div className="skeleton-line short" />
            </div>
            <div className="loading-table-cell" style={{ width: 120 }}>
              <div className="skeleton-line short" />
            </div>
          </div>
        ))}
      </div>
    </>
  )
}

export default Loading
