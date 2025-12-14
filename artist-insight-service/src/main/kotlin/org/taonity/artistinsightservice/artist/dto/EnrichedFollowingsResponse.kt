package org.taonity.artistinsightservice.artist.dto

import org.taonity.artistinsightservice.advisory.AdvisoryDto

data class EnrichedFollowingsResponse(
    val artists: List<EnrichableArtists>,
    val advisories: Set<AdvisoryDto> = setOf(),
    val gptUsagesLeft: Int
)
