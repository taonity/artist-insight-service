package org.taonity.artistinsightservice.followings.dto

import org.taonity.artistinsightservice.advisory.AdvisoryDto

data class FollowingsResponse(
    val artists: List<EnrichableArtists>,
    val advisories: Set<AdvisoryDto> = setOf()
)
