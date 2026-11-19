package org.taonity.artistinsightservice.console.dto

import com.fasterxml.jackson.databind.JsonNode
import org.springframework.data.domain.Page
import org.taonity.artistinsightservice.console.entity.ConsoleRole
import org.taonity.artistinsightservice.user.entity.SpotifyUserEntity

data class AccessInfo(
    val spotifyId: String,
    val role: ConsoleRole,
    val isAdmin: Boolean,
    val isOwner: Boolean,
)

data class UserSummary(
    val spotifyId: String,
    val displayName: String,
    val gptUsagesLeft: Int,
    val role: ConsoleRole,
) {
    companion object {
        fun from(u: SpotifyUserEntity) =
            UserSummary(u.spotifyId, u.displayName, u.gptUsagesLeft, u.consoleRole)
    }
}

data class ChangeRoleBody(val role: ConsoleRole)

data class UpdateConfigBody(val values: Map<String, JsonNode>)

data class PageResponse<T>(
    val content: List<T>,
    val totalElements: Long,
    val totalPages: Int,
    val number: Int,
    val size: Int,
) {
    companion object {
        fun <T : Any> of(p: Page<T>) =
            PageResponse(p.content, p.totalElements, p.totalPages, p.number, p.size)
    }
}
