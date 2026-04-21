'use client'

import React, { useEffect, useState } from 'react'
import type { Advisory } from '@/types/advisory'

interface Props {
  advisories: Advisory[]
}

const AdvisoryCards: React.FC<Props> = ({ advisories }) => {
  const [visibleAdvisories, setVisibleAdvisories] = useState<Advisory[]>(advisories)

  useEffect(() => {
    setVisibleAdvisories(advisories)
  }, [advisories])

  const dismiss = (code: Advisory['code']) => {
    setVisibleAdvisories((prev) => prev.filter((advisory) => advisory.code !== code))
  }

  if (visibleAdvisories.length === 0) {
    return null
  }

  return (
    <ul className="advisories">
      {visibleAdvisories.map((advisory) => (
        <li
          key={advisory.code}
          className={`advisory-card ${advisory.severity.toLowerCase()}`}
        >
          <div className="advisory-content">
            <strong>{advisory.title}</strong>
            <p>{advisory.detail}</p>
          </div>
          <button className="advisory-close" onClick={() => dismiss(advisory.code)}>
            ×
          </button>
        </li>
      ))}
    </ul>
  )
}

export default AdvisoryCards