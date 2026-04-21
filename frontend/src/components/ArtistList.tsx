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
import Genres from '../components/Genres'

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

export interface EnrichableArtistObject {
  artistObject: Artist
  genreEnriched: boolean
}

interface Props {
  enrichableArtistObjects: EnrichableArtistObject[]
}

function AvatarCell({ data }: ICellRendererParams<EnrichableArtistObject>) {
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

function GenresCell({ data, value }: ICellRendererParams<EnrichableArtistObject, string[]>) {
  if (!data || !value || value.length === 0) {
    return null
  }

  return (
    <div style={{ display: 'flex', alignItems: 'center', height: '100%' }}>
      <Genres genres={value} enriched={data.genreEnriched} />
    </div>
  )
}

const columnDefs: ColDef<EnrichableArtistObject>[] = [
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
    field: 'artistObject.genres',
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

const ArtistList: React.FC<Props> = ({ enrichableArtistObjects }) => {

  return (
    <div style={{ width: '100%' }} className="ag-theme-quartz-dark">
      <AgGridReact
        rowData={enrichableArtistObjects}
        columnDefs={columnDefs}
        domLayout="autoHeight"
        rowHeight={60}
        suppressHorizontalScroll={true}
      />
    </div>
  )
}

export default ArtistList
