package org.taonity.artistinsightservice.mvc

import org.taonity.artistinsightservice.AdvisoryDto

data class EnrichedFollowingsResponse(
    val artists: List<EnrichableArtists>,
    val advisories: MutableList<AdvisoryDto> = mutableListOf(),
    val gptUsagesLeft: Int
)
