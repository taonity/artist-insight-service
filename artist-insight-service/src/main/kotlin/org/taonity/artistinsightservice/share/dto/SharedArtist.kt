package org.taonity.artistinsightservice.share.dto

import org.taonity.artistinsightservice.artist.dto.SafeArtistObject

data class SharedArtist(
    val artistObject: SafeArtistObject,
    val enrichedGenres: List<String> = emptyList()
)

