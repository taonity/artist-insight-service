package org.taonity.artistinsightservice.followings

import org.taonity.artistinsightservice.attachments.AdvisoryDto
import org.taonity.artistinsightservice.followings.dto.EnrichableArtists

data class FollowingsResponse(
    val artists: List<EnrichableArtists>,
    val advisories: Set<AdvisoryDto> = setOf()
)
