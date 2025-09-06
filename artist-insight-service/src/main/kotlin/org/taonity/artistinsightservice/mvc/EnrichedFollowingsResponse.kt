package org.taonity.artistinsightservice.mvc

import org.taonity.artistinsightservice.AdvisoryDto

data class EnrichedFollowingsResponse(
    val artists: List<EnrichableArtists>,
    val advisories: Set<AdvisoryDto> = setOf(),
    val gptUsagesLeft: Int
)
