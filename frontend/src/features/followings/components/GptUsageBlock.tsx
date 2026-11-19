'use client'

import React from 'react'

interface Props {
  count: number
}

const GptUsageBlock: React.FC<Props> = ({ count }) => {
  return (
    <span className="gpt-usage">
      GPT Usages Left: <span className="gpt-usage-count">{count}</span>
    </span>
  )
}

export default GptUsageBlock
