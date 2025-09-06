package org.taonity.artistinsightservice.mvc

import org.taonity.artistinsightservice.attachments.AdvisoryDto

data class ErrorResponse(
    val advisories: Set<AdvisoryDto> = setOf()
)