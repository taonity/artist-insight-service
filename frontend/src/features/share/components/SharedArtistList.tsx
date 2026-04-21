'use client'

import React from 'react'
import Image from 'next/image'
import { AgGridReact } from 'ag-grid-react'
import {
  AllCommunityModule,
  ColDef,
  ICellRendererParams,
  ModuleRegistry,
} from 'ag-grid-community'
import Genres from '@/features/artists/components/Genres'
import type { SharedArtist } from '@/types/share'

ModuleRegistry.registerModules([AllCommunityModule])

interface Props {
  artists: SharedArtist[]
}

function AvatarCell({ data }: ICellRendererParams<SharedArtist>) {
  if (!data) {
    return null
  }

  const artist = data.artistObject
  const imageUrl = artist.images?.[0]?.url ?? '/default-artist-pfp.jpeg'

  return (
    <a
      href={artist.externalUrls.spotify}
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
}

function GenresCell({ data }: ICellRendererParams<SharedArtist>) {
  if (!data) {
    return null
  }

  const spotifyGenres = data.artistObject.genres ?? []
  const enrichedGenres = data.enrichedGenres ?? []

  if (spotifyGenres.length === 0 && enrichedGenres.length === 0) {
    return null
  }

  return (
    <div style={{ display: 'flex', alignItems: 'center', gap: '8px', height: '100%' }}>
      {spotifyGenres.length > 0 && <Genres genres={spotifyGenres} enriched={false} />}
      {enrichedGenres.length > 0 && <Genres genres={enrichedGenres} enriched={true} />}
    </div>
  )
}

const columnDefs: ColDef<SharedArtist>[] = [
  {
    headerName: 'Avatar',
    field: 'artistObject.images',
    cellRenderer: AvatarCell,
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
    cellRenderer: GenresCell,
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

const SharedArtistList: React.FC<Props> = ({ artists }) => {
  return (
    <div style={{ width: '100%' }} className="ag-theme-quartz-dark">
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