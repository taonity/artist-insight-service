package org.taonity.artistinsightservice.mvc

import org.taonity.artistinsightservice.attachments.AdvisoryDto

data class AdvisoryResponse(
    val advisories: Set<AdvisoryDto> = setOf()
)