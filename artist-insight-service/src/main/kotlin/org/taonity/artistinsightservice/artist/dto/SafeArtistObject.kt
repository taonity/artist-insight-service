package org.taonity.artistinsightservice.artist.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
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
)
// TODO: verify inner objects?
data class ValidatedArtistObject(
    @field:NotBlank
    @field:NotNull
    val id: String?,
    @field:NotBlank
    @field:NotNull
    val name: String?,
    @field:NotNull
    val genres: List<String>?,
    @field:NotBlank
    @field:NotNull
    val href: String?,
    @field:NotNull
    val images: MutableList<ImageObject>?,
    @field:NotNull
    val externalUrls: ExternalUrlObject?,
    @field:NotNull
    val followers: FollowersObject?,
    @field:NotNull
    val popularity: Int?
) {
    companion object {
        fun of(artistObject: ArtistObject) : ValidatedArtistObject {
            return ValidatedArtistObject(
                artistObject.id,
                artistObject.name,
                artistObject.genres,
                artistObject.href,
                artistObject.images,
                artistObject.externalUrls,
                artistObject.followers,
                artistObject.popularity
            )
        }
    }
    fun toSafe(): SafeArtistObject {
        return SafeArtistObject(
            id!!,
            name!!,
            genres!!,
            href!!,
            images!!,
            externalUrls!!,
            followers!!,
            popularity!!,
        )
    }
}
