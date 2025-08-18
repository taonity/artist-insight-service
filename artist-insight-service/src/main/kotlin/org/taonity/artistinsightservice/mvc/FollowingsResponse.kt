package org.taonity.artistinsightservice.mvc

import org.taonity.artistinsightservice.AdvisoryDto

data class FollowingsResponse(
    val artists: List<EnrichableArtists>,
    val advisories: MutableList<AdvisoryDto> = mutableListOf()
)
