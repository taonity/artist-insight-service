package org.taonity.artistinsightservice.integration.spotify.dto

import org.taonity.spotify.model.ArtistObject
import org.taonity.spotify.model.ExternalUrlObject
import org.taonity.spotify.model.FollowersObject
import org.taonity.spotify.model.ImageObject

data class SafeArtistObject(
    val id: String,
    val name: String,
    var genres: List<String>,
    val href: String,
    val images: MutableList<ImageObject>,
    val externalUrls: ExternalUrlObject,
    val followers: FollowersObject,
    val popularity: Int
) {
    companion object {
        fun fromApi(api: ArtistObject): SafeArtistObject = SafeArtistObject(
            id = requireNotBlank(api.id, "id"),
            name = requireNotBlank(api.name, "name"),
            genres = requireNotNull(api.genres) { "genres must not be null" },
            href = requireNotBlank(api.href, "href"),
            images = requireNotNull(api.images) { "images must not be null" },
            externalUrls = requireNotNull(api.externalUrls) { "externalUrls must not be null" },
            followers = requireNotNull(api.followers) { "followers must not be null" },
            popularity = requireNotNull(api.popularity) { "popularity must not be null" }
        )

        private fun requireNotBlank(value: String?, field: String): String {
            require(!value.isNullOrBlank()) { "$field must not be null or blank" }
            return value
        }
    }
}
