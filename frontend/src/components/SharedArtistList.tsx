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

interface Props {
  artists: Artist[]
}

const SharedArtistList: React.FC<Props> = ({ artists }) => {
  const containerStyle = useMemo(() => ({ width: '100%' }), [])

  const columnDefs: ColDef<Artist>[] = [
    {
      headerName: 'Avatar',
      field: 'images',
      cellRenderer: (params: any) => {
        const artist = params.data
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
      field: 'name',
      sortable: true,
      filter: true,
      flex: 1,
      cellStyle: { display: 'flex', alignItems: 'center' },
    },
    {
      headerName: 'Genres',
      field: 'genres',
      cellRenderer: (params: any) => {
        const genres = params.value
        if (!genres || genres.length === 0) return null
        return (
          <div style={{ display: 'flex', alignItems: 'center', height: '100%' }}>
            <Genres genres={genres} enriched={false} />
          </div>
        )
      },
      flex: 2,
      sortable: false,
    },
    {
      headerName: 'Followers',
      field: 'followers.total',
      sortable: true,
      filter: 'agNumberColumnFilter',
      width: 120,
      valueFormatter: (params) => (params.value ? params.value.toLocaleString() : '0'),
      cellStyle: { display: 'flex', alignItems: 'center' },
    },
    {
      headerName: 'Popularity',
      field: 'popularity',
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
