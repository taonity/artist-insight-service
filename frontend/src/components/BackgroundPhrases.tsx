'use client'

import { useEffect, useState, useRef } from 'react'

interface Phrase {
  id: number
  text: string
  x: number
  y: number
  opacity: number
  width: number
  description: string
}

interface BoundingBox {
  x: number
  y: number
  width: number
  height: number
}

interface Genre {
  name: string
  description: string
}

interface BackgroundPhrasesProps {
  onGenreHover: (description: string | null) => void
}

export default function BackgroundPhrases({ onGenreHover }: BackgroundPhrasesProps) {
  const [phrases, setPhrases] = useState<Phrase[]>([])
  const containerRef = useRef<HTMLDivElement>(null)

  const genres: Genre[] = [
    { name: 'Rock', description: 'Electric guitars, drums, and powerful vocals define this energetic genre born in the 1950s.' },
    { name: 'Pop', description: 'Catchy melodies and mainstream appeal characterize this popular music style loved worldwide.' },
    { name: 'Hip Hop', description: 'Rhythmic beats, rap vocals, and urban culture define this influential modern music movement.' },
    { name: 'Jazz', description: 'Improvisation, swing rhythms, and complex harmonies create this sophisticated American art form.' },
    { name: 'Electronic', description: 'Synthesizers and digital production create futuristic soundscapes in this technology-driven genre.' },
    { name: 'Classical', description: 'Orchestral compositions and timeless works from the great masters of Western music tradition.' },
    { name: 'R&B', description: 'Soulful vocals and smooth rhythms blend in this rhythm and blues influenced genre.' },
    { name: 'Country', description: 'Storytelling through acoustic guitars and heartfelt lyrics about life and love.' },
    { name: 'Metal', description: 'Heavy distorted guitars, aggressive vocals, and intense energy define this powerful genre.' },
    { name: 'Indie', description: 'Independent artists creating unique sounds outside mainstream commercial music industry.' },
    { name: 'Folk', description: 'Traditional acoustic music telling stories of culture, history, and everyday life.' },
    { name: 'Blues', description: 'Emotional guitar-based music expressing life struggles with distinctive twelve-bar progressions.' },
    { name: 'Reggae', description: 'Offbeat rhythms and bass-heavy grooves from Jamaica spreading messages of peace.' },
    { name: 'Punk', description: 'Fast-paced, rebellious music with raw energy and anti-establishment attitude.' },
    { name: 'Soul', description: 'Passionate vocals and emotional depth rooted in African American gospel traditions.' },
    { name: 'Funk', description: 'Syncopated bass lines and danceable grooves create infectious rhythmic patterns.' },
    { name: 'Disco', description: 'Dance-oriented beats and glamorous style dominated nightclubs in the 1970s era.' },
    { name: 'House', description: 'Four-on-the-floor beats and repetitive rhythms drive this electronic dance music.' },
    { name: 'Techno', description: 'Futuristic electronic sounds and hypnotic beats born in Detroit underground scene.' },
    { name: 'Dubstep', description: 'Heavy bass drops and wobbles characterize this electronic dance music subgenre.' },
    { name: 'Trap', description: 'Hard-hitting 808 bass, hi-hats, and modern hip hop production define this style.' },
    { name: 'Ambient', description: 'Atmospheric soundscapes and textures create immersive sonic environments for relaxation.' },
    { name: 'Lo-fi', description: 'Intentionally imperfect production creates nostalgic, relaxed beats for studying and chilling.' },
    { name: 'K-Pop', description: 'Korean pop music blending catchy hooks, choreography, and polished production values.' },
    { name: 'Latin', description: 'Passionate rhythms and Spanish lyrics celebrate Latin American musical traditions.' },
    { name: 'Gospel', description: 'Spiritual Christian music featuring powerful vocals and uplifting religious messages.' },
    { name: 'Ska', description: 'Upbeat Jamaican music with offbeat guitar chops and brass sections.' },
    { name: 'Grunge', description: 'Raw, distorted guitars and angst-filled lyrics from Seattle alternative rock scene.' },
    { name: 'Emo', description: 'Emotional punk rock with confessional lyrics exploring feelings and relationships.' },
    { name: 'Shoegaze', description: 'Wall of guitar effects and ethereal vocals create dreamy atmospheric soundscapes.' },
    { name: 'Dream Pop', description: 'Ethereal vocals and lush production create atmospheric, otherworldly musical landscapes.' },
    { name: 'Post-Rock', description: 'Instrumental compositions building dramatic crescendos beyond traditional rock song structures.' },
    { name: 'Industrial', description: 'Harsh mechanical sounds and aggressive electronic beats create dystopian sonic landscapes.' },
    { name: 'Synthwave', description: 'Retro 1980s synthesizer sounds evoking nostalgia for neon-lit futuristic aesthetics.' },
    { name: 'Vaporwave', description: 'Surreal electronic music sampling 80s and 90s pop culture with slowed tempos.' },
    { name: 'Bluegrass', description: 'Acoustic string instruments and vocal harmonies rooted in Appalachian mountain music.' },
    { name: 'Afrobeat', description: 'West African rhythms fused with jazz, funk, and traditional percussion patterns.' },
    { name: 'Bossa Nova', description: 'Brazilian fusion of samba rhythms with cool jazz harmonies and gentle guitar.' },
    { name: 'Flamenco', description: 'Passionate Spanish guitar music accompanied by rhythmic hand claps and dancing.' },
    { name: 'Tango', description: 'Dramatic Argentine dance music featuring bandoneon accordion and passionate rhythms.' },
    { name: 'Swing', description: 'Big band jazz with strong rhythmic feel perfect for dancing and celebration.' },
    { name: 'Bebop', description: 'Fast-tempo jazz emphasizing improvisation and complex chord progressions.' },
    { name: 'Dub', description: 'Jamaican music emphasizing bass and drums with heavy use of reverb effects.' },
    { name: 'Garage', description: 'Raw, energetic rock music recorded with DIY aesthetic and punk attitude.' },
    { name: 'Grime', description: 'British electronic music with aggressive beats and rapid-fire MC vocals.' },
    { name: 'Drill', description: 'Dark, aggressive hip hop subgenre with ominous beats and street narratives.' },
    { name: 'Drum and Bass', description: 'Fast breakbeats and heavy bass characterize this energetic electronic dance genre.' },
    { name: 'Trance', description: 'Hypnotic electronic music with repeating melodic phrases and uplifting energy.' },
    { name: 'Psychedelic', description: 'Mind-bending sounds inspired by altered consciousness and experimental recording techniques.' },
    { name: 'Progressive', description: 'Complex compositions pushing boundaries with unconventional structures and virtuoso performances.' },
  ]

  useEffect(() => {
    const generatePhrases = () => {
      if (!containerRef.current) return
      
      const windowWidth = window.innerWidth
      const windowHeight = window.innerHeight
      
      // Calculate grid based on density (1 phrase per 150x150px)
      const cellSize = 150
      const cols = Math.ceil(windowWidth / cellSize)
      const rows = Math.ceil(windowHeight / cellSize)
      
      // Border padding
      const borderPadding = 20
      
      // Define safe zone for logo and button (center area with 50px padding)
      const safeZonePadding = 50
      const safeZone = {
        x: windowWidth / 2 - 200 - safeZonePadding,
        y: windowHeight / 2 - 150 - safeZonePadding,
        width: 400 + safeZonePadding * 2,
        height: 300 + safeZonePadding * 2,
      }
      
      const occupiedSpaces: BoundingBox[] = []
      
      // Add safe zone to occupied spaces
      occupiedSpaces.push(safeZone)
      
      const newPhrases: Phrase[] = []
      let id = 0
      
      // Estimate character width (approximate)
      const charWidth = 7
      const lineHeight = 20
      
      let genreIndex = 0
      
      for (let row = 0; row < rows; row++) {
        for (let col = 0; col < cols; col++) {
          let attempts = 0
          const maxAttempts = 10
          
          while (attempts < maxAttempts) {
            // Add randomness to position within cell (chaotic distribution)
            const baseX = col * cellSize
            const baseY = row * cellSize
            
            // Random offset within cell (±40% of cell size for chaos)
            const offsetX = (Math.random() - 0.5) * cellSize * 0.8
            const offsetY = (Math.random() - 0.5) * cellSize * 0.8
            
            const x = baseX + cellSize / 2 + offsetX
            const y = baseY + cellSize / 2 + offsetY
            
            // Pick a genre (cycle through genres, use duplicates if needed)
            const genre = genres[genreIndex % genres.length]
            const phrase = genre.name
            const description = genre.description
            genreIndex++
            
            const phraseWidth = phrase.length * charWidth
            const phraseHeight = lineHeight
            
            // Create bounding box for this phrase (centered on position)
            const phraseBBox: BoundingBox = {
              x: x - phraseWidth / 2,
              y: y - phraseHeight / 2,
              width: phraseWidth,
              height: phraseHeight,
            }
            
            // Check if within borders
            const withinBorders = 
              phraseBBox.x > borderPadding &&
              phraseBBox.x + phraseBBox.width < windowWidth - borderPadding &&
              phraseBBox.y > borderPadding &&
              phraseBBox.y + phraseBBox.height < windowHeight - borderPadding
            
            // Check collision with occupied spaces
            let hasCollision = false
            for (const occupied of occupiedSpaces) {
              if (
                phraseBBox.x < occupied.x + occupied.width &&
                phraseBBox.x + phraseBBox.width > occupied.x &&
                phraseBBox.y < occupied.y + occupied.height &&
                phraseBBox.y + phraseBBox.height > occupied.y
              ) {
                hasCollision = true
                break
              }
            }
            
            if (withinBorders && !hasCollision) {
              // Add padding around the phrase to prevent tight packing
              occupiedSpaces.push({
                x: phraseBBox.x - 10,
                y: phraseBBox.y - 10,
                width: phraseBBox.width + 20,
                height: phraseBBox.height + 20,
              })
              
              newPhrases.push({
                id: id++,
                text: phrase,
                x,
                y,
                opacity: Math.random() * 0.15 + 0.05, // 0.05 to 0.2 opacity
                width: phraseWidth,
                description: description,
              })
              break
            }
            
            attempts++
          }
        }
      }
      
      setPhrases(newPhrases)
    }
    
    generatePhrases()
    
    // Regenerate on window resize
    const handleResize = () => {
      generatePhrases()
    }
    
    window.addEventListener('resize', handleResize)
    return () => window.removeEventListener('resize', handleResize)
  }, [])

  return (
    <div ref={containerRef} className="background-phrases">
      {phrases.map((phrase) => (
        <span
          key={phrase.id}
          className="background-phrase"
          style={{
            left: `${phrase.x}px`,
            top: `${phrase.y}px`,
            opacity: phrase.opacity,
          }}
          onMouseEnter={() => onGenreHover(phrase.description)}
          onMouseLeave={() => onGenreHover(null)}
        >
          {phrase.text}
        </span>
      ))}
    </div>
  )
}
