package org.taonity.artistinsightservice

import org.springframework.stereotype.Component
import org.springframework.web.context.annotation.RequestScope

@Component
@RequestScope
data class ResponseAttachments(
    val advisories: MutableList<Advisory> = mutableListOf()
) {
    fun advisoryDtos(): List<AdvisoryDto> {
        return advisories.map { it.toDto() }
    }
}