package org.taonity.artistinsightservice

import org.springframework.stereotype.Component
import org.springframework.web.context.annotation.RequestScope

@Component
@RequestScope
data class ResponseAttachments(
    val advisories: MutableSet<Advisory> = mutableSetOf()
) {
    fun advisoryDtos(): Set<AdvisoryDto> {
        return advisories.map { it.toDto() }
            .toSet()
    }
}