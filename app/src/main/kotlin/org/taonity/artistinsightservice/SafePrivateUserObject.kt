package org.taonity.artistinsightservice

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import org.taonity.spotify.model.ArtistObject
import org.taonity.spotify.model.PrivateUserObject

data class SafePrivateUserObject(
    val id: String,
    val displayName: String
)

data class ValidatedPrivateUserObject(
    @field:NotBlank
    @field:NotNull
    val id: String?,
    @field:NotBlank
    @field:NotNull
    val displayName: String?
) {
    companion object {
        fun of(privateUserObject: PrivateUserObject) : ValidatedPrivateUserObject {
            return ValidatedPrivateUserObject(
                privateUserObject.id,
                privateUserObject.displayName,
            )
        }
    }
    fun toSafe(): SafePrivateUserObject {
        return SafePrivateUserObject(
            id!!,
            displayName!!,
        )
    }
}