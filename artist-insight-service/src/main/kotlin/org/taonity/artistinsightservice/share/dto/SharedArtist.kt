package org.taonity.artistinsightservice.share.dto

import org.taonity.artistinsightservice.integration.spotify.dto.SafeArtistObject

data class SharedArtist(
    val artistObject: SafeArtistObject,
    val enrichedGenres: List<String> = emptyList()
)

