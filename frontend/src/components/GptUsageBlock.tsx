'use client'

import React from 'react'

interface Props {
  count: number
}

const GptUsageBlock: React.FC<Props> = ({ count }) => {
  return (
    <div className="gpt-usage">
      GPT Usages Left: <span>{count}</span>
    </div>
  )
}

export default GptUsageBlock
