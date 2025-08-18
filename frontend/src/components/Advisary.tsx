import React from 'react'

type Severity = "INFO" | "WARNING" | "ERROR";


export interface Advisory {
  code: "TOO_MANY_FOLLOWERS" | "GPT_ENRICHMENT_AVAILABLE"
  title: string
  detail: string
  severity: Severity
}

interface Props {
  advisories: Advisory[]
}

const AdvisoryCards: React.FC<Props> = ({ advisories }) => {
  return (
    <ul className="artist-list">
      {advisories.map((advisory) => {
        return (
          <li key={advisory.code} className="artist-item">
            <span>{advisory.title}</span>
            <span>{advisory.detail}</span>
          </li>
        );
      })}
    </ul>
  )
}

export default AdvisoryCards