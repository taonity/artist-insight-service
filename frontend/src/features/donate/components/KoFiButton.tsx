'use client'

import '@/styles/kofi-button.scss'

interface Props {
  username: string
  label: string
  title?: string
  preset?: string
  backgroundColor?: ColorName
  animation?: boolean
}

const knownPresets = ['', 'default', 'thin', 'skinny', 'circle', 'no_background']

const colors = {
  kofiWhite: '#FFF',
  kofiBlack: '#000',
  kofiRed: '#FF5E5B',
  kofiBlue: '#13C3FF',
  kofiYellow: '#FBAA19',
  kofiGrey: '#434B57',
  no_background: 'transparent',
} as const

type ColorName = keyof typeof colors

function getKofiColor(color: ColorName): string {
  return colors[color]
}

const KOFI_URL = process.env.KOFI_URL

const KoFiButton: React.FC<Props> = ({
  username,
  label = 'Support Me on Ko-fi',
  title = '',
  preset,
  backgroundColor = 'no_background',
  animation = true,
}) => {
  const normalizedPreset = preset && knownPresets.includes(preset) ? preset : ''
  const profileUrl = `${KOFI_URL}/${username}`

  return (
    <div className="KofiContainer" style={{ backgroundColor: getKofiColor(backgroundColor) }}>
      <a
        className={`KofiButton ${normalizedPreset === 'default' ? '' : normalizedPreset}`}
        href={profileUrl}
        target="_blank"
        rel="noreferrer noopener external"
        title={title}
      >
        <figure className="KofiImageContainer">
          <span className={`KofiImage animation_${animation}`} aria-hidden="true" />
        </figure>
        {label && <span className="KofiText">{label}</span>}
      </a>
    </div>
  )
}

export default KoFiButton