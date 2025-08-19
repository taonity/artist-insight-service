package org.taonity.artistinsightservice.mvc

import org.taonity.artistinsightservice.Advisory

data class FollowingsResponse(
    val artists: List<EnrichableArtists>,
    val warnings: List<Advisory> = emptyList()
)
