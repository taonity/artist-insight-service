package org.taonity.artistinsightservice

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import org.taonity.spotify.model.ArtistObject
import org.taonity.spotify.model.ImageObject

data class SafeArtistObject(
    val id: String,
    val name: String,
    val genres: List<String>,
    val href: String,
    val images: MutableList<ImageObject>
)

data class ValidatedArtistObject(
    @field:NotBlank
    val id: String?,
    @field:NotBlank
    val name: String?,
    @field:NotNull
    val genres: List<String>?,
    @field:NotBlank
    val href: String?,
    @field:NotNull
    val images: MutableList<ImageObject>?
) {
    companion object {
        fun of(artistObject: ArtistObject) : ValidatedArtistObject {
            return ValidatedArtistObject(
                artistObject.id,
                artistObject.name,
                artistObject.genres,
                artistObject.href,
                artistObject.images,
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
        )
    }
}
