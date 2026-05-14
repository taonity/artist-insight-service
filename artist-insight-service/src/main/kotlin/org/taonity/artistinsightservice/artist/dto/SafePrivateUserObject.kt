package org.taonity.artistinsightservice.artist.dto

import org.taonity.spotify.model.ImageObject
import org.taonity.spotify.model.PrivateUserObject

data class SafePrivateUserObject(
    val id: String,
    val displayName: String,
    val images: MutableList<ImageObject>
) {
    companion object {
        fun fromApi(api: PrivateUserObject): SafePrivateUserObject = SafePrivateUserObject(
            id = requireNotBlank(api.id, "id"),
            displayName = requireNotBlank(api.displayName, "displayName"),
            images = requireNotNull(api.images) { "images must not be null" }
        )

        private fun requireNotBlank(value: String?, field: String): String {
            require(!value.isNullOrBlank()) { "$field must not be null or blank" }
            return value
        }
    }
}
