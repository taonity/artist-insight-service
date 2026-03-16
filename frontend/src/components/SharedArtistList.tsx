'use client'

import React, { useMemo } from 'react'
import Image from 'next/image'
import { AgGridReact } from 'ag-grid-react'
import { ColDef, ModuleRegistry, AllCommunityModule } from 'ag-grid-community'
import Genres from './Genres'

ModuleRegistry.registerModules([AllCommunityModule])

interface Artist {
  id: string
  name: string
  images?: { url: string }[]
  genres?: string[]
  externalUrls: { spotify: string }
  followers: { total: number }
  popularity: number
}

interface SharedArtist {
  artistObject: Artist
  enrichedGenres: string[]
}

interface Props {
  artists: SharedArtist[]
}

const SharedArtistList: React.FC<Props> = ({ artists }) => {
  const containerStyle = useMemo(() => ({ width: '100%' }), [])

  const columnDefs: ColDef<SharedArtist>[] = [
    {
      headerName: 'Avatar',
      field: 'artistObject.images',
      cellRenderer: (params: any) => {
        const artist = params.data.artistObject
        const imageUrl =
          artist.images && artist.images.length > 0
            ? artist.images[0].url
            : '/default-artist-pfp.jpeg'
        const spotifyUrl = artist.externalUrls.spotify

        return (
          <a
            href={spotifyUrl}
            target="_blank"
            rel="noopener noreferrer"
            style={{ display: 'flex', alignItems: 'center', height: '100%' }}
          >
            <Image
              src={imageUrl}
              alt={artist.name}
              width={40}
              height={40}
              style={{ borderRadius: '50%', objectFit: 'cover' }}
            />
          </a>
        )
      },
      width: 90,
      sortable: false,
      filter: false,
    },
    {
      headerName: 'Name',
      field: 'artistObject.name',
      sortable: true,
      filter: true,
      flex: 1,
      cellStyle: { display: 'flex', alignItems: 'center' },
    },
    {
      headerName: 'Genres',
      cellRenderer: (params: any) => {
        const sharedArtist: SharedArtist = params.data
        const spotifyGenres = sharedArtist.artistObject.genres ?? []
        const enrichedGenres = sharedArtist.enrichedGenres ?? []
        if (spotifyGenres.length === 0 && enrichedGenres.length === 0) return null
        return (
          <div style={{ display: 'flex', alignItems: 'center', gap: '8px', height: '100%' }}>
            {spotifyGenres.length > 0 && <Genres genres={spotifyGenres} enriched={false} />}
            {enrichedGenres.length > 0 && <Genres genres={enrichedGenres} enriched={true} />}
          </div>
        )
      },
      flex: 2,
      sortable: false,
    },
    {
      headerName: 'Followers',
      field: 'artistObject.followers.total',
      sortable: true,
      filter: 'agNumberColumnFilter',
      width: 120,
      valueFormatter: (params) => (params.value ? params.value.toLocaleString() : '0'),
      cellStyle: { display: 'flex', alignItems: 'center' },
    },
    {
      headerName: 'Popularity',
      field: 'artistObject.popularity',
      sortable: true,
      filter: 'agNumberColumnFilter',
      width: 120,
      cellStyle: { display: 'flex', alignItems: 'center' },
    },
  ]

  return (
    <div style={containerStyle} className="ag-theme-quartz-dark">
      <AgGridReact
        rowData={artists}
        columnDefs={columnDefs}
        domLayout="autoHeight"
        rowHeight={60}
        suppressHorizontalScroll={true}
      />
    </div>
  )
}

export default SharedArtistList
