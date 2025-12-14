package org.taonity.artistinsightservice.advisory

data class AdvisoryResponse(
    val advisories: Set<AdvisoryDto> = setOf()
)