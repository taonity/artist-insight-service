package org.taonity.artistinsightservice.followings.dto

import org.taonity.artistinsightservice.attachments.AdvisoryDto

data class EnrichedFollowingsResponse(
    val artists: List<EnrichableArtists>,
    val advisories: Set<AdvisoryDto> = setOf(),
    val gptUsagesLeft: Int
)
