package org.taonity.artistinsightservice.share.dto

import org.taonity.artistinsightservice.artist.dto.SafeArtistObject

data class SharedArtistsResponse(
    val owner: ShareOwnerInfo,
    val artists: List<SafeArtistObject>,
    val mergedGenres: List<String>
)
