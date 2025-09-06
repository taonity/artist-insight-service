package org.taonity.artistinsightservice.mvc

import org.taonity.artistinsightservice.AdvisoryDto

data class ErrorResponse(
    val advisories: Set<AdvisoryDto> = setOf()
)